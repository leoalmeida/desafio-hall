import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ApplicationDetails } from './application-details';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ReactiveFormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ApplicationType } from '../../../models/application-type';
import { ApplicationService } from 'src/app/services/application.service';
import { of } from 'rxjs';
import { createSpyObj, SpyObj } from '../../../../test-helpers/spy-utils';

describe('ApplicationDetails', () => {
  let component: ApplicationDetails;
  let fixture: ComponentFixture<ApplicationDetails>;
  let dialogRefSpy: SpyObj<MatDialogRef<ApplicationDetails>, 'close'>;
  let applicationServiceSpy: SpyObj<ApplicationService, 'createOne'>;

  const mockApplication: ApplicationType = {
    id: 1,
    name: 'App Teste',
    ownerTeam: 'desenv',
    repoUrl: 'http://repo.example.com',
  };

  beforeEach(async () => {
    dialogRefSpy = createSpyObj<MatDialogRef<ApplicationDetails>>(['close']);
    applicationServiceSpy = createSpyObj<ApplicationService>([
      'createOne',
    ]);
    applicationServiceSpy.createOne.mockReturnValue(of(true));

    await TestBed.configureTestingModule({
      imports: [ApplicationDetails, ReactiveFormsModule, NoopAnimationsModule],
      providers: [
        { provide: MatDialogRef, useValue: dialogRefSpy },
        { provide: MAT_DIALOG_DATA, useValue: mockApplication },
        { provide: ApplicationService, useValue: applicationServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ApplicationDetails);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('deve criar o componente', () => {
    expect(component).toBeTruthy();
  });

  it('deve inicializar o formulário com os dados recebidos via MAT_DIALOG_DATA', () => {
    expect(component.formApplication.value).toEqual({
      name: mockApplication.name,
      ownerTeam: mockApplication.ownerTeam,
      repoUrl: mockApplication.repoUrl,
    });
  });

  it('deve invalidar o formulário se campos obrigatórios estiverem vazios', () => {
    component.formApplication.controls['name'].setValue('');
    component.formApplication.controls['repoUrl'].setValue('');

    expect(component.formApplication.valid).toBe(false);
  });

  it('deve fechar o diálogo com true ao chamar onSubmit se válido', () => {
    const updatedValue = {
      name: 'Novo Nome',
      ownerTeam: 'nova-equipe',
      repoUrl: 'http://novo-repo.com',
    };
    component.formApplication.patchValue(updatedValue);

    component.onSubmit();

    expect(applicationServiceSpy.createOne).toHaveBeenCalled();
  });

  it('não deve fechar o diálogo ao chamar onSubmit se o formulário for inválido', () => {
    component.formApplication.controls['name'].setValue('');
    component.onSubmit();
    expect(dialogRefSpy.close).not.toHaveBeenCalled();
  });

  it('deve fechar o diálogo sem dados ao chamar onCancel', () => {
    component.onCancel();
    expect(dialogRefSpy.close).toHaveBeenCalledWith();
  });
});
