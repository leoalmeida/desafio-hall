import {
  ApplicationConfig,
  inject,
  provideBrowserGlobalErrorListeners,
  provideZoneChangeDetection,
} from '@angular/core';
import {
  HttpInterceptorFn,
  provideHttpClient,
  withInterceptors,
} from '@angular/common/http';
import {
  provideClientHydration,
  withIncrementalHydration,
} from '@angular/platform-browser';
import { provideRouter, withViewTransitions } from '@angular/router';
import { routes } from './app.routes';
import { environment } from '../environments/environment';
import { catchError, throwError } from 'rxjs';
import { TokenStorageService } from './services/token-storage.service';

export const appInterceptor: HttpInterceptorFn = (req, next) => {
  if (!environment.production) {
    console.log('Request made with URL:', req.url);
  }
  const tokenService = inject(TokenStorageService);
  const token = tokenService.getUser().token;    
  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    });
  }
  
  return next(req);
};

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((error) => {
      console.error('HTTP Error:', error);
      // Here you can add global error handling logic, e.g., show a notification
      return throwError(() => error);
    }),
  );
};

export const appConfig: ApplicationConfig = {
  providers: [
    provideHttpClient(
      withInterceptors([appInterceptor, errorInterceptor]), // loggingInterceptor, cachingInterceptor
    ),
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes, withViewTransitions()),
    provideClientHydration(withIncrementalHydration()),
  ],
};
