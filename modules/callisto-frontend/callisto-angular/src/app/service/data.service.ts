import { Injectable } from '@angular/core';
import { HttpService } from './http.service';
import { Observable } from 'rxjs/Observable';
import { RequestOptions, Headers} from '@angular/http';

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
}
