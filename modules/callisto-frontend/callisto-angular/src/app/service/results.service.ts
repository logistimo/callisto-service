import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';

import {QueryResults} from '../model/queryresult'

@Injectable()
export class ResultsService {

  private resultSource = new BehaviorSubject<any>([]);
  currentResult = this.resultSource.asObservable();

  constructor() { }

  changeState(results) {
    results['rows'].unshift(results['headings']);
    this.resultSource.next(results['rows']);
  }

  getState() {
    return this.resultSource.asObservable();
  }

}
