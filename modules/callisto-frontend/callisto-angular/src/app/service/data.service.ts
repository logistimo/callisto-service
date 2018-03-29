import { Injectable } from '@angular/core';
import { HttpService } from './http.service';
import { Observable } from 'rxjs/Observable';
import { RequestOptions, Headers} from '@angular/http';
import { QueryText } from '../model/querytext'
import { Utils } from '../util/utils'

@Injectable()
export class DataService {

  requestOption : RequestOptions;

  constructor(private httpClient:HttpService) {
    this.requestOption = new RequestOptions();
    this.requestOption.headers = new Headers();
    this.requestOption.headers.append('X-app-version', 'v2');
  }

  getUser() {
    return this.httpClient.get('user/get', this.requestOption);
  }

  runQuery(body) {
    return this.httpClient.post('query/run', body, this.requestOption)
  }

  saveQuery(body) {
    return this.httpClient.post('query/save', body, this.requestOption)
  }

  searchQueryId(queryId) {
    return this.httpClient.get('query/get/' + queryId, this.requestOption)
      .map(res => { return Utils.checkNotNullEmpty(res._body) ?
          JSON.parse(res._body) as QueryText : null
        });
  }
}
