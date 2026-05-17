import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginPage } from './login-page';
import { AuthService } from '../../services/auth.service';
import { TokenStorageService } from '../../services/token-storage.service';
import { Router } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of } from 'rxjs';
import { createSpyObj, SpyObj } from '../../../test-helpers/spy-utils';

describe('LoginPage', () => {
  let component: LoginPage;
  let fixture: ComponentFixture<LoginPage>;
  let authServiceSpy: SpyObj<AuthService, 'login'>;
  let tokenStorageSpy: SpyObj<TokenStorageService, 'isAuthenticated'>;
  let routerSpy: SpyObj<Router, 'navigate'>;

  beforeEach(async () => {
    authServiceSpy = createSpyObj<AuthService>(['login']);
    tokenStorageSpy = createSpyObj<TokenStorageService>(['isAuthenticated']);
    routerSpy = createSpyObj<Router>(['navigate']);

    tokenStorageSpy.isAuthenticated.mockReturnValue(false);

    await TestBed.configureTestingModule({
      imports: [LoginPage, ReactiveFormsModule, NoopAnimationsModule],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: TokenStorageService, useValue: tokenStorageSpy },
        { provide: Router, useValue: routerSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LoginPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('deve criar o componente', () => {
    expect(component).toBeTruthy();
  });

  it('deve inicializar o formulário vazio e inválido', () => {
    expect(component.formLogin.valid).toBe(false);
    expect(component.formLogin.value).toEqual({ username: '', password: '' });
  });

  it('deve validar campos obrigatórios', () => {
    const usernameControl = component.formLogin.get('username');
    const passwordControl = component.formLogin.get('password');

    usernameControl?.setValue('');
    passwordControl?.setValue('');

    expect(usernameControl?.errors?.['required']).toBeTruthy();
    expect(passwordControl?.errors?.['required']).toBeTruthy();
  });

  it('deve chamar o serviço de login e navegar se bem-sucedido', () => {
    const mockUser = {
      email: 'teste@example.com',
      name: 'Teste',
      role: 'ROLE_VIEWER',
    };
    authServiceSpy.login.mockReturnValue(of(mockUser as any));

    component.formLogin.setValue({ username: 'user', password: '123' });
    component.onSubmit();

    expect(authServiceSpy.login).toHaveBeenCalledWith('user', '123');
  });

  it('não deve chamar o serviço de login se o formulário for inválido', () => {
    component.formLogin.setValue({ username: '', password: '' });
    component.onSubmit();
    expect(authServiceSpy.login).not.toHaveBeenCalled();
  });

  it('deve redirecionar no ngOnInit se já estiver autenticado', () => {
    tokenStorageSpy.isAuthenticated.mockReturnValue(true);
    component.ngOnInit();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['']);
  });

  it('deve atualizar isLoggedIn no reloadPage quando não autenticado', () => {
    tokenStorageSpy.isAuthenticated.mockReturnValue(false);

    component.reloadPage();

    expect(component.isLoggedIn()).toBe(false);
    expect(routerSpy.navigate).not.toHaveBeenCalled();
  });
});
