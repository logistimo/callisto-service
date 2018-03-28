import { XHRBackend } from '@angular/http';
import { AngularReduxRequestOptions } from '../service/angular.request.options';
import { HttpService } from '../service/http.service';

function httpServiceFactory(backend: XHRBackend, options: AngularReduxRequestOptions) {
    return new HttpService(backend, options);
}

export { httpServiceFactory };