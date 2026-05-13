import { Component, inject, Input, OnInit } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { LoadingService } from './loading.service';
import {
  RouteConfigLoadEnd,
  RouteConfigLoadStart,
  Router,
} from '@angular/router';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AsyncPipe } from '@angular/common';

@Component({
  selector: 'app-loading-indicator',
  templateUrl: './loading-indicator.html',
  styleUrls: ['./loading-indicator.css'],
  imports: [MatProgressSpinnerModule, AsyncPipe],
})
export class LoadingIndicator implements OnInit {
  @Input() detectRouteTransitions = false;
  loading$: Observable<boolean>;
  private loadingService: LoadingService = inject(LoadingService);
  private router: Router = inject(Router);

  constructor() {
    this.loading$ = this.loadingService.loading$;
  }

  ngOnInit() {
    if (this.detectRouteTransitions) {
      this.router.events
        .pipe(
          tap((event) => {
            if (event instanceof RouteConfigLoadStart) {
              this.loadingService.loadingOn();
            } else if (event instanceof RouteConfigLoadEnd) {
              this.loadingService.loadingOff();
            }
          }),
        )
        .subscribe();
    }
  }
}
