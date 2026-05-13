import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { UserType } from 'src/app/models/user-type';
import { NotificationService } from 'src/app/services/notification.service';
import { TitleService } from 'src/app/services/title.service';
import { TokenStorageService } from 'src/app/services/token-storage.service';
import { AuthService } from 'src/app/services/auth.service';
import { ApprovalList } from '../approval-list/approval-list';
import { AuditlogList } from '../auditlog-list/auditlog-list';

type AdminUserRow = UserType & {
  id: number;
  nome: string;
};

@Component({
  selector: 'app-admin-board',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatSnackBarModule,
    MatTabsModule,
    ApprovalList,
    AuditlogList,
  ],
  templateUrl: './admin-board.html',
  styleUrls: ['./admin-board.css'],
})
export class AdminBoard implements OnInit {
  protected loggedUser = signal({} as UserType);
  protected readonly title = signal('');
  private titleService: TitleService = inject(TitleService);

  displayedColumns: string[] = ['id', 'name', 'actions'];

  private notify: NotificationService = inject(NotificationService);
  private tokenStorageService: TokenStorageService =
    inject(TokenStorageService);
  private authService: AuthService = inject(AuthService);
  dataSource = new MatTableDataSource<AdminUserRow>([]);

  constructor() {
    try {
      this.tokenStorageService.loggedUser$.subscribe((user) => {
        this.loggedUser.set(user);
      });
    } catch (error: any) {
      this.notify.showError(error.message || 'Erro ao identificar usuário');
    }
  }

  ngOnInit(): void {
    this.titleService.setTitle();
  }

  removeUser(id: number) {
    this.dataSource.data = this.dataSource.data.filter(
      (user) => user.id !== id,
    );

    this.notify.showSuccess('Usuário removido com sucesso!', {
      duration: 3000,
    });
  }
}
