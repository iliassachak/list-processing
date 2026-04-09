import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListHome } from './list-home';

describe('ListHome', () => {
  let component: ListHome;
  let fixture: ComponentFixture<ListHome>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListHome],
    }).compileComponents();

    fixture = TestBed.createComponent(ListHome);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
