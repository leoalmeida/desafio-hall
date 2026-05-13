import { MatButtonModule } from '@angular/material/button';
import { Component, inject, input } from '@angular/core';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { ApplicationType } from '../../../models/application-type';
import { MatCardModule } from '@angular/material/card';
import { MatBadgeModule } from '@angular/material/badge';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { ReleaseDetails } from '../../release-version/release-details/release-details';
import { ReleaseService } from 'src/app/services/release.service';

@Component({
  selector: 'app-application-card',
  imports: [
    MatCardModule,
    MatDividerModule,
    MatDialogModule,
    MatIconModule,
    MatButtonModule,
    MatBadgeModule,
  ],
  templateUrl: './application-card.html',
  styleUrl: './application-card.css',
})
export class ApplicationCard {
  application = input.required<ApplicationType>();

  private dialogAcao: MatDialog = inject(MatDialog);
  private releaseService: ReleaseService = inject(ReleaseService);

  onApplicationReleases(application: ApplicationType): void {
    const dialogRef = this.dialogAcao.open(ReleaseDetails, {
      width: '500px',
      data: { ...application },
    });

    dialogRef.afterClosed().subscribe(() => {
      this.releaseService.getAll();
    });
  }
}
