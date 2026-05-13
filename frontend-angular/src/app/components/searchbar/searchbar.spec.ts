import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';

import { Searchbar } from './searchbar';

describe('Searchbar', () => {
  let component: Searchbar;
  let fixture: ComponentFixture<Searchbar>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Searchbar],
    }).compileComponents();

    fixture = TestBed.createComponent(Searchbar);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('deve criar o componente', () => {
    expect(component).toBeTruthy();
  });

  it('deve emitir uma string vazia ao chamar clear()', () => {
    const spy = vi.fn();
    component.messageEvent.subscribe(spy);

    component.clear();

    expect(spy).toHaveBeenCalledWith('');
  });

  it('deve emitir o termo de busca ao chamar onSearchUpdated()', () => {
    const spy = vi.fn();
    const searchTerm = 'App teste';
    component.messageEvent.subscribe(spy);

    component.onSearchUpdated(searchTerm);

    expect(spy).toHaveBeenCalledWith(searchTerm);
  });

  it('deve conter um elemento de input no template', () => {
    const input = fixture.debugElement.query(By.css('input'));
    expect(input).toBeTruthy();
  });
});
