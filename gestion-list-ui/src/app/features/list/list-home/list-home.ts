import {Component, inject, OnInit, signal} from '@angular/core';
import {AuthService} from '../../../core/services/auth.service';
import {ListService} from '../../../core/services/list.service';
import {ToastService} from '../../../core/services/toast.service';
import {ListMeta} from '../../../core/models/models';
import {FormsModule} from '@angular/forms';
import {DatePipe} from '@angular/common';
import {RouterLink} from '@angular/router';
import {HttpEventType} from '@angular/common/http';

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
export class ListHome implements OnInit {
  auth = inject(AuthService);
  private listSvc = inject(ListService);
  private toast = inject(ToastService);

  lists = signal<ListMeta[]>([]);
  loading = signal(true);
  showUpload = false;
  uploadName = '';
  selectedFile: File | null = null;
  uploading = false;
  uploadProgress = 0;          // 0-100
  uploadPhase: 'uploading' | 'processing' | 'done' | '' = '';
  deletingListId: string | null = null;
  deletingListName = '';

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.listSvc.getLists().subscribe({
      next: l => {
        this.lists.set(l);
        console.log(l);
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
    if (!this.selectedFile || !this.uploadName) return;
    this.uploading = true;
    this.uploadProgress = 0;
    this.uploadPhase = 'uploading';

    this.listSvc.uploadList(this.selectedFile, this.uploadName).subscribe({
      next: (event: any) => {
        if (event.type === HttpEventType.UploadProgress && event.total) {
          // Pendant le transfert fichier → 0..90%
          this.uploadProgress = Math.round(90 * event.loaded / event.total);
        } else if (event.type === HttpEventType.Response) {
          // Serveur a répondu → passe à 100%
          this.uploadProgress = 100;
          this.uploadPhase = 'done';
          setTimeout(() => {
            this.toast.show('Liste importée avec succès !', 'success');
            this.showUpload = false;
            this.uploading = false;
            this.uploadProgress = 0;
            this.uploadPhase = '';
            this.uploadName = '';
            this.selectedFile = null;
            this.load();
          }, 600);
        } else if (event.type === HttpEventType.Sent) {
          // Requête envoyée, serveur traite → montre "Traitement..."
          this.uploadPhase = 'processing';
        }
      },
      error: () => {
        this.toast.show('Erreur lors de l\'import', 'error');
        this.uploading = false;
        this.uploadProgress = 0;
        this.uploadPhase = '';
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
