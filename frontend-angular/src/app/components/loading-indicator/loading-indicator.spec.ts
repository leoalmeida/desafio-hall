import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoadingIndicator } from './loading-indicator';
import { LoadingService } from './loading.service';
import {
  Router,
  RouteConfigLoadStart,
  RouteConfigLoadEnd,
} from '@angular/router';
import { BehaviorSubject, Subject } from 'rxjs';
import { By } from '@angular/platform-browser';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { createSpyObj, SpyObj } from '../../../../test-helpers/spy-utils';

describe('LoadingIndicator', () => {
  let component: LoadingIndicator;
  let fixture: ComponentFixture<LoadingIndicator>;
  let loadingServiceSpy: SpyObj<LoadingService>;
  let routerEventsSubject: Subject<any>;
  let loadingSubject: BehaviorSubject<boolean>;

  beforeEach(async () => {
    loadingSubject = new BehaviorSubject<boolean>(false);
    loadingServiceSpy = createSpyObj<LoadingService>(
      ['loadingOn', 'loadingOff'],
      { loading$: loadingSubject.asObservable() } as Partial<LoadingService>,
    );
    routerEventsSubject = new Subject<any>();
    const routerSpy = {
      events: routerEventsSubject.asObservable(),
    };

    await TestBed.configureTestingModule({
      imports: [LoadingIndicator, NoopAnimationsModule],
      providers: [
        { provide: LoadingService, useValue: loadingServiceSpy },
        { provide: Router, useValue: routerSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LoadingIndicator);
    component = fixture.componentInstance;
  });

  it('deve criar o componente', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('deve exibir o spinner quando loading$ for true', () => {
    loadingSubject.next(true);
    fixture.detectChanges();

    const spinner = fixture.debugElement.query(By.css('mat-spinner'));
    expect(spinner).toBeTruthy();
  });

  it('não deve exibir o spinner quando loading$ for false', () => {
    loadingSubject.next(false);
    fixture.detectChanges();

    const spinner = fixture.debugElement.query(By.css('mat-spinner'));
    expect(spinner).toBeFalsy();
  });

  it('deve reagir a eventos de rota quando detectRouteTransitions for true', () => {
    component.detectRouteTransitions = true;
    component.ngOnInit();

    routerEventsSubject.next(new RouteConfigLoadStart({} as any));
    expect(loadingServiceSpy.loadingOn).toHaveBeenCalled();

    routerEventsSubject.next(new RouteConfigLoadEnd({} as any));
    expect(loadingServiceSpy.loadingOff).toHaveBeenCalled();
  });

  it('não deve reagir a eventos de rota quando detectRouteTransitions for false', () => {
    component.detectRouteTransitions = false;
    component.ngOnInit();

    routerEventsSubject.next(new RouteConfigLoadStart({} as any));
    expect(loadingServiceSpy.loadingOn).not.toHaveBeenCalled();

    routerEventsSubject.next(new RouteConfigLoadEnd({} as any));
    expect(loadingServiceSpy.loadingOff).not.toHaveBeenCalled();
  });
});
