import { TestBed } from '@angular/core/testing';
import {
  ActivatedRouteSnapshot,
  CanActivateFn,
  RouterStateSnapshot,
  UrlTree,
  provideRouter,
} from '@angular/router';
import { TokenStorageService } from '../services/token-storage.service';
import { signal } from '@angular/core';

import { canActivateViewer } from './can-activate-viewer';

describe('canActivateViewer', () => {
  const isAuthenticatedSignal = signal(false);

  const executeGuard: CanActivateFn = (...guardParameters) =>
    TestBed.runInInjectionContext(() => canActivateViewer(...guardParameters));

  beforeEach(() => {
    const spy = {
      isAuthenticated: isAuthenticatedSignal.asReadonly(),
    };

    TestBed.configureTestingModule({
      providers: [
        { provide: TokenStorageService, useValue: spy },
        provideRouter([]),
      ],
    });

    TestBed.inject(TokenStorageService);
  });

  it('deve ser criado', () => {
    expect(executeGuard).toBeTruthy();
  });

  it('deve retornar true se o usuário estiver autenticado', () => {
    isAuthenticatedSignal.set(true);
    const result = executeGuard(
      {} as ActivatedRouteSnapshot,
      {} as RouterStateSnapshot,
    );
    expect(result).toBe(true);
  });

  it('deve redirecionar para /login se o usuário não estiver autenticado', () => {
    isAuthenticatedSignal.set(false);
    const result = executeGuard(
      {} as ActivatedRouteSnapshot,
      {} as RouterStateSnapshot,
    );
    expect(result instanceof UrlTree).toBe(true);
    expect((result as UrlTree).toString()).toBe('/login');
  });
});
