import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReleaseDetails } from './release-details';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ReactiveFormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ApplicationType } from '../../../models/application-type';
import { ReleaseService } from '../../../services/release.service';
import { ApplicationService } from '../../../services/application.service';
import { NotificationService } from '../../../services/notification.service';
import { createSpyObj, SpyObj } from '../../../../test-helpers/spy-utils';

describe('ReleaseDetails', () => {
  let component: ReleaseDetails;
  let fixture: ComponentFixture<ReleaseDetails>;
  let dialogRefSpy: SpyObj<MatDialogRef<ReleaseDetails>, 'close'>;
  let releaseServiceSpy: SpyObj<ReleaseService, 'getAll' | 'getAllAndReturn' | 'searchReleases' | 'createOne' | 'approveRelease' | 'disapproveRelease' | 'promoteRelease'>;
  let applicationServiceSpy: SpyObj<ApplicationService, 'getAll'>;
  let notificationServiceSpy: SpyObj<
    NotificationService,
    'showSuccess' | 'showError'
  >;

  const mockApplication: ApplicationType = {
    id: '00000000-0000-0000-0000-000000000001',
    name: 'Application Teste',
    ownerTeam: 'desenv',
    repoUrl: '',
    createdAt: '2026-01-01T00:00:00Z',
  };

  beforeEach(async () => {
    dialogRefSpy = createSpyObj<MatDialogRef<ReleaseDetails>>(['close']);
    releaseServiceSpy = createSpyObj<ReleaseService>([
      'getAll',
      'getAllAndReturn',
      'searchReleases',
      'createOne',
      'approveRelease',
      'disapproveRelease',
      'promoteRelease',
    ] as const);
    applicationServiceSpy = createSpyObj<ApplicationService>(['getAll']);
    notificationServiceSpy = createSpyObj<NotificationService>([
      'showSuccess',
      'showError',
    ]);

    await TestBed.configureTestingModule({
      imports: [ReleaseDetails, ReactiveFormsModule, NoopAnimationsModule],
      providers: [
        { provide: MatDialogRef, useValue: dialogRefSpy },
        { provide: MAT_DIALOG_DATA, useValue: mockApplication },
        { provide: ReleaseService, useValue: releaseServiceSpy },
        { provide: ApplicationService, useValue: applicationServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ReleaseDetails);
    component = fixture.componentInstance;
  });

  it('deve criar o componente', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('deve fechar o diálogo ao chamar onSubmit se válido', () => {
    fixture.detectChanges();
    const releaseData = {
      applicationId: '00000000-0000-0000-0000-000000000001',
      version: '1.0.0',
      env: 'DEV' as const,
      status: 'CREATED' as const,
      evidenceUrl: 'http://example.com/evidence',
    };
    component.formRelease.patchValue(releaseData);

    component.onSubmit();

    expect(releaseServiceSpy.createOne).toHaveBeenCalledWith(
      expect.objectContaining(releaseData),
    );
    expect(dialogRefSpy.close).toHaveBeenCalledWith();
  });

  it('não deve fechar o diálogo ao chamar onSubmit se o formulário for inválido', () => {
    fixture.detectChanges();
    component.formRelease.controls['applicationId'].setValue('');

    component.onSubmit();

    expect(dialogRefSpy.close).not.toHaveBeenCalled();
  });
  
  it('deve fechar o diálogo sem dados ao chamar onCancel', () => {
    fixture.detectChanges();
    component.onCancel();
    expect(dialogRefSpy.close).toHaveBeenCalledWith();
  });
});
