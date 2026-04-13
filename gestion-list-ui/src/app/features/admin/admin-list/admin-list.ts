import {Component, inject, OnDestroy, OnInit, signal} from '@angular/core';
import {ActivatedRoute, RouterLink} from '@angular/router';
import {ListService} from '../../../core/services/list.service';
import {ToastService} from '../../../core/services/toast.service';
import {Assignment, ListMeta} from '../../../core/models/models';
import {FormsModule} from '@angular/forms';
import {Subscription} from 'rxjs';
import {WsService} from '../../../core/services/ws.service';

@Component({
  selector: 'app-admin-list',
  imports: [
    FormsModule,
    RouterLink
  ],
  templateUrl: './admin-list.html',
  styleUrl: './admin-list.scss',
})
export class AdminList implements OnInit, OnDestroy {
  private route = inject(ActivatedRoute);
  private listSvc = inject(ListService);
  private toast = inject(ToastService);
  private wsSvc = inject(WsService);

  listId!: string;
  meta = signal<ListMeta | null>(null);
  assignments = signal<Assignment[]>([]);
  users = signal<any[]>([]);
  permissions = signal<Record<string, Record<string, boolean>>>({});

  private wsSub?: Subscription;

  assign = {userId: '', start: 0, end: 0};

  ngOnInit() {
    this.listId = this.route.snapshot.paramMap.get('id')!;
    this.loadAll();

    this.wsSvc.connect();
    this.wsSub = this.wsSvc.subscribeGlobal().subscribe((evt: any) => {
      if (evt.type === 'USER_REGISTERED') {
        // Un nouvel utilisateur vient de s'inscrire → recharger la liste
        this.listSvc.getUsers().subscribe(u =>
          this.users.set(u.filter((x: any) => !x.roles.includes('ADMIN')))
        );
        this.toast.show(`Nouvel utilisateur : ${evt.by}`, 'info');
      } else if (evt.type === 'ASSIGNMENT_CHANGED' && evt.listId === this.listId) {
        this.listSvc.getAssignments(this.listId).subscribe(a => this.assignments.set(a));
      }
    });
  }

  ngOnDestroy() {
    this.wsSub?.unsubscribe();
    this.wsSvc.disconnect();
  }

  loadAll() {
    this.listSvc.getList(this.listId).subscribe(m => this.meta.set(m));
    this.listSvc.getUsers().subscribe(u => this.users.set(u.filter((x: any) => !x.roles.includes('ADMIN'))));
    this.listSvc.getAssignments(this.listId).subscribe(a => this.assignments.set(a));
    this.listSvc.getPermissions(this.listId).subscribe(p => this.permissions.set(p));
  }

  addAssignment() {
    if (!this.assign.userId) return;
    this.listSvc.assign(this.listId, this.assign.userId, this.assign.start, this.assign.end).subscribe({
      next: (a) => {
        this.assignments.update(arr => [...arr, a]);
        this.toast.show('Assignation créée', 'success');
      },
      error: () => this.toast.show('Erreur assignation', 'error')
    });
  }

  deleteAssignment(id: string) {
    this.listSvc.deleteAssignment(this.listId, id).subscribe({
      next: () => {
        this.assignments.update(arr => arr.filter(a => a.id !== id));
        this.toast.show('Assignation supprimée', 'success');
      }
    });
  }

  getPermission(userId: string, colName: string): boolean {
    return this.permissions()[userId]?.[colName] ?? false;
  }

  togglePermission(colName: string, userId: string, event: Event) {
    const canEdit = (event.target as HTMLInputElement).checked;
    this.listSvc.setPermission(this.listId, colName, userId, canEdit, true).subscribe({
      next: () => {
        this.permissions.update(p => ({
          ...p,
          [userId]: {...(p[userId] ?? {}), [colName]: canEdit}
        }));
        this.toast.show(canEdit ? 'Permission accordée' : 'Permission retirée', 'success');
      },
      error: () => this.toast.show('Erreur permission', 'error')
    });
  }
}
