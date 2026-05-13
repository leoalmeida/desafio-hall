import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { TokenStorageService } from '../services/token-storage.service';

export const canActivateViewer: CanActivateFn = () => {
  const router = inject(Router);
  const tokenService = inject(TokenStorageService);
  return tokenService.isAuthenticated() ? true : router.parseUrl('/login');
};
