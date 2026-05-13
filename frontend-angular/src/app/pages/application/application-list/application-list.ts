import { MatButtonModule } from '@angular/material/button';
import { Component, computed, inject, signal } from '@angular/core';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { ApplicationService } from '../../../services/application.service';
import { LoadingService } from '../../../components/loading-indicator/loading.service';
import { TokenStorageService } from '../../../services/token-storage.service';
import { ApplicationCard } from '../application-card/application-card';
import { Searchbar } from '../../../components/searchbar/searchbar';
import { ApplicationDetails } from '../application-details/application-details';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { UserType } from 'src/app/models/user-type';
import { NotificationService } from 'src/app/services/notification.service';

@Component({
  selector: 'app-applications-list',
  standalone: true,
  imports: [
    ApplicationCard,
    Searchbar,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatPaginatorModule,
  ],
  templateUrl: './application-list.html',
  styleUrls: ['./application-list.css'],
})
export class ApplicationList {
  protected loggedUser = signal({} as UserType);
  searchQuery = signal<string>('');
  pageIndex = signal(0);
  pageSize = signal(10);
  readonly pageSizeOptions = [10, 25, 50];

  private applicationService: ApplicationService = inject(ApplicationService);
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
      this.applicationService.getAll();
    } catch (error: any) {
      this.notify.showError(error.message || 'Erro ao identificar usuário.');
    } finally {
      this.loadingService.loadingOff();
    }
  }

  filteredApplicationList = computed(() => {
    try {
      const normalizedQuery = this.searchQuery()
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .toLowerCase();
      return this.applicationService
        .items()
        .filter((x) => x?.name.toLowerCase().includes(normalizedQuery));
    } catch (error: any) {
      this.notify.showError(error.message || 'Erro ao filtrar aplicações.');
      return [];
    }
  });

  paginatedApplicationList = computed(() => {
    const start = this.pageIndex() * this.pageSize();
    return this.filteredApplicationList().slice(start, start + this.pageSize());
  });

  filteredApplicationCount = computed(() => this.filteredApplicationList().length);

  handleMessage(message: string): void {
    this.searchQuery.set(message);
    this.pageIndex.set(0);
  }

  handlePageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
  }

  onCreateApplication(): void {
    const dialogRef = this.dialogAcao.open(ApplicationDetails, {
      width: '500px',
      data: {},
    });
    // Mantem o fluxo de fechamento sem side effects de log.
    dialogRef.afterClosed().subscribe();
  }
}
