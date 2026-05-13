import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ApplicationCard } from './application-card';
import { MatDialogRef } from '@angular/material/dialog';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ApplicationType } from '../../../models/application-type';
import { ReleaseService } from '../../../services/release.service';
import { createSpyObj, SpyObj } from '../../../../test-helpers/spy-utils';
import { of } from 'rxjs';
import { Mock } from 'vitest';

describe('ApplicationCard', () => {
  let component: ApplicationCard;
  let fixture: ComponentFixture<ApplicationCard>;
  let releaseServiceSpy: SpyObj<ReleaseService, 'getAll'>;
  let openSpy: Mock;

  const mockApplication: ApplicationType = {
    id: 1,
    name: 'Application Teste',
    ownerTeam: 'desenv',
    repoUrl: 'http://repo.example.com',
  };

  beforeEach(async () => {
    releaseServiceSpy = createSpyObj<ReleaseService>(['getAll']);

    await TestBed.configureTestingModule({
      imports: [ApplicationCard, NoopAnimationsModule],
      providers: [
        { provide: ReleaseService, useValue: releaseServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ApplicationCard);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('application', mockApplication);

    // Spy on the component's injected MatDialog instance (from MatDialogModule inside component's imports)
    const dialogRefSpy = createSpyObj<MatDialogRef<any>>(['afterClosed']);
    dialogRefSpy.afterClosed.mockReturnValue(of(undefined));
    openSpy = vi.spyOn((component as any).dialogAcao, 'open').mockReturnValue(dialogRefSpy as any) as unknown as Mock;

    fixture.detectChanges();
  });

  it('deve criar o componente', () => {
    expect(component).toBeTruthy();
  });

  it('deve abrir o diálogo de releases ao chamar onApplicationReleases', () => {
    component.onApplicationReleases(mockApplication);
    expect(openSpy).toHaveBeenCalled();
  });

  it('deve atualizar a lista de releases após fechar o diálogo', () => {
    component.onApplicationReleases(mockApplication);
    expect(releaseServiceSpy.getAll).toHaveBeenCalled();
  });
});
