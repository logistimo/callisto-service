import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { QueryListingComponent } from './query-listing.component';

describe('QueryListingComponent', () => {
  let component: QueryListingComponent;
  let fixture: ComponentFixture<QueryListingComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ QueryListingComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(QueryListingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
