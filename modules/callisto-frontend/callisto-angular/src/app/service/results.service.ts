import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

import {QueryResults} from '../model/queryresult'
import {GraphResult} from '../model/graph-result'

@Injectable()
export class ResultsService {

  private resultSource = new BehaviorSubject<any>([]);
  currentResult = this.resultSource.asObservable();

  constructor() { }

  changeState(graphResult : GraphResult) {
    this.resultSource.next(graphResult);
  }

  getState() {
    return this.resultSource.asObservable();
  }

}
