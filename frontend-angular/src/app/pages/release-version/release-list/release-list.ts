import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { NotificationService } from 'src/app/services/notification.service';
import { UserType } from 'src/app/models/user-type';
import { ReleaseService } from '../../../services/release.service';
import { LoadingService } from '../../../components/loading-indicator/loading.service';
import { TokenStorageService } from '../../../services/token-storage.service';
import { Searchbar } from '../../../components/searchbar/searchbar';
import { ReleaseDetails } from '../release-details/release-details';

@Component({
  selector: 'app-releases-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatSelectModule,
    MatDialogModule,
    MatPaginatorModule,
    Searchbar,
  ],
  templateUrl: './release-list.html',
  styleUrls: ['./release-list.css'],
})
export class ReleaseList {
  protected loggedUser = signal({} as UserType);
  searchQuery = signal<string>('');
  envFilter = signal<string>('');
  statusFilter = signal<string>('');
  pageIndex = signal(0);
  pageSize = signal(10);
  readonly pageSizeOptions = [10, 25, 50];

  readonly envOptions = ['DEV', 'PREPROD', 'PROD'];
  readonly statusOptions = [
    'CREATED',
    'PENDING_PREPROD',
    'PENDING_PROD',
    'APPROVED_PREPROD',
    'APPROVED_PROD',
    'REJECTED',
    'DEPLOYED',
  ];

  displayedColumns: string[] = [
    'id',
    'applicationId',
    'version',
    'env',
    'status',
    'createdAt',
    'deployedAt',
    'actions',
  ];

  get envFilterValue(): string { return this.envFilter(); }
  set envFilterValue(val: string) {
    this.envFilter.set(val);
    this.pageIndex.set(0);
  }

  get statusFilterValue(): string { return this.statusFilter(); }
  set statusFilterValue(val: string) {
    this.statusFilter.set(val);
    this.pageIndex.set(0);
  }

  private releaseService: ReleaseService = inject(ReleaseService);
  private loadingService: LoadingService = inject(LoadingService);
  private tokenStorageService: TokenStorageService = inject(TokenStorageService);
  private dialogAcao: MatDialog = inject(MatDialog);
  private notify: NotificationService = inject(NotificationService);

  constructor() {
    try {
      this.loadingService.loadingOn();
      this.tokenStorageService.loggedUser$.subscribe((user) => {
        this.loggedUser.set(user);
      });
      this.releaseService.getAll();
    } catch (error: any) {
      this.notify.showError(error.message || 'Erro ao carregar releases.');
    } finally {
      this.loadingService.loadingOff();
    }
  }

  filteredReleaseList = computed(() => {
    try {
      const normalizedQuery = this.searchQuery()
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .toLowerCase()
        .trim();
      const env = this.envFilter();
      const status = this.statusFilter();

      return this.releaseService.items().filter((item) => {
        if (env && item.env !== env) return false;
        if (status && item.status !== status) return false;

        if (!normalizedQuery) return true;

        const candidate = [
          item.id,
          item.applicationId,
          item.version,
          item.env,
          item.status,
          item.createdAt,
          item.deployedAt,
        ]
          .join(' ')
          .normalize('NFD')
          .replace(/[\u0300-\u036f]/g, '')
          .toLowerCase();

        return candidate.includes(normalizedQuery);
      });
    } catch (error: any) {
      this.notify.showError(error.message || 'Erro ao filtrar releases.');
      return [];
    }
  });

  paginatedReleaseList = computed(() => {
    const start = this.pageIndex() * this.pageSize();
    return this.filteredReleaseList().slice(start, start + this.pageSize());
  });

  filteredReleaseCount = computed(() => this.filteredReleaseList().length);

  get isApprover(): boolean {
    const role: string = this.loggedUser()?.userData?.role ?? "";
    return role === 'ROLE_APPROVER' || role === 'ROLE_ADMIN';
  }

  handleMessage(message: string): void {
    this.searchQuery.set(message);
    this.pageIndex.set(0);
  }

  handlePageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
  }

  clearFilters(): void {
    this.searchQuery.set('');
    this.envFilter.set('');
    this.statusFilter.set('');
    this.pageIndex.set(0);
  }

  onCreateRelease(): void {
    const dialogRef = this.dialogAcao.open(ReleaseDetails, {
      width: '500px',
      data: {},
    });
    dialogRef.afterClosed().subscribe(() => {
      this.releaseService.getAll();
    });
  }

  onApprove(id: number): void {
    this.releaseService.approveRelease(id);
  }

  onDisapprove(id: number): void {
    this.releaseService.disapproveRelease(id);
  }

  onPromote(id: number): void {
    this.releaseService.promoteRelease(id);
  }
}
