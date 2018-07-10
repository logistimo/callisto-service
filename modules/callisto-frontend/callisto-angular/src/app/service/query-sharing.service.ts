import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { QueryText } from '../model/querytext'

@Injectable({
  providedIn: 'root'
})
export class QuerySharingService {

  private querySource = new BehaviorSubject<QueryText>(null);
  currentResult = this.querySource.asObservable();

  constructor() { }

  changeState(queryText: QueryText) {
    this.querySource.next(queryText);
  }

  getState() {
    return this.querySource.asObservable();
  }
}
