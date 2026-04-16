import {HttpErrorResponse, HttpInterceptorFn} from '@angular/common/http';
import {catchError, throwError} from 'rxjs';
import {inject} from '@angular/core';
import {AuthService} from '../services/auth.service';
import {ToastService} from '../services/toast.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const auth    = inject(AuthService);
  const toast   = inject(ToastService);

  // Routes auth : le composant gère lui-même l'affichage (pas de toast)
  const AUTH_PATHS = ['/auth/login', '/auth/register'];
  const isAuthCall = AUTH_PATHS.some(p => req.url.includes(p));

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {

      const message = extractMessage(error);

      if (error.status === 401) {
        // Token expiré ou invalide → déconnexion silencieuse
        if (!isAuthCall) {
          auth.logout();
          toast.show('Votre session a expiré. Veuillez vous reconnecter.', 'error');
        }
        // Pour /auth/login → laisser le composant afficher son propre message
      }

      else if (error.status === 403) {
        toast.show(message, 'error');
      }

      else if (error.status === 404) {
        toast.show(message, 'error');
      }

      else if (error.status === 409) {
        // Conflit (ex: username déjà pris) → géré par le composant register
        if (!isAuthCall) toast.show(message, 'error');
      }

      else if (error.status === 400) {
        // Erreur de validation métier
        if (!isAuthCall) toast.show(message, 'error');
      }

      else if (error.status >= 500) {
        toast.show('Erreur serveur. Veuillez réessayer ultérieurement.', 'error');
      }

      // Propager l'erreur pour que le composant puisse aussi la gérer si besoin
      return throwError(() => error);
    })
  );
};

/**
 * Extrait le message lisible depuis la réponse ApiError du backend.
 * Format attendu : { timestamp, status, error, message, path }
 */
function extractMessage(error: HttpErrorResponse): string {
  if (error.error?.message) return error.error.message;
  if (typeof error.error === 'string' && error.error.length < 200) return error.error;

  // Fallback selon le status HTTP
  switch (error.status) {
    case 400: return 'Requête invalide.';
    case 401: return 'Non authentifié.';
    case 403: return 'Accès refusé.';
    case 404: return 'Ressource introuvable.';
    case 409: return 'Conflit : cette ressource existe déjà.';
    default:  return 'Une erreur est survenue.';
  }
}
