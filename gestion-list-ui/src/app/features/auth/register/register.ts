import {Component, inject} from '@angular/core';
import {AbstractControl, FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {AuthService} from '../../../core/services/auth.service';
import {Router, RouterLink} from '@angular/router';
import {ToastService} from '../../../core/services/toast.service';

@Component({
  selector: 'app-register',
  imports: [
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './register.html',
  styleUrl: './register.scss',
})
export class Register {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);
  private toast = inject(ToastService);

  loading = false;
  error = '';
  showPwd = false;

  form = this.fb.group({
    username: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    confirm: ['', Validators.required],
  }, {validators: this.matchPasswords});

  private matchPasswords(g: AbstractControl) {
    const pwd = g.get('password')?.value;
    const conf = g.get('confirm')?.value;
    return pwd === conf ? null : {mismatch: true};
  }

  touched(field: string) {
    const c = this.form.get(field);
    return c?.invalid && c?.touched;
  }

  submit() {
    this.form.markAllAsTouched();
    if (this.form.invalid) return;

    this.loading = true;
    this.error = '';
    const {username, email, password} = this.form.value;

    this.auth.register(username!, email!, password!).subscribe({
      next: () => {
        this.toast.show('Compte créé avec succès !', 'success');
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'Ce nom d\'utilisateur est déjà pris.';
        this.loading = false;
      }
    });
  }
}
