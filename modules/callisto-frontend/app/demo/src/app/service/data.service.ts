import { Injectable } from '@angular/core';
import { HttpClient} from '@angular/common/http';

@Injectable()
export class DataService {
    baseUrl: String = "http://localhost:8090";

    header = {headers: {'X-app-version':'v2'}};

    constructor(private httpClient : HttpClient){

    }

    getUser(){
        return this.httpClient.get(this.baseUrl + '/user/get', this.header);
    }

    runQuery(body){
        return this.httpClient.post(this.baseUrl + '/query/run', body, this.header)
    }

    saveQuery(body){
        return this.httpClient.put(this.baseUrl + '/query/save', body, this.header)
    }
}