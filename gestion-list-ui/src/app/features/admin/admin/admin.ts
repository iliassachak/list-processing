import {Component, inject, OnInit, signal} from '@angular/core';
import {ListService} from '../../../core/services/list.service';
import {ListMeta} from '../../../core/models/models';
import {CommonModule} from '@angular/common';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'app-admin',
  imports: [
    CommonModule,
    RouterLink
  ],
  templateUrl: './admin.html',
  styleUrl: './admin.scss',
})
export class Admin implements OnInit {
  private listSvc = inject(ListService);
  lists = signal<ListMeta[]>([]);
  loading = signal(true);

  ngOnInit() {
    this.listSvc.getLists().subscribe({
      next: l => {
        this.lists.set(l);
        this.loading.set(false);
      }
    });
  }
}
