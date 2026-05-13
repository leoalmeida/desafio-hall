import { TestBed } from '@angular/core/testing';
import { PermissionService } from './permission.service';
import { TokenType } from '../models/token-type';

describe('PermissionService', () => {
  let service: PermissionService;

  const mockUser: TokenType = {
    id: 1,
    roles: ['ROLE_USER', 'ROLE_ADMIN'],
    permissions: ['READ', 'WRITE'],
    sub: 'joao@teste.com',
    username: 'joao',
    iat: 123456,
    exp: 789012,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [PermissionService],
    });
    service = TestBed.inject(PermissionService);
  });

  it('deve ser criado', () => {
    expect(service).toBeTruthy();
  });

  describe('canActivate', () => {
    it('deve retornar true se o ID do usuário coincidir e ele possuir a role', () => {
      const result = service.canActivate(mockUser, 1, 'ROLE_ADMIN');
      expect(result).toBe(true);
    });

    it('deve retornar false se o ID do usuário for diferente', () => {
      const result = service.canActivate(mockUser, 2, 'ROLE_ADMIN');
      expect(result).toBe(false);
    });

    it('deve retornar false se o usuário não possuir a role solicitada', () => {
      const result = service.canActivate(mockUser, 1, 'ROLE_MODERATOR');
      expect(result).toBe(false);
    });
  });

  describe('canMatch', () => {
    it('deve retornar true se o usuário possuir a role', () => {
      const result = service.canMatch(mockUser, 'ROLE_USER');
      expect(result).toBe(true);
    });

    it('deve retornar false se o usuário não possuir a role', () => {
      const result = service.canMatch(mockUser, 'ROLE_GUEST');
      expect(result).toBe(false);
    });

    it('deve lidar com lista de roles vazia', () => {
      const userWithoutRoles = { ...mockUser, roles: [] };
      const result = service.canMatch(userWithoutRoles, 'ROLE_USER');
      expect(result).toBe(false);
    });
  });
});
