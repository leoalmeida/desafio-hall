import {
  ApplicationConfig,
  inject,
  provideBrowserGlobalErrorListeners,
  provideZoneChangeDetection,
} from '@angular/core';
import {
  HttpInterceptorFn,
  HttpErrorResponse,
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
import { catchError, retry, throwError, timer } from 'rxjs';
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
    retry({
      count: isRetryableMethod(req.method) ? 2 : 0,
      delay: (error, retryCount) => {
        if (!isRetryableError(error)) {
          throw error;
        }

        return timer(300 * retryCount);
      },
    }),
    catchError((error) => {
      console.error('HTTP Error:', error);
      return throwError(() => error);
    }),
  );
};

function isRetryableMethod(method: string): boolean {
  return ['GET', 'HEAD', 'OPTIONS'].includes(method.toUpperCase());
}

function isRetryableError(error: unknown): boolean {
  if (!(error instanceof HttpErrorResponse)) {
    return false;
  }

  return error.status === 0 || error.status === 408 || error.status === 429 || error.status >= 500;
}

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
