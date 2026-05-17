import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { TokenStorageService } from '../services/token-storage.service';

export const canActivateViewer: CanActivateFn = () => {
  const router = inject(Router);
  const tokenService = inject(TokenStorageService);
  if (!tokenService.isAuthenticated()) {
    return router.parseUrl('/login');
  }

  return tokenService.hasAnyRole(['ROLE_VIEWER', 'ROLE_APPROVER', 'ROLE_ADMIN'])
    ? true
    : router.parseUrl('/acesso-negado');
};
