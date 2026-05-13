import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ApplicationList } from './application-list';
import { ApplicationService } from '../../../services/application.service';
import { LoadingService } from '../../core/loading-indicator/loading.service';
import { TokenStorageService } from '../../../services/token-storage.service';
import { MatDialog } from '@angular/material/dialog';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of } from 'rxjs';
import { signal } from '@angular/core';
import { ApplicationType } from '../../../models/application-type';
import { UserType } from '../../../models/user-type';
import { createSpyObj, SpyObj } from '../../../../test-helpers/spy-utils';

describe('ApplicationList', () => {
  let component: ApplicationList;
  let fixture: ComponentFixture<ApplicationList>;
  let applicationServiceSpy: SpyObj<ApplicationService>;
  let loadingServiceSpy: SpyObj<LoadingService>;
  let tokenStorageServiceSpy: SpyObj<TokenStorageService>;
  let dialogSpy: SpyObj<MatDialog>;

  const mockApplications: ApplicationType[] = [
    { id: 1, name: 'App teste', ownerTeam: 'desenv', repoUrl: 'http://repo1' },
    { id: 2, name: 'Plano de Saúde', ownerTeam: 'ti', repoUrl: 'http://repo2' },
  ];

  const mockUser: UserType = {
    id: 1,
    email: 'user@test.com',
    nome: 'Usuario Teste',
    telefone: '11999990000',
  };

  beforeEach(async () => {
    applicationServiceSpy = createSpyObj<ApplicationService>(['getAll'], {
      items: signal(mockApplications),
    } as Partial<ApplicationService>);
    loadingServiceSpy = createSpyObj<LoadingService>([
      'loadingOn',
      'loadingOff',
    ]);
    tokenStorageServiceSpy = createSpyObj<TokenStorageService>([], {
      loggedUser$: of(mockUser),
    } as Partial<TokenStorageService>);
    dialogSpy = createSpyObj<MatDialog>(['open']);

    await TestBed.configureTestingModule({
      imports: [ApplicationList, NoopAnimationsModule],
      providers: [
        { provide: ApplicationService, useValue: applicationServiceSpy },
        { provide: LoadingService, useValue: loadingServiceSpy },
        { provide: TokenStorageService, useValue: tokenStorageServiceSpy },
        { provide: MatDialog, useValue: dialogSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ApplicationList);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('deve criar o componente e carregar dados iniciais', () => {
    expect(component).toBeTruthy();
    expect(applicationServiceSpy.getAll).toHaveBeenCalled();
    expect(loadingServiceSpy.loadingOn).toHaveBeenCalled();
    expect(loadingServiceSpy.loadingOff).toHaveBeenCalled();
  });

  it('deve filtrar a lista de aplicações com base na searchQuery', () => {
    component.searchQuery.set('vale');
    fixture.detectChanges();

    const filtered = component.filteredApplicationList();
    expect(filtered?.length).toBe(1);
    expect(filtered![0].name).toBe('App teste');
  });

  it('deve filtrar sem diferenciar maiúsculas e minúsculas', () => {
    component.searchQuery.set('PLANO');
    fixture.detectChanges();

    const filtered = component.filteredApplicationList();
    expect(filtered?.length).toBe(1);
    expect(filtered![0].name).toBe('Plano de Saúde');
  });

  it('deve atualizar searchQuery ao chamar handleMessage', () => {
    const query = 'nova busca';
    component.handleMessage(query);
    expect(component.searchQuery()).toBe(query);
  });
});
