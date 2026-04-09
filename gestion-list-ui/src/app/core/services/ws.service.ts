import {inject, Injectable} from '@angular/core';
import {AuthService} from './auth.service';
import {Client, IMessage} from '@stomp/stompjs';
import {Observable, Subject} from 'rxjs';
import {WsEvent} from '../models/models';
import SockJS from 'sockjs-client';

@Injectable({
  providedIn: 'root',
})
export class WsService {
  private auth = inject(AuthService);
  private client?: Client;
  private subs = new Map<string, Subject<WsEvent>>();

  connect(): void {
    this.client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      connectHeaders: {Authorization: `Bearer ${this.auth.token}`},
      reconnectDelay: 3000,
      onStompError: (f) => console.error('STOMP error', f),
    });
    this.client.activate();
  }

  subscribeList(listId: string): Observable<WsEvent> {
    let subject = this.subs.get(listId);
    if (!subject) {
      subject = new Subject<WsEvent>();
      this.subs.set(listId, subject);
    }
    const topic = `/topic/list.${listId}`;
    const doSub = () => {
      this.client!.subscribe(topic, (msg: IMessage) => {
        try {
          subject!.next(JSON.parse(msg.body));
        } catch {
        }
      });
    };
    if (this.client?.connected) doSub();
    else this.client!.onConnect = () => doSub();
    return subject.asObservable();
  }

  disconnect(): void {
    this.client?.deactivate();
    this.subs.clear();
  }
}
