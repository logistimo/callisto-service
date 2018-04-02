import { Component, OnInit, ElementRef, Inject, AfterViewInit } from '@angular/core';
import {MatDialog, MatSnackBar, MatDialogRef, MAT_DIALOG_DATA} from '@angular/material';
import {ReactiveFormsModule, FormControl, FormsModule} from '@angular/forms';
import {Observable} from 'rxjs/Observable'

import {QueryText} from '../model/querytext'
import {CallistoUser} from '../model/callistouser'
import {ServerConfig} from '../model/serverconfig'
import {QueryRequest} from '../model/queryrequest'
import {Utils} from '../util/utils'
import { DataService } from '../service/data.service';
import { ResultsService } from '../service/results.service';

import '../../../node_modules/pivottable/dist/pivot.min.js';
import '../../../node_modules/pivottable/dist/pivot.min.css';

import 'rxjs/add/operator/debounceTime';
import 'rxjs/add/operator/distinctUntilChanged';
import 'rxjs/add/operator/switchMap';


@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.css'],
    providers: [DataService]
})
export class HomeComponent implements OnInit {

    queryTextModel = new QueryText('','');
    queryTextModelFinal = new QueryText('','');

    serverConfigs:ServerConfig[] = [];

    dataEmpty = false;
    private el:ElementRef;

    constructor(private dataService:DataService, private resultsService:ResultsService, @Inject(ElementRef)el:ElementRef, public dialog: MatDialog) {
        this.el = el;
    }

    ngOnInit() {
        this.dataService.getUser().subscribe((response:Response) => {
            var _dbs = this.serverConfigs;
            var _dbsModel = this.queryTextModel;
            let body : CallistoUser = JSON.parse(response['_body']) as CallistoUser;
            body.server_configs.forEach(function (server:ServerConfig) {
                _dbs.push(server);
                if (Utils.checkNullEmpty(_dbsModel.server_id)) {
                    _dbsModel.server_id = server.id;
                }
            });
        });
    }

    runQuery(event, mQueryText:QueryText) {
        const request : QueryRequest = new QueryRequest();
        request.query = new QueryText(mQueryText.query, mQueryText.server_id);
        request.columnText = {TOKEN_COLUMNS: mQueryText.columns}

        this.dataService.runQuery(request).subscribe(data => {
            this.resultsService.changeState(JSON.parse(data))
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
            .map(queryId => this.dataService.searchQueryId(queryId) )
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
            if(data.status == 200) {
                this.dialogRef.close();
                this.snackBar.open("Success!", '', {
                    duration: 2000,
                });
            } else {
                this.snackBar.open("Failure!", '', {
                    duration: 2000,
                });
            }
            return (data);
        });
    }
}
