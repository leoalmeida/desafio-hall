import { inject, Injectable } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { filter, map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class TitleService {
  private title = new BehaviorSubject<string>('Frontend App');
  title$ = this.title.asObservable();
  private titleBrowser: Title = inject(Title);
  private router: Router = inject(Router);
  private activatedRoute: ActivatedRoute = inject(ActivatedRoute);
  constructor() {}
  setTitle() {
    this.router.events
      .pipe(
        filter((event) => event instanceof NavigationEnd),
        map(() => {
          let route = this.activatedRoute;
          while (route.firstChild) {
            route = route.firstChild;
          }
          return route;
        }),
        filter((route) => route.outlet === 'primary'),
        map((route) => {
          const title = route.snapshot.data['title'];
          const detail = route.snapshot.data['detail'];
          const type = route.snapshot.queryParams['type'];
          if (!title) {
            return 'Frontend App';
          }

          if (detail) return (type !== 'new' ? 'Criar ' : 'Alterar ') + title;
          return title;
        }),
      )
      .subscribe((title) => {
        this.titleBrowser.setTitle(title);
        this.title.next(title);
      });
  }
}
