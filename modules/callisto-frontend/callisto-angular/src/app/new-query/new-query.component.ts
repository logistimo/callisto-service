import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { MatSnackBar } from '@angular/material';
import { QueryText } from '../model/querytext';
import { Datastore } from '../model/datastore';
import { DataService } from '../service/data.service';
import {Utils} from '../util/utils'
import {ReactiveFormsModule, FormControl, FormsModule} from '@angular/forms';

@Component({
    selector: 'app-new-query',
    templateUrl: './new-query.component.html',
    styleUrls: ['./new-query.component.css'],
    providers: [DataService]
})
export class NewQueryComponent implements OnInit {

    private queryTextModel:QueryText;
    private datastores:Datastore[] = [];
    private queryIdField:FormControl = new FormControl();
    private queryIdUnavailable = true;
    private searchedQueryText:QueryText;

    @Output() pathEmitter = new EventEmitter<string>();

    constructor(public snackBar:MatSnackBar, private dataService:DataService) {
    }

    ngOnInit() {
        this.queryTextModel = new QueryText();
        this.getDatastores();
        this.checkQueryIdAvailability();
    }

    saveQuery(event, mQueryText:QueryText) {
        this.dataService.saveQuery(mQueryText)
            .subscribe(data => {
                this.showSnackbar(data.msg);
            });
    }

    private getDatastores() {
        this.dataService.getDatastores()
            .subscribe(response => {
                var _dbs = this.datastores;
                var _dbsModel = this.queryTextModel;
                let datastores = response as Array<Datastore>;
                datastores.forEach(function (datastore:Datastore) {
                    _dbs.push(datastore);
                    if (Utils.checkNullEmpty(_dbsModel.datastore_id)) {
                        _dbsModel.datastore_id = datastore.id;
                    }
                });
            });
    }

    private showSnackbar(msg) {
        this.snackBar.open(msg, 'close', {duration: 2000});
    }

    private checkQueryIdAvailability() {
        this.queryIdField.valueChanges
            .debounceTime(400)
            .distinctUntilChanged()
            .filter(term => {
                this.queryIdUnavailable = Utils.checkNullEmpty(term) ? true : this.queryIdUnavailable;
                return Utils.checkNotNullEmpty(term);
            })
            .map(queryId => this.dataService.getQuery(queryId))
            .subscribe(res => {
                res.subscribe(queryResult => {
                    this.queryIdUnavailable = Utils.checkNotNullEmpty(queryResult);
                    this.searchedQueryText = queryResult;
                })
            });
    }


}