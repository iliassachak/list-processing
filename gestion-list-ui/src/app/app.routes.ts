import {Routes} from '@angular/router';
import {authGuard} from './core/guard/auth-guard';
import {adminGuard} from './core/guard/admin-guard';

export const routes: Routes = [
  {path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login')
        .then(m => m.Login)
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./features/auth/register/register')
        .then(m => m.Register)
  },
  {
    path: '', canActivate: [authGuard],
    loadComponent: () =>
      import('./layout/layout/layout')
        .then(m => m.Layout),
    children: [
      {
        path: '',
        redirectTo: 'lists',
        pathMatch: 'full'
      },
      {
        path: 'lists',
        loadComponent: () =>
          import('./features/list/list-home/list-home')
            .then(m => m.ListHome)
      },
      {
        path: 'lists/:id',
        loadComponent: () =>
          import('./features/list/list-view/list-view')
            .then(m => m.ListView)
      },
      {
        path: 'admin',
        canActivate: [adminGuard],
        loadComponent: () => import('./features/admin/admin/admin').then(m => m.Admin)
      },
      {
        path: 'admin/lists/:id',
        canActivate: [adminGuard],
        loadComponent: () => import('./features/admin/admin-list/admin-list').then(m => m.AdminList)
      },
    ]
  },
  {path: '**', redirectTo: ''}
];
