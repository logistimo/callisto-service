import { Injectable } from '@angular/core';
import { HttpService } from './http.service';
import { Observable } from 'rxjs/Observable';
import { RequestOptions, Headers, URLSearchParams} from '@angular/http';
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

  private getDefaultRequestOptions() : RequestOptions {
    const reqOptions = new RequestOptions();
    reqOptions.headers = new Headers();
    reqOptions.headers.append('X-app-version', 'v2');
    if(Utils.checkNullEmpty(reqOptions.params)){
      reqOptions.params = new URLSearchParams();
      reqOptions.params.append('userId', "logistimo");
    }
    return reqOptions
  }


  getUser() {
    return this.httpClient.get('user/get', this.requestOption);
  }

  getDatastores() {
    var reqOptions = this.getDefaultRequestOptions();
    return this.httpClient.get('datastore/', reqOptions);
  }

  getQueryIds(page, size) {
    var reqOptions = this.getDefaultRequestOptions();
    reqOptions.params.append('page', page);
    reqOptions.params.append('size', String(size));
    return this.httpClient.get('query/ids', reqOptions)
        .map(res => { return Utils.checkNotNullEmpty(res['_body']) ?
          JSON.parse(res['_body']) : null
        });
  }

  getQueries(page, size) {
    var reqOptions = this.getDefaultRequestOptions();
    reqOptions.params.append('page', page);
    reqOptions.params.append('size', String(size));
    return this.httpClient.get('query', reqOptions)
        .map(res => {
          return Utils.checkNotNullEmpty(res._body) ?
          {result: JSON.parse(res._body), totalSize: res.headers._headers.get('size')[0]} : null
        });
  }

  getDomainFilters() {
    var reqOptions = this.getDefaultRequestOptions();
    return this.httpClient.get('filter', reqOptions)
        .map(res => {
          return Utils.checkNotNullEmpty(res._body) ? JSON.parse(res._body) : null
        });
  }

  searchQueriesLike(term, page, size) {
    var reqOptions = this.getDefaultRequestOptions();
    reqOptions.params.append('page', page);
    reqOptions.params.append('size', String(size));
    return this.httpClient.get('query/search/' + term, reqOptions)
        .map(res => {
          return Utils.checkNotNullEmpty(res._body) ?
          {result: JSON.parse(res._body), totalSize: res.headers._headers.get('size')[0]} : null;
        });
  }

  getQuery(queryId) {
    return this.httpClient.get('query/get/' + queryId, this.requestOption)
      .map(res => { return Utils.checkNotNullEmpty(res._body) ?
        JSON.parse(res._body) as QueryText : null
      });
  }

  runQuery(body) {
    return this.httpClient.post('query/run', body, this.requestOption)
      .map(res => res['_body'])
  }

  saveQuery(body) {
    return this.httpClient.put('query/save', body, this.requestOption)
  }

  searchQueryId(queryId) {
    return this.httpClient.get('query/get/' + queryId, this.requestOption)
      .map(res => { return Utils.checkNotNullEmpty(res._body) ?
          JSON.parse(res._body) as QueryText : null
        });
  }

  searchQueryIdLike(term) {
    return this.httpClient.get('query/all/' + term, this.requestOption)
      .map(res => { return Utils.checkNotNullEmpty(res._body) ?
            {result: JSON.parse(res._body), totalSize: res.headers._headers.get('size')[0]} : null;
        });
  }

  getFilterResults(searchTerm:string, filterId:string):any {
    var reqOptions = this.getDefaultRequestOptions();
    reqOptions.params.append('search', searchTerm);
    return this.httpClient.get('filter/search/' + filterId, reqOptions)
      .map(res => {
          return Utils.checkNotNullEmpty(res._body) ? JSON.parse(res._body) : null;
        });
  }
}
