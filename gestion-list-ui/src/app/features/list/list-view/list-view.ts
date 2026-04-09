import {Component, inject, OnDestroy, OnInit, signal} from '@angular/core';
import {AuthService} from '../../../core/services/auth.service';
import {ActivatedRoute, RouterLink} from '@angular/router';
import {ListService} from '../../../core/services/list.service';
import {WsService} from '../../../core/services/ws.service';
import {ToastService} from '../../../core/services/toast.service';
import {Column, ListMeta, Row, WsEvent} from '../../../core/models/models';
import {CellValueChangedEvent, ColDef, GetRowIdFunc, GetRowIdParams, GridApi, GridReadyEvent} from 'ag-grid-community';
import {Subscription} from 'rxjs';
import {AgGridAngular} from 'ag-grid-angular';
import {FormsModule} from '@angular/forms';
import {style} from '@angular/animations';

@Component({
  selector: 'app-list-view',
  imports: [
    RouterLink,
    AgGridAngular,
    FormsModule
  ],
  templateUrl: './list-view.html',
  styleUrl: './list-view.scss',
})
export class ListView implements OnInit, OnDestroy {
  auth = inject(AuthService);
  private route = inject(ActivatedRoute);
  private listSvc = inject(ListService);
  private wsSvc = inject(WsService);
  private toast = inject(ToastService);

  listId!: string;
  meta = signal<ListMeta | null>(null);
  rows = signal<Row[]>([]);
  filteredRows = signal<Row[]>([]);
  colDefs = signal<ColDef[]>([]);
  loading = signal(true);
  wsConnected = signal(false);
  activeFilters = signal<{ col: string, val: string }[]>([]);

  showAddRow = false;
  addingRow = false;
  newRowData: Record<string, any> = {};
  newRowAssignTo = '';
  users = signal<any[]>([]);

  filterModal = {show: false, col: '', val: '', suggestions: [] as string[]};

  private gridApi?: GridApi;
  private editableCols = new Set<string>();
  private wsSub?: Subscription;
  private filterMap = new Map<string, string>();

  readonly defaultColDef: ColDef = {sortable: true, resizable: true, minWidth: 100, flex: 1};
  readonly getRowId: GetRowIdFunc = (p: GetRowIdParams) => p.data.id;

  get gridHeight() {
    return `${Math.max(300, Math.min(window.innerHeight - 280, 600))}px`;
  }

  ngOnInit() {
    this.listId = this.route.snapshot.paramMap.get('id')!;
    this.load();
    if (this.auth.isAdmin) this.loadUsers();
  }

  load() {
    this.loading.set(true);
    Promise.all([
      this.listSvc.getList(this.listId).toPromise(),
      this.listSvc.getRows(this.listId).toPromise(),
      this.listSvc.getEditableCols(this.listId).toPromise(),
    ]).then(([meta, rows, editCols]) => {
      this.meta.set(meta!);
      this.rows.set(rows!);
      this.editableCols = new Set(editCols ?? []);
      this.buildColDefs(meta!.columns);
      this.applyFilterMap();
      this.loading.set(false);
      this.connectWs();
    }).catch(() => this.loading.set(false));
  }

  loadUsers() {
    this.listSvc.getUsers().subscribe(u => this.users.set(u));
  }

  buildColDefs(cols: Column[]) {
    const defs: ColDef[] = [];

    if (this.auth.isAdmin) {
      defs.push({
        field: '_actions', headerName: '', width: 60, sortable: false, resizable: false,
        cellRenderer: (p: any) => {
          const btn = document.createElement('button');
          btn.innerHTML = '🗑️';
          btn.style.cssText = 'background:none;border:none;cursor:pointer;font-size:14px;padding:2px';
          btn.title = 'Supprimer';
          btn.addEventListener('click', () => this.deleteRow(p.data.id));
          return btn;
        }
      });
    }

    cols.forEach(col => {
      const isEditable = this.editableCols.has(col.name);
      defs.push({
        field: `data.${col.name}`,
        headerName: col.name,
        editable: isEditable,
        cellStyle: isEditable ? {backgroundColor: '#f0f9ff', cursor: 'pointer'} : {},
        filter: false,
        headerComponent: 'agColumnHeader',
        headerComponentParams: {
          template: `<div style="display:flex;align-items:center;gap:4px;width:100%">
            <span style="flex:1">${col.name}</span>
            <span style="cursor:pointer;color:#4f46e5;font-size:11px" data-filter="1">🔍</span>
          </div>`
        },
      });
    });
    this.colDefs.set(defs);

    // After grid is ready, attach filter button listeners
    setTimeout(() => this.attachFilterButtons(cols), 300);
  }

  attachFilterButtons(cols: Column[]) {
    document.querySelectorAll('[data-filter="1"]').forEach((el, i) => {
      el.addEventListener('click', (e) => {
        e.stopPropagation();
        const col = cols[i].name;
        this.openFilterModal(col);
      });
    });
  }

