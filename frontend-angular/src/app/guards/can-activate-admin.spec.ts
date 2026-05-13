import { TestBed } from '@angular/core/testing';
import {
  ActivatedRouteSnapshot,
  CanActivateFn,
  Router,
  RouterStateSnapshot,
  UrlTree,
} from '@angular/router';
import { TokenStorageService } from '../services/token-storage.service';
import { signal } from '@angular/core';

import { canActivateAdmin } from './can-activate-admin';
import { createSpyObj, SpyObj } from '../../test-helpers/spy-utils';

describe('canActivateAdmin', () => {
  let tokenStorageSpy: SpyObj<TokenStorageService, 'hasRole'>;
  let routerSpy: SpyObj<Router, 'parseUrl'>;
  const isAuthenticatedSignal = signal(false);

  const executeGuard: CanActivateFn = (...guardParameters) =>
    TestBed.runInInjectionContext(() => canActivateAdmin(...guardParameters));

  beforeEach(() => {
    tokenStorageSpy = createSpyObj<TokenStorageService>(['hasRole'], {
      isAuthenticated: isAuthenticatedSignal.asReadonly(),
    } as Partial<TokenStorageService>);
    routerSpy = createSpyObj<Router>(['parseUrl']);

    TestBed.configureTestingModule({
      providers: [
        { provide: TokenStorageService, useValue: tokenStorageSpy },
        { provide: Router, useValue: routerSpy },
      ],
    });
  });

  it('deve ser criado', () => {
    expect(executeGuard).toBeTruthy();
  });

  it('deve retornar true se o usuário estiver autenticado e for ADMIN', () => {
    isAuthenticatedSignal.set(true);
    tokenStorageSpy.hasRole.mockReturnValue(true);

    const result = executeGuard(
      {} as ActivatedRouteSnapshot,
      {} as RouterStateSnapshot,
    );
    expect(result).toBe(true);
  });

  it('deve redirecionar para acesso-negado se autenticado mas não for ADMIN', () => {
    isAuthenticatedSignal.set(true);
    tokenStorageSpy.hasRole.mockReturnValue(false);
    const mockUrlTree = {} as UrlTree;
    routerSpy.parseUrl.mockReturnValue(mockUrlTree);

    const result = executeGuard(
      {} as ActivatedRouteSnapshot,
      {} as RouterStateSnapshot,
    );
    expect(result).toBe(mockUrlTree);
    expect(routerSpy.parseUrl).toHaveBeenCalledWith('/acesso-negado');
  });

  it('deve redirecionar para acesso-negado se não estiver autenticado', () => {
    isAuthenticatedSignal.set(false);
    routerSpy.parseUrl.mockReturnValue({} as UrlTree);
    executeGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot);
    expect(routerSpy.parseUrl).toHaveBeenCalledWith('/acesso-negado');
  });
});
