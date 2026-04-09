import {Component, inject} from '@angular/core';
import {FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {AuthService} from '../../../core/services/auth.service';
import {Router} from '@angular/router';
import {ToastService} from '../../../core/services/toast.service';

@Component({
  selector: 'app-login',
  imports: [
    ReactiveFormsModule
  ],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);
  private toast = inject(ToastService);

  form = this.fb.group({username: ['', Validators.required], password: ['', Validators.required]});
  loading = false;
  error = '';

  submit() {
    if (this.form.invalid) return;
    this.loading = true;
    this.error = '';
    const {username, password} = this.form.value;
    this.auth.login(username!, password!).subscribe({
      next: () => this.router.navigate(['/']),
      error: () => {
        this.error = 'Identifiants incorrects';
        this.loading = false;
      }
    });
  }
}
