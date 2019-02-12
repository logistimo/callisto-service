import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NewDatastoreComponent } from './new-datastore.component';

describe('NewDatastoreComponent', () => {
  let component: NewDatastoreComponent;
  let fixture: ComponentFixture<NewDatastoreComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ NewDatastoreComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NewDatastoreComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
