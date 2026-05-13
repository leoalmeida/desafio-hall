import { Component, inject, OnInit, signal } from '@angular/core';
import { TitleService } from './services/title.service';
import { LoadingIndicator } from './components/loading-indicator/loading-indicator';
import { RouterOutlet } from '@angular/router';
import { Toolbar } from './components/toolbar/toolbar';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [LoadingIndicator, Toolbar, RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})
export class AppComponent implements OnInit {
  protected readonly title = signal('');

  private titleService: TitleService = inject(TitleService);

  ngOnInit(): void {
    this.titleService.setTitle();
  }

  updateViewByRole(): void {}
}
