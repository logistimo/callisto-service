import { Component, OnInit, ElementRef, Inject, AfterViewInit } from '@angular/core';
import {MatDialog, MatSnackBar, MatDialogRef, MAT_DIALOG_DATA} from '@angular/material';
import {ReactiveFormsModule, FormControl, FormsModule} from '@angular/forms';
import {Observable} from 'rxjs/Observable'

import {QueryText} from '../model/querytext'
import {CallistoUser} from '../model/callistouser'
import {Datastore} from '../model/datastore'
import {QueryRequest} from '../model/queryrequest'
import {Utils} from '../util/utils'
import { DataService } from '../service/data.service';
import { ResultsService } from '../service/results.service';
import { QueryService } from '../service/query.service';

import '../../../node_modules/pivottable/dist/pivot.min.js';
import '../../../node_modules/pivottable/dist/pivot.min.css';

import 'rxjs/add/operator/debounceTime';
import 'rxjs/add/operator/distinctUntilChanged';
import 'rxjs/add/operator/switchMap';
import {ApiResponse} from "../model/apiresponse";
import {GraphResult} from "../model/graph-result";


@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.css'],
    providers: [DataService]
})
export class HomeComponent implements OnInit {

    queryTextModel = new QueryText('','');
    datastores : Datastore[] = [];

    dataEmpty = false;
    private el:ElementRef;

    constructor(private dataService:DataService,
                private queryService:QueryService, @Inject(ElementRef)el:ElementRef, public dialog: MatDialog, private resultsService: ResultsService) {
        this.el = el;
    }

    ngOnInit() {
        this.dataService.getDatastores()
            .subscribe(response => {
                var _dbs = this.datastores;
                var _dbsModel = this.queryTextModel;
                let apiResponse = response as ApiResponse<Array<Datastore>>;
                apiResponse.payload.forEach(function (datastore:Datastore) {
                    _dbs.push(datastore);
                    if (Utils.checkNullEmpty(_dbsModel.datastore_id)) {
                        _dbsModel.datastore_id = datastore.id;
                    }
                });
            });
        this.queryService.currentQueryText.subscribe(res => {
                if(Utils.checkNotNullEmpty(res)) {
                    this.queryTextModel = res;
                }
            }
        )
    }

    runQuery(event, mQueryText:QueryText) {
        const request : QueryRequest = new QueryRequest();
        request.query = new QueryText(mQueryText.query, mQueryText.datastore_id);
        request.columnText = {TOKEN_COLUMNS: mQueryText.columns}

        this.dataService.runQuery(request).subscribe(data => {
          const result = new GraphResult();
          result.query_id = mQueryText.query_id;
          result.result = data;
          this.resultsService.changeState(result);
        });
    }

    saveQueryPopup(event, queryText:QueryText) {
        let dialogRef = this.dialog.open(SaveQueryDialog, {
            width: '720px',
            data: queryText.clone()
        });

        dialogRef.afterClosed().subscribe(result => {

        });
    }
}

@Component({
    selector: 'save-query-dialog',
    templateUrl: 'save-query-dialog.html',
    providers: [DataService]
})
export class SaveQueryDialog {
    queryToSave: any;
    private queryIdField: FormControl = new FormControl();
    searchedQueryText : QueryText;
    queryIdUnavailable = true;

    constructor(
        private dataService:DataService,
        public dialogRef: MatDialogRef<SaveQueryDialog>,
        public snackBar: MatSnackBar,
        @Inject(MAT_DIALOG_DATA) data: any) {
        this.queryToSave = data;

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
            } );
    }

    ngOnInit() {

    }

    private onCancelClick(): void {
        this.dialogRef.close();
    }

    saveQuery(event, queryTextModelFinal : QueryText) {
        this.dataService.saveQuery(queryTextModelFinal).subscribe(data => {
            return data;
        });
    }
}
