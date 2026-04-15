import {Component, inject, OnDestroy, OnInit, signal} from '@angular/core';
import {AuthService} from '../../../core/services/auth.service';
import {ListService} from '../../../core/services/list.service';
import {ToastService} from '../../../core/services/toast.service';
import {ListMeta} from '../../../core/models/models';
import {FormsModule} from '@angular/forms';
import {DatePipe} from '@angular/common';
import {RouterLink} from '@angular/router';
import {HttpEventType} from '@angular/common/http';
import {Subscription} from 'rxjs';
import {WsService} from '../../../core/services/ws.service';

@Component({
  selector: 'app-list-home',
  imports: [
    FormsModule,
    DatePipe,
    RouterLink
  ],
  templateUrl: './list-home.html',
  styleUrl: './list-home.scss',
})
export class ListHome implements OnInit, OnDestroy {
  auth = inject(AuthService);
  private listSvc = inject(ListService);
  private toast = inject(ToastService);
  private wsSvc = inject(WsService);

  lists = signal<ListMeta[]>([]);
  loading = signal(true);
  showUpload = false;

  deletingListId: string | null = null;
  deletingListName = '';

  uploadMode: 'new' | 'append' = 'new';   // mode choisi par l'user
  uploadName    = '';
  selectedFile: File | null = null;
  uploading     = false;
  uploadProgress = 0;
  uploadPhase: 'uploading' | 'processing' | 'done' | '' = '';
  appendTargetId = '';   // id de la liste cible pour le mode append

  private wsSub?: Subscription;

  ngOnInit() {
    this.load();

    this.wsSvc.connect();
    this.wsSub = this.wsSvc.subscribeGlobal().subscribe((evt: any) => {
      if (evt.type === 'LIST_ADDED') {
        this.listSvc.getLists().subscribe(l => this.lists.set(l));
      } else if (evt.type === 'LIST_DELETED') {
        this.listSvc.getLists().subscribe(l => this.lists.set(l));
      } else if (evt.type === 'ASSIGNMENT_CHANGED') {
        this.listSvc.getLists().subscribe(l => this.lists.set(l));
      }
    });
  }

  ngOnDestroy() {
    this.wsSub?.unsubscribe();
    this.wsSvc.disconnect();
  }

  load() {
    this.loading.set(true);
    this.listSvc.getLists().subscribe({
      next: l => {
        this.lists.set(l);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  hover(e: MouseEvent, on: boolean) {
    (e.currentTarget as HTMLElement).style.transform = on ? 'translateY(-2px)' : '';
    (e.currentTarget as HTMLElement).style.boxShadow = on ? '0 4px 16px rgba(79,70,229,.15)' : '';
  }

  onFile(e: Event) {
    this.selectedFile = (e.target as HTMLInputElement).files?.[0] ?? null;
  }

  upload() {
    if (!this.selectedFile) return;
    if (this.uploadMode === 'new' && !this.uploadName) return;
    if (this.uploadMode === 'append' && !this.appendTargetId) return;

    this.uploading      = true;
    this.uploadProgress = 0;
    this.uploadPhase    = 'uploading';

    const obs = this.uploadMode === 'new'
      ? this.listSvc.uploadList(this.selectedFile, this.uploadName)
      : this.listSvc.appendToList(this.appendTargetId, this.selectedFile);

    obs.subscribe({
      next: (event: any) => {
        if (event.type === HttpEventType.UploadProgress && event.total) {
          this.uploadProgress = Math.round(90 * event.loaded / event.total);
        } else if (event.type === HttpEventType.Sent) {
          this.uploadPhase = 'processing';
        } else if (event.type === HttpEventType.Response) {
          this.uploadProgress = 100;
          this.uploadPhase    = 'done';
          const result = event.body;

          if (this.uploadMode === 'new') {
            // Ajouter la nouvelle liste dans le signal
            if (result) this.lists.update(ls => [...ls, result]);
            this.toast.show('Liste importée avec succès !', 'success');
          } else {
            // Mettre à jour le totalRows de la liste existante
            if (result) {
              this.lists.update(ls =>
                ls.map(l => l.id === result.id ? { ...l, totalRows: result.totalRows } : l)
              );
            }
            this.toast.show('Lignes ajoutées avec succès !', 'success');
          }

          setTimeout(() => {
            this.showUpload      = false;
            this.uploading       = false;
            this.uploadProgress  = 0;
            this.uploadPhase     = '';
            this.uploadName      = '';
            this.selectedFile    = null;
            this.appendTargetId  = '';
            this.uploadMode      = 'new';
          }, 600);
        }
      },
      error: (err) => {
        const msg = err?.error?.message ?? 'Erreur lors de l\'import';
        this.toast.show(msg, 'error');
        this.uploading      = false;
        this.uploadProgress = 0;
        this.uploadPhase    = '';
      }
    });
  }

  confirmDelete(list: ListMeta, event: MouseEvent) {
    event.preventDefault();
    event.stopPropagation();

    if (!confirm(
      `Supprimer définitivement la liste "${list.name}" ?\n\n` +
      `Toutes ses lignes, colonnes, assignations et permissions seront supprimées.`
    )) return;

    this.deletingListId = list.id;
    this.deletingListName = list.name;

    this.listSvc.deleteList(list.id).subscribe({
      next: () => {
        this.lists.update(ls => ls.filter(l => l.id !== list.id));
        this.toast.show(`Liste "${list.name}" supprimée`, 'success');
        this.deletingListId = null;
        this.deletingListName = '';
      },
      error: () => {
        this.toast.show('Erreur lors de la suppression', 'error');
        this.deletingListId = null;
        this.deletingListName = '';
      }
    });
  }
}
