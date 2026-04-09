import {inject, Injectable, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Router} from '@angular/router';
import {tap} from 'rxjs';
import {AuthResponse} from '../models/models';

const API = 'http://localhost:8080/api/auth';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  currentUser = signal<AuthResponse | null>(this.loadStored());

  login(username: string, password: string) {
    return this.http.post<AuthResponse>(`${API}/login`, { username, password }).pipe(
      tap(res => { localStorage.setItem('auth', JSON.stringify(res)); this.currentUser.set(res); })
    );
  }

  logout() {
    localStorage.removeItem('auth');
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }

  get token() { return this.currentUser()?.token ?? null; }
  get isAdmin() { return this.currentUser()?.roles.includes('ADMIN') ?? false; }
  get isLoggedIn() { return !!this.currentUser(); }

  private loadStored(): AuthResponse | null {
    try { return JSON.parse(localStorage.getItem('auth') || 'null'); }
    catch { return null; }
  }
}
