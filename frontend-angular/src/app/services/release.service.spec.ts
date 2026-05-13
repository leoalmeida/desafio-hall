import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { ReleaseService } from './release.service';
import { ReleaseType } from '../models/release-type';
import { NotificationService } from './notification.service';
import { environment } from '../../environments/environment';
import { firstValueFrom } from 'rxjs';

describe('ReleaseService', () => {
  let service: ReleaseService;
  let httpMock: HttpTestingController;
  let notificationSpy: {
    showSuccess: ReturnType<typeof vi.fn>;
    showError: ReturnType<typeof vi.fn>;
  };

  const mockRelease: ReleaseType = {
    id: 1,
    applicationId: 1,
    version: 'V1.0',
    env: 'DEV',
    status: 'CREATED',
    evidenceUrl: 'http://example.com/evidence',
  };

  beforeEach(() => {
    notificationSpy = { showSuccess: vi.fn(), showError: vi.fn() };

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        ReleaseService,
        { provide: NotificationService, useValue: notificationSpy },
      ],
    });

    service = TestBed.inject(ReleaseService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('deve ser criado', () => {
    expect(service).toBeTruthy();
  });

  it('deve buscar todos os releases (getAll) e atualizar a signal', () => {
    const mockList = [mockRelease];

    service.getAll();

    const req = httpMock.expectOne(environment.releasesApi);
    expect(req.request.method).toBe('GET');
    req.flush(mockList);

    expect(service.items()).toEqual(mockList);
    expect(notificationSpy.showSuccess).toHaveBeenCalled();
  });

  it('deve buscar todos os releases com retorno booleano (getAllAndReturn)', async () => {
    const resultPromise = firstValueFrom(service.getAllAndReturn());

    const req = httpMock.expectOne(environment.releasesApi);
    expect(req.request.method).toBe('GET');
    req.flush([mockRelease]);

    const result = await resultPromise;
    expect(result).toBe(true);
    expect(service.items().length).toBe(1);
  });

  it('deve criar um novo release (createOne)', async () => {
    const resultPromise = firstValueFrom(service.createOne(mockRelease));

    const req = httpMock.expectOne(environment.releasesApi);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(mockRelease);
    req.flush(mockRelease);

    const result = await resultPromise;
    expect(result).toBe(true);
    expect(service.items().length).toBe(1);
  });

  it('deve chamar endpoint approveRelease', () => {
    service.approveRelease(mockRelease.id!);

    const req = httpMock.expectOne(
      `${environment.releasesApi}/${mockRelease.id}/approve`,
    );
    expect(req.request.method).toBe('POST');
    req.flush(mockRelease);
  });

  it('deve chamar endpoint disapproveRelease', () => {
    service.disapproveRelease(mockRelease.id!);

    const req = httpMock.expectOne(
      `${environment.releasesApi}/${mockRelease.id}/disapprove`,
    );
    expect(req.request.method).toBe('POST');
    req.flush(mockRelease);
  });

  it('deve chamar endpoint promoteRelease', () => {
    service.promoteRelease(mockRelease.id!);

    const req = httpMock.expectOne(
      `${environment.releasesApi}/${mockRelease.id}/promote`,
    );
    expect(req.request.method).toBe('POST');
    req.flush(mockRelease);
  });
});
