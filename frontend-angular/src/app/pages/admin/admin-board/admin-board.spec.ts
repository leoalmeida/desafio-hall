import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { signal } from '@angular/core';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

import { AdminBoard } from './admin-board';
import { NotificationService } from 'src/app/services/notification.service';
import { TitleService } from 'src/app/services/title.service';
import { TokenStorageService } from 'src/app/services/token-storage.service';
import { AuthService } from 'src/app/services/auth.service';
import { ApprovalService } from 'src/app/services/approval.service';
import { AuditlogService } from 'src/app/services/auditlog.service';
import { LoadingService } from '../../../components/loading-indicator/loading.service';

describe('AdminBoard', () => {
  let component: AdminBoard;
  let fixture: ComponentFixture<AdminBoard>;
  let titleServiceSpy: { setTitle: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    titleServiceSpy = { setTitle: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [AdminBoard, NoopAnimationsModule],
      providers: [
        {
          provide: TokenStorageService,
          useValue: { loggedUser$: of({ email: 'admin@test.com', name: 'Admin', role: 'ADMIN' }) },
        },
        { provide: TitleService, useValue: titleServiceSpy },
        { provide: AuthService, useValue: {} },
        {
          provide: NotificationService,
          useValue: { showSuccess: vi.fn(), showError: vi.fn() },
        },
        {
          provide: ApprovalService,
          useValue: { getAll: vi.fn(), items: signal([]) },
        },
        {
          provide: AuditlogService,
          useValue: { getAll: vi.fn(), items: signal([]) },
        },
        {
          provide: LoadingService,
          useValue: { loadingOn: vi.fn(), loadingOff: vi.fn() },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminBoard);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('deve criar o componente', () => {
    expect(component).toBeTruthy();
  });

  it('deve chamar setTitle no ngOnInit', () => {
    expect(titleServiceSpy.setTitle).toHaveBeenCalled();
  });

  it('deve remover usuário da tabela ao chamar removeUser', () => {
    const notifyMock = TestBed.inject(NotificationService);
    (component as any).dataSource.data = [
      { id: 1, nome: 'Alice', email: 'a@a.com', role: 'ADMIN', token: {} },
      { id: 2, nome: 'Bob', email: 'b@b.com', role: 'USER', token: {} },
    ];

    component.removeUser(1);

    expect((component as any).dataSource.data.length).toBe(1);
    expect((component as any).dataSource.data[0].id).toBe(2);
    expect(notifyMock.showSuccess).toHaveBeenCalled();
  });

  it('deve ter três abas definidas no template', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const tabs = compiled.querySelectorAll('.mat-mdc-tab');
    expect(tabs.length).toBe(3);
  });
});
