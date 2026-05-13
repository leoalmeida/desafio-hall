import { Inject, LOCALE_ID, Pipe, PipeTransform } from '@angular/core';
import { DatePipe } from '@angular/common';

@Pipe({
  name: 'dateFormatPipe',
  standalone: true,
})
export class DateFormatPipe implements PipeTransform {
  constructor(@Inject(LOCALE_ID) private locale: string) {}
  transform(value: string) {
    try {
      const datePipe = new DatePipe(this.locale);
      return datePipe.transform(value, 'dd/MM/yyyy HH:mm:ss') || '';
    } catch {
      return '';
    }
  }
}
