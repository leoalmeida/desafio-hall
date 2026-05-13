import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReleaseList } from './release-list';
import { ReleaseService } from '../../../services/release.service';
import { LoadingService } from '../../core/loading-indicator/loading.service';
import { TokenStorageService } from '../../../services/token-storage.service';
import { MatDialog } from '@angular/material/dialog';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of } from 'rxjs';
import { signal } from '@angular/core';
import { ReleaseType } from '../../../models/release-type';
import { UserType } from '../../../models/user-type';
import { createSpyObj, SpyObj } from '../../../../test-helpers/spy-utils';
import { TokenType, TokenValueType } from 'src/app/models/token-type';

describe('ReleaseList', () => {
  let component: ReleaseList;
  let fixture: ComponentFixture<ReleaseList>;
  let releaseServiceSpy: SpyObj<ReleaseService, 'getAll' | 'getAllAndReturn' | 'searchReleases' | 'createOne' | 'approveRelease' | 'disapproveRelease' | 'promoteRelease'>;
  let loadingServiceSpy: SpyObj<LoadingService>;
  let tokenStorageServiceSpy: SpyObj<TokenStorageService>;
  let dialogSpy: SpyObj<MatDialog>;

  const mockReleases: ReleaseType[] = [
    { id: 1, applicationId: 1, version: 'V1.0', env: 'PROD', status: 'APPROVED_PROD', evidenceUrl: '' },
    { id: 2, applicationId: 1, version: 'V1.1', env: 'PROD', status: 'PENDING_PREPROD', evidenceUrl: '' },
    { id: 3, applicationId: 2, version: 'V2.0', env: 'DEV', status: 'CREATED', evidenceUrl: '' },
  ];

  const mockUser: UserType = {
    email: 'user@test.com',
    name: 'Usuario Teste',
    role: 'VIEWER',
    token: {} as TokenType,
    userData: {} as TokenValueType
  };

  beforeEach(async () => {
    releaseServiceSpy = createSpyObj<ReleaseService>(['getAll'], {
      items: signal(mockReleases),
    } as Partial<ReleaseService>);
    loadingServiceSpy = createSpyObj<LoadingService>([
      'loadingOn',
      'loadingOff',
    ]);
    tokenStorageServiceSpy = createSpyObj<TokenStorageService>([], {
      loggedUser$: of(mockUser),
    } as Partial<TokenStorageService>);
    dialogSpy = createSpyObj<MatDialog>(['open']);

    await TestBed.configureTestingModule({
      imports: [ReleaseList, NoopAnimationsModule],
      providers: [
        { provide: ReleaseService, useValue: releaseServiceSpy },
        { provide: LoadingService, useValue: loadingServiceSpy },
        { provide: TokenStorageService, useValue: tokenStorageServiceSpy },
        { provide: MatDialog, useValue: dialogSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ReleaseList);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('deve criar o componente e carregar dados iniciais', () => {
    expect(component).toBeTruthy();
    expect(releaseServiceSpy.getAll).toHaveBeenCalled();
    expect(loadingServiceSpy.loadingOn).toHaveBeenCalled();
    expect(loadingServiceSpy.loadingOff).toHaveBeenCalled();
  });

  it('deve filtrar a lista de releases com base na searchQuery', () => {
    component.searchQuery.set('V1.0');
    fixture.detectChanges();

    const filtered = component.filteredReleaseList();
    expect(filtered?.length).toBe(1);
    expect(filtered![0].version).toBe('V1.0');
  });

  it('deve filtrar sem diferenciar maiúsculas e minúsculas', () => {
    component.searchQuery.set('created');
    fixture.detectChanges();

    const filtered = component.filteredReleaseList();
    expect(filtered?.length).toBe(1);
    expect(filtered![0].status).toBe('CREATED');
  });

  it('deve atualizar searchQuery ao chamar handleMessage', () => {
    const query = 'nova busca';
    component.handleMessage(query);
    expect(component.searchQuery()).toBe(query);
  });
});
