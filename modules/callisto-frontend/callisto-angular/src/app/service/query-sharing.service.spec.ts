import { TestBed, inject } from '@angular/core/testing';

import { QuerySharingService } from './query-sharing.service';

describe('QuerySharingService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [QuerySharingService]
    });
  });

  it('should be created', inject([QuerySharingService], (service: QuerySharingService) => {
    expect(service).toBeTruthy();
  }));
});
