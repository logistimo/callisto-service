import { Injectable } from '@angular/core';
import { QueryText } from '../model/querytext';
import { BehaviorSubject } from 'rxjs';

@Injectable()
export class QueryService {

  private queryTextSource = new BehaviorSubject<QueryText>(new QueryText('',''));
  currentQueryText = this.queryTextSource.asObservable();

  constructor() { }

  changeState(queryText) {
    this.queryTextSource.next(queryText);
  }

  getState() {
    return this.queryTextSource.asObservable();
  }

}
