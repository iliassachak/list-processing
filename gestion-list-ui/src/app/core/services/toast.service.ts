import {Injectable, signal} from '@angular/core';

export interface Toast {
  id: number;
  message: string;
  type: 'success' | 'error' | 'info';
}

@Injectable({
  providedIn: 'root',
})
export class ToastService {
  toasts = signal<Toast[]>([]);
  private id = 0;

  show(message: string, type: 'success' | 'error' | 'info' = 'info') {
    const toast: Toast = {id: ++this.id, message, type};
    this.toasts.update(t => [...t, toast]);
    setTimeout(() => this.remove(toast.id), 3500);
  }

  remove(id: number) {
    this.toasts.update(t => t.filter(x => x.id !== id));
  }
}
