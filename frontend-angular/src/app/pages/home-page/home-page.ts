import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { TitleService } from '../../services/title.service';

@Component({
  selector: 'app-home-page',
  standalone: true,
  imports: [CommonModule, MatCardModule],
  templateUrl: './home-page.html',
  styleUrls: ['./home-page.css'],
})
export class HomePage implements OnInit {
  protected readonly title = signal('');
  private titleService: TitleService = inject(TitleService);

  ngOnInit(): void {
    this.titleService.setTitle();
  }
}
