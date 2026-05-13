import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { ApplicationService } from './application.service';
import { ApplicationType } from '../models/application-type';
import { NotificationService } from './notification.service';
import { environment } from '../../environments/environment';
import { firstValueFrom } from 'rxjs';

describe('ApplicationService', () => {
  let service: ApplicationService;
  let httpMock: HttpTestingController;
  let notificationSpy: {
    showSuccess: ReturnType<typeof vi.fn>;
    showError: ReturnType<typeof vi.fn>;
  };

  const mockApplication: ApplicationType = {
    id: 1,
    name: 'App Teste',
    ownerTeam: 'desenv',
    repoUrl: 'http://repo.example.com',
  };

  beforeEach(() => {
    notificationSpy = { showSuccess: vi.fn(), showError: vi.fn() };

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        ApplicationService,
        { provide: NotificationService, useValue: notificationSpy },
      ],
    });

    service = TestBed.inject(ApplicationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('deve ser criado', () => {
    expect(service).toBeTruthy();
  });

  it('deve buscar todas as aplicações (getAll) e atualizar a signal', () => {
    const mockList = [mockApplication];

    service.getAll();

    const req = httpMock.expectOne(environment.applicationsApi);
    expect(req.request.method).toBe('GET');
    req.flush(mockList);

    expect(service.items()).toEqual(mockList);
    expect(notificationSpy.showSuccess).toHaveBeenCalled();
  });

  it('deve buscar todas as aplicações com retorno booleano (getAllAndReturn)', async () => {
    const resultPromise = firstValueFrom(service.getAllAndReturn());

    const req = httpMock.expectOne(environment.applicationsApi);
    expect(req.request.method).toBe('GET');
    req.flush([mockApplication]);

    const result = await resultPromise;
    expect(result).toBe(true);
    expect(service.items().length).toBe(1);
  });

  it('deve criar uma nova aplicação (createOne)', async () => {
    const resultPromise = firstValueFrom(service.createOne(mockApplication));

    const req = httpMock.expectOne(environment.applicationsApi);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(mockApplication);
    req.flush(mockApplication);

    const result = await resultPromise;
    expect(result).toBe(true);
    expect(service.items().length).toBe(1);
  });

  it('deve propagar erro em getAll com falha HTTP', async () => {
    service.getAll();

    const req = httpMock.expectOne(environment.applicationsApi);
    req.flush(
      { message: 'Erro interno' },
      { status: 500, statusText: 'Server Error' },
    );

    expect(notificationSpy.showError).toHaveBeenCalled();
  });
});
