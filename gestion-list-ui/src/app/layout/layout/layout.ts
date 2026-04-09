import {Component, inject} from '@angular/core';
import {AuthService} from '../../core/services/auth.service';
import {RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';

@Component({
  selector: 'app-layout',
  imports: [
    RouterLink,
    RouterLinkActive,
    RouterOutlet
  ],
  templateUrl: './layout.html',
  styleUrl: './layout.scss',
})
export class Layout {
  auth = inject(AuthService);

  get initial() {
    return (this.auth.currentUser()?.username ?? 'U')[0].toUpperCase();
  }
}
