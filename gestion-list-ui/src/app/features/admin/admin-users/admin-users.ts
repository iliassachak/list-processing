import {Component, computed, inject, OnInit, signal} from '@angular/core';
import {ListService} from '../../../core/services/list.service';
import {AuthService} from '../../../core/services/auth.service';
import {ToastService} from '../../../core/services/toast.service';
import {User} from '../../../core/models/models';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {WsService} from '../../../core/services/ws.service';
import {Subscription} from 'rxjs';

@Component({
  selector: 'app-admin-users',
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-users.html',
  styleUrl: './admin-users.scss',
})
export class AdminUsers implements OnInit{

  private listSvc = inject(ListService);
  private auth    = inject(AuthService);
  private toast   = inject(ToastService);
  private wsSvc = inject(WsService);

  users   = signal<User[]>([]);
  loading = signal(true);
  search = signal('');

  private wsSub?: Subscription;

  // id des actions en cours pour désactiver les boutons de la ligne
  pending: Record<string, boolean> = {};

  pwdModal = {
    show: false, show_pwd: false, saving: false,
    userId: '', username: '', value: ''
  };

  get currentUserId() { return this.auth.currentUser()?.userId ?? ''; }

  filtered = computed(() => {
    const q = this.search().toLowerCase();
    return !q ? this.users()
      : this.users().filter(u =>
        u.username.toLowerCase().includes(q) ||
        u.email.toLowerCase().includes(q)
      );
  });

  ngOnInit(): void {
    this.fetchUsers();
    this.wsSvc.connect();

    this.wsSub = this.wsSvc.subscribeGlobal().subscribe((evt: any) => {
      if (evt.type === 'USER_REGISTERED') {
        this.fetchUsers();
        this.toast.show(`Nouvel utilisateur : ${evt.by}`, 'info');
      }
    });
  }

  private fetchUsers() {
    this.listSvc.getAllUsers().subscribe({
      next: u => { this.users.set(u); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  toggleEnabled(user: User) {
    const next = !user.enabled;
    this.pending[user.id] = true;
    this.listSvc.setUserEnabled(user.id, next).subscribe({
      next: updated => {
        this.users.update(list => list.map(u => u.id === user.id ? updated : u));
        this.toast.show(
          next ? `${user.username} activé` : `${user.username} désactivé`,
          next ? 'success' : 'info'
        );
        delete this.pending[user.id];
      },
      error: () => {
        this.toast.show('Opération non autorisée', 'error');
        delete this.pending[user.id];
      }
    });
  }

  toggleAdmin(user: User) {
    const giveAdmin = !user.roles.includes('ADMIN');
    this.pending[user.id] = true;
    this.listSvc.setUserAdminRole(user.id, giveAdmin).subscribe({
      next: updated => {
        this.users.update(list => list.map(u => u.id === user.id ? updated : u));
        this.toast.show(
          giveAdmin ? `${user.username} est maintenant Admin` : `Rôle Admin retiré à ${user.username}`,
          'success'
        );
        delete this.pending[user.id];
      },
      error: () => {
        this.toast.show('Opération non autorisée', 'error');
        delete this.pending[user.id];
      }
    });
  }

  openPwdModal(user: User) {
    this.pwdModal = { show: true, show_pwd: false, saving: false,
      userId: user.id, username: user.username, value: '' };
  }

  submitPassword() {
    if (!this.pwdModal.value || this.pwdModal.value.length < 6) return;
    this.pwdModal.saving = true;
    this.listSvc.changeUserPassword(this.pwdModal.userId, this.pwdModal.value).subscribe({
      next: () => {
        this.toast.show(`Mot de passe de ${this.pwdModal.username} mis à jour`, 'success');
        this.pwdModal.show = false;
        this.pwdModal.saving = false;
      },
      error: () => {
        this.toast.show('Erreur lors du changement de mot de passe', 'error');
        this.pwdModal.saving = false;
      }
    });
  }

}