  openFilterModal(colName: string) {
    const vals = [...new Set(this.rows().map(r => String(r.data[colName] ?? '')))].sort();
    this.filterModal = {show: true, col: colName, val: this.filterMap.get(colName) ?? '', suggestions: vals};
  }

  applyFilter() {
    if (this.filterModal.val.trim()) {
      this.filterMap.set(this.filterModal.col, this.filterModal.val.trim());
    } else {
      this.filterMap.delete(this.filterModal.col);
    }
    this.filterModal.show = false;
    this.applyFilterMap();
  }

  applyFilterMap() {
    this.activeFilters.set([...this.filterMap.entries()].map(([col, val]) => ({col, val})));
    let result = this.rows();
    this.filterMap.forEach((val, col) => {
      result = result.filter(r => String(r.data[col] ?? '').toLowerCase().includes(val.toLowerCase()));
    });
    this.filteredRows.set(result);
  }

  removeFilter(col: string) {
    this.filterMap.delete(col);
    this.applyFilterMap();
  }

  clearFilters() {
    this.filterMap.clear();
    this.applyFilterMap();
  }

  onGridReady(e: GridReadyEvent) {
    this.gridApi = e.api;
  }

  onCellChanged(e: CellValueChangedEvent) {
    if (e.colDef.field === '_actions') return;
    const row: Row = e.data;
    this.listSvc.updateCell(this.listId, row.id, e.colDef.headerName!, e.newValue).subscribe({
      error: () => {
        this.toast.show('Erreur: modification non autorisée', 'error');
        this.rows.update(rows => rows.map(r => r.id === row.id ? {
          ...r,
          data: {...r.data, [e.colDef.field!]: e.oldValue}
        } : r));
        this.applyFilterMap();
      }
    });
  }

  connectWs() {
    this.wsSvc.connect();
    this.wsSub = this.wsSvc.subscribeList(this.listId).subscribe((evt: WsEvent) => {
      this.wsConnected.set(true);
      this.handleWsEvent(evt);
    });
  }

  handleWsEvent(evt: WsEvent) {
    if (evt.type === 'CELL_UPDATED') {
      this.rows.update(rows => rows.map(r => r.id === evt.rowId
        ? {...r, data: {...r.data, [evt.columnName]: evt.value}, lastModifiedBy: evt.by}
        : r));
      this.applyFilterMap();
      this.gridApi?.applyTransaction({update: [{id: evt.rowId, [evt.columnName]: evt.value}]});
    } else if (evt.type === 'ROW_ADDED') {
      const newRow: Row = {
        id: evt.rowId,
        rowIndex: evt.rowIndex,
        data: evt.data,
        lastModifiedAt: evt.at,
        lastModifiedBy: evt.by
      };
      if (this.auth.isAdmin) {
        this.rows.update(r => [...r, newRow]);
        this.applyFilterMap();
      }
      this.toast.show(`Nouvelle ligne ajoutée par ${evt.by}`, 'info');
    } else if (evt.type === 'ROW_DELETED') {
      this.rows.update(r => r.filter(x => x.id !== evt.rowId));
      this.applyFilterMap();
    } else if (evt.type === 'ASSIGNMENT_CHANGED' && !this.auth.isAdmin) {
      this.load();
    }
  }

  deleteRow(rowId: string) {
    if (!confirm('Supprimer cette ligne ?')) return;
    this.listSvc.deleteRow(this.listId, rowId).subscribe({
      next: () => {
        this.rows.update(r => r.filter(x => x.id !== rowId));
        this.applyFilterMap();
        this.toast.show('Ligne supprimée', 'success');
      },
      error: () => this.toast.show('Erreur suppression', 'error')
    });
  }

  addRow() {
    this.addingRow = true;
    const data = {...this.newRowData};
    this.listSvc.addRow(this.listId, data, this.newRowAssignTo || null).subscribe({
      next: (row) => {
        this.rows.update(r => [...r, row]);
        this.applyFilterMap();
        this.toast.show('Ligne ajoutée', 'success');
        this.showAddRow = false;
        this.addingRow = false;
        this.newRowData = {};
        this.newRowAssignTo = '';
      },
      error: () => {
        this.toast.show('Erreur ajout', 'error');
        this.addingRow = false;
      }
    });
  }

  downloadExcel() {
    this.listSvc.downloadList(this.listId, this.meta()?.name ?? 'liste').subscribe(blob => {
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `${this.meta()?.name ?? 'liste'}.xlsx`;
      a.click();
      URL.revokeObjectURL(url);
      this.toast.show('Téléchargement démarré', 'success');
    });
  }

  ngOnDestroy() {
    this.wsSub?.unsubscribe();
    this.wsSvc.disconnect();
  }

  protected readonly HTMLElement = HTMLElement;
  protected readonly style = style;
}
