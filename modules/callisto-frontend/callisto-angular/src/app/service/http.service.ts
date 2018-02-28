import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import 'rxjs/Rx';
import {
    Http,
    RequestOptions,
    RequestOptionsArgs,
    Response,
    Request,
    Headers,
    XHRBackend
} from '@angular/http';

import { AngularReduxRequestOptions } from './angular.request.options';

@Injectable()
export class HttpService extends Http {

  apiUrl = 'http://localhost:8090/';

  constructor(
      backend: XHRBackend,
      defaultOptions: AngularReduxRequestOptions
  ) {
    super(backend, defaultOptions);
  }

  get(url: string, options?: RequestOptionsArgs): Observable<any> {


    return super.get(this.getFullUrl(url), this.requestOptions(options))
        .catch(this.onCatch)
        .do((res: Response) => {
          this.onSuccess(res);
        }, (error: any) => {
          this.onError(error);
        })
        .finally(() => {
          this.onEnd();
        });

  }

  private requestOptions(options?: RequestOptionsArgs): RequestOptionsArgs {

    if (options == null) {
      options = new AngularReduxRequestOptions();
    }

    if (options.headers == null) {
      options.headers = new Headers();
    }

    return options;
  }

  private getFullUrl(url: string): string {
    return this.apiUrl + url;
  }

  private onCatch(error: any, caught: Observable<any>): Observable<any> {
    return Observable.throw(error);
  }

  private onSuccess(res: Response): void {
    console.log('Request successful');
  }

  private onError(res: Response): void {
    console.log('Error, status code: ' + res.status);
  }

  private onEnd(): void {

  }

  /*private showLoader(): void {
    this.loaderService.show();
  }

  private hideLoader(): void {
    this.loaderService.hide();
  }*/

}