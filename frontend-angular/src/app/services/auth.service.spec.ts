import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { TokenStorageService } from './token-storage.service';
import { firstValueFrom } from 'rxjs';
import { createSpyObj, SpyObj } from '../../test-helpers/spy-utils';
import { environment } from '../../environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let tokenStorageSpy: SpyObj<TokenStorageService>;

  beforeEach(() => {
    const spy = createSpyObj<TokenStorageService>([
      'saveJsonWebToken',
      'signOut',
    ]);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService, { provide: TokenStorageService, useValue: spy }],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    tokenStorageSpy = TestBed.inject(
      TokenStorageService,
    ) as unknown as typeof tokenStorageSpy;
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('deve ser criado', () => {
    expect(service).toBeTruthy();
  });

  it('deve realizar login e salvar o token', () => {
    service.login('jrrtolk', '123');

    const req = httpMock.expectOne(environment.authApi);
    expect(req.request.method).toBe('POST');
    req.flush({ id: 1, email: 'jrrtolk', nome: 'Test', accessToken: 'tok' });

    expect(tokenStorageSpy.saveJsonWebToken).toHaveBeenCalled();
  });

  it('deve realizar logout e limpar o armazenamento', () => {
    service.logout();

    const req = httpMock.expectOne(`${environment.authApi}/signout`);
    req.flush({});

    expect(tokenStorageSpy.signOut).toHaveBeenCalled();
  });

  it('deve emitir o usuário logado através do observable loggedUser$', async () => {
    const user$ = service.login('jrrtolk', '123');

    const req = httpMock.expectOne(environment.authApi);
    req.flush({ id: 1, email: 'jrrtolk', nome: 'Test', accessToken: 'tok' });

    const user = await firstValueFrom(user$);
    expect(user).toBeDefined();
  });
});
