import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { QueryText } from '../model/querytext'
import { Datastore } from '../model/datastore'
import { Utils } from '../util/utils'
import 'rxjs/add/operator/map'

@Injectable()
export class DataService {

  requestOption;
  public defaultUserName = "logistimo";
  constructor(private http:HttpClient) {
    this.requestOption = {
      params: {},
      headers: {}
    };
    this.requestOption.headers['X-app-version'] = 'v2';
  }

  private getDefaultRequestOptions() {
    var reqOptions = {
      params: {},
      headers: {}
    };
    reqOptions.headers['X-app-version'] = 'v2';
    reqOptions.params['userId'] = this.defaultUserName;
    return reqOptions
  }


  getUser() {
    return this.http.get('user/get', this.requestOption);
  }

  getDatastores() {
    var reqOptions = this.getDefaultRequestOptions();
    return this.http.get('datastore/', reqOptions);
  }

  getQueryIds(page, size) {
    var reqOptions = this.getDefaultRequestOptions();
    reqOptions.params['page'] = page;
    reqOptions.params['size'] = String(size);
    return this.http.get('query/ids', reqOptions)
        .subscribe(res => { return Utils.checkNotNullEmpty(res['_body']) ?
          JSON.parse(res['_body']) : null
        });
  }

  getQueries(page, size) {
    var reqOptions = this.getDefaultRequestOptions();
    reqOptions.params['page'] = page;
    reqOptions.params['size'] = String(size);
    reqOptions['observe'] = 'response';
    return this.http.get('query', reqOptions)
        .map(res => {
          return res['body'];
        });
  }

  searchQueriesLike(term, page, size) {
    var reqOptions = this.getDefaultRequestOptions();
    reqOptions.params['page'] = page;
    reqOptions.params['size'] = String(size);
    reqOptions['observe'] = 'response';
    return this.http.get('query/search/' + term, reqOptions)
        .map(res => {
          return res['body'];
        });
  }

  getDomainFilters() {
    var reqOptions = this.getDefaultRequestOptions();
    return this.http.get('filter', reqOptions);
  }

  getQuery(queryId) {
    return this.http.get('query/' + queryId, this.requestOption)
      .map(res => { return Utils.checkNotNullEmpty(res) ? res as any : null});
  }

  runQuery(body) {
    return this.http.post('query/run', body, this.requestOption);
  }

  saveQuery(body : QueryText) {
    const reqOptions = this.getDefaultRequestOptions();
    return this.http.put('query/save', body, reqOptions)
        .map(res => { return Utils.checkNotNullEmpty(res) ? res as any : null})
  }

  searchQueryIdLike(term) {
    const reqOptions = this.getDefaultRequestOptions();
    reqOptions['observe'] = 'response';
    return this.http.get('query/all/' + term, reqOptions)
      .map(res => { return Utils.checkNotNullEmpty(res['body']) ?
            {result: res['body'], totalSize: res['headers'].get('size')} : null;
        });
  }

  getFilterResults(searchTerm:string, filterId:string):any {
    var reqOptions = this.getDefaultRequestOptions();
    reqOptions.params['search'] = searchTerm;
    return this.http.get('filter/search/' + filterId, reqOptions);
  }

  deleteQuery(query_id:string):any {
    var reqOptions = this.getDefaultRequestOptions();
    return this.http.delete('query/' + query_id, reqOptions)
        .map(res => { return Utils.checkNotNullEmpty(res) ? res as any : null});
  }

  getDatastore(datastoreId:string):any {
    var reqOptions = this.getDefaultRequestOptions();
    return this.http.get('datastore/' + datastoreId, reqOptions)
        .map(res => { return Utils.checkNotNullEmpty(res) ? res as any : null});
  }

  saveDatastore(datastoreModel : Datastore):any {
    datastoreModel.userId = this.defaultUserName;
    const reqOptions = this.getDefaultRequestOptions();
    return this.http.put('datastore', datastoreModel, reqOptions)
        .map(res => { return Utils.checkNotNullEmpty(res) ? res as any : null});
  }
}
