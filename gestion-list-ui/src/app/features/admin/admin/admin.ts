import {Component, inject, OnDestroy, OnInit, signal} from '@angular/core';
import {ListService} from '../../../core/services/list.service';
import {ListMeta} from '../../../core/models/models';
import {CommonModule} from '@angular/common';
import {RouterLink} from '@angular/router';
import {WsService} from '../../../core/services/ws.service';
import {Subscription} from 'rxjs';

@Component({
  selector: 'app-admin',
  imports: [
    CommonModule,
    RouterLink
  ],
  templateUrl: './admin.html',
  styleUrl: './admin.scss',
})
export class Admin implements OnInit, OnDestroy {
  private listSvc = inject(ListService);
  lists = signal<ListMeta[]>([]);
  loading = signal(true);
  private wsSvc = inject(WsService);
  private wsSub?: Subscription;

  ngOnInit() {
    this.load();
    this.wsSvc.connect();
    this.wsSub = this.wsSvc.subscribeGlobal().subscribe((evt: any) => {
      if (evt.type === 'LIST_ADDED') {
        this.listSvc.getLists().subscribe(l => this.lists.set(l));
      } else if (evt.type === 'LIST_DELETED') {
        this.listSvc.getLists().subscribe(l => this.lists.set(l));
      }
    });
  }

  ngOnDestroy() {
    this.wsSub?.unsubscribe();
    this.wsSvc.disconnect();
  }

  load(){
    this.listSvc.getLists().subscribe({
      next: l => {
        this.lists.set(l);
        this.loading.set(false);
      }
    });
  }
}
