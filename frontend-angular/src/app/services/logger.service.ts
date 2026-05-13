import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class LoggerService {
  log(...msgs: unknown[]) {
    void msgs;
  }
  error(...msgs: unknown[]) {
    void msgs;
  }
  warn(...msgs: unknown[]) {
    void msgs;
  }
}
