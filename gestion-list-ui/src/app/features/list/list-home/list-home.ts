import {Component, inject, OnInit, signal} from '@angular/core';
import {AuthService} from '../../../core/services/auth.service';
import {ListService} from '../../../core/services/list.service';
import {ToastService} from '../../../core/services/toast.service';
import {ListMeta} from '../../../core/models/models';
import {FormsModule} from '@angular/forms';
import {DatePipe} from '@angular/common';
import {RouterLink} from '@angular/router';

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
    this.listSvc.uploadList(this.selectedFile, this.uploadName).subscribe({
      next: () => {
        this.toast.show('Liste importée avec succès !', 'success');
        this.showUpload = false;
        this.uploading = false;
        this.uploadName = '';
        this.selectedFile = null;
        this.load();
      },
      error: () => {
        this.toast.show('Erreur lors de l\'import', 'error');
        this.uploading = false;
      }
    });
  }

  confirmDelete(list: ListMeta, event: MouseEvent) {
    event.preventDefault();
    event.stopPropagation(); // évite la navigation vers la liste
    if (!confirm(`Supprimer définitivement la liste "${list.name}" ?\n\nToutes ses lignes, colonnes, assignations et permissions seront supprimées.`)) return;
    this.listSvc.deleteList(list.id).subscribe({
      next: () => {
        this.lists.update(ls => ls.filter(l => l.id !== list.id));
        this.toast.show(`Liste "${list.name}" supprimée`, 'success');
      },
      error: () => this.toast.show('Erreur lors de la suppression', 'error')
    });
  }
}
