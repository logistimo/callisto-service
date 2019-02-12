import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DatastoresComponent } from './datastores.component';

describe('DatastoresComponent', () => {
  let component: DatastoresComponent;
  let fixture: ComponentFixture<DatastoresComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DatastoresComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DatastoresComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
