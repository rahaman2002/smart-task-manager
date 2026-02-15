import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';

export const AuthGuard: CanActivateFn = () => {
  const router = inject(Router);
  const token = localStorage.getItem('token');

  console.log('AuthGuard token:', token); // 🔍 debug

  if (token && token.length > 0) {
    return true;
  }

  router.navigate(['/']);
  return false;
};
