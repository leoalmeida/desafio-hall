import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Toolbar } from './toolbar';
import { TokenStorageService } from '../../../services/token-storage.service';
import { BehaviorSubject } from 'rxjs';
import { provideRouter } from '@angular/router';
import { By } from '@angular/platform-browser';
import { TitleService } from '../../../services/title.service';
import { UserType } from 'src/app/models/user-type';
import { createSpyObj, SpyObj } from '../../../../test-helpers/spy-utils';

describe('Toolbar', () => {
  let component: Toolbar;
  let fixture: ComponentFixture<Toolbar>;
  let tokenStorageSpy: SpyObj<TokenStorageService>;
  let titleServiceSpy: SpyObj<TitleService>;
  let loggedUserSubject: BehaviorSubject<UserType>;
  let autenticadoSubject: BehaviorSubject<boolean>;
  let titleSubject: BehaviorSubject<string>;

  beforeEach(async () => {
    loggedUserSubject = new BehaviorSubject<UserType>({
      id: 1,
      nome: 'Usuario Teste',
      email: 'user@test.com',
      telefone: '11999990000',
      stats: [],
      logs: [],
    });
    autenticadoSubject = new BehaviorSubject<boolean>(false);
    titleSubject = new BehaviorSubject<string>('Frontend App');

    tokenStorageSpy = createSpyObj<TokenStorageService>([], {
      loggedUser$: loggedUserSubject.asObservable(),
      autenticado$: autenticadoSubject.asObservable(),
    } as Partial<TokenStorageService>);
    titleServiceSpy = createSpyObj<TitleService>([], {
      title$: titleSubject.asObservable(),
    } as Partial<TitleService>);

    await TestBed.configureTestingModule({
      imports: [Toolbar],
      providers: [
        { provide: TokenStorageService, useValue: tokenStorageSpy },
        { provide: TitleService, useValue: titleServiceSpy },
        provideRouter([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Toolbar);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('deve criar o componente', () => {
    expect(component).toBeTruthy();
  });

  it('deve exibir o título recebido do TitleService', () => {
    titleSubject.next('BIP App');
    fixture.detectChanges();

    const titleElement = fixture.debugElement.query(By.css('.head-title'))
      .nativeElement as HTMLElement;
    expect(titleElement.textContent).toContain('BIP App');
  });

  it('deve atualizar loggedUser quando o serviço emitir um novo usuário', () => {
    const novoUsuario: UserType = {
      id: 2,
      nome: 'Admin',
      email: 'admin@test.com',
      telefone: '11888887777',
      stats: [],
      logs: [],
    };
    loggedUserSubject.next(novoUsuario);

    expect((component as any).loggedUser().nome).toBe('Admin');
  });

  it('deve alternar o estado de "opened" ao interagir com o menu (simulação lógica)', () => {
    expect(component.opened).toBe(false);
    component.opened = !component.opened;
    expect(component.opened).toBe(true);
  });

  it('deve renderizar os links de navegação baseados nas rotas', () => {
    autenticadoSubject.next(true);
    fixture.detectChanges();

    const links = fixture.debugElement.queryAll(By.css('a'));
    expect(links.length).toBeGreaterThan(0);
  });

  it('deve exibir o nome do usuário logado no template', () => {
    autenticadoSubject.next(true);
    loggedUserSubject.next({
      id: 3,
      nome: 'Carlos Silva',
      email: 'carlos@test.com',
      telefone: '11777776666',
      stats: [],
      logs: [],
    });
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Carlos Silva');
  });
});
