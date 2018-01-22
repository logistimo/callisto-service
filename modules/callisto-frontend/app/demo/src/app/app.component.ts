import { Component, OnInit,Inject, ElementRef, AfterViewInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DataService } from './service/data.service';
import {MatDialog, MatDialogRef} from '@angular/material';

import 'pivottable/dist/pivot.min.js';
import 'pivottable/dist/pivot.min.css';

declare var jQuery:any;
declare var $:any;

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css'],
    providers: [DataService]
})
export class AppComponent {
    title = 'app';
    queryModel:string = '';
    colsModel:string = '';
    dbs = [];
    dbsModel = {id: 1};
    private el:ElementRef;

    constructor(private dataService:DataService, @Inject(ElementRef)el:ElementRef, public dialog:MatDialog) {
        this.el = el;
    }

    ngOnInit() {

        this.dataService.getUser().subscribe(data => {
            var _dbs = this.dbs;
            var _dbsModel = this.dbsModel;
            data['server_configs'].forEach(function (server) {
                _dbs.push(server);
                if (_dbsModel['id'] == undefined) {
                    _dbsModel = server;
                }
            });
            console.log(data);
        });

    }

    renderTable(input) {

        if (!this.el || !this.el.nativeElement || !this.el.nativeElement.children) {
            console.log('cant build without element');
            return;
        }

        var container = this.el.nativeElement;
        var inst = jQuery(container);
        var targetElement = inst.find('#output');

        if (!targetElement) {
            console.log('cant find the pivot element');
            return;
        }

        //this helps if you build more than once as it will wipe the dom for that element
        while (targetElement.firstChild) {
            targetElement.removeChild(targetElement.firstChild);
        }
        //here is the magic
        console.log($.pivotUtilities);
        targetElement.pivotUI(input,
            {
                rows: [],
                cols: [],
                rendererName: "Bar Chart",
                renderers: $.extend(
                    $.pivotUtilities.renderers,
                    $.pivotUtilities.c3_renderers,
                    $.pivotUtilities.export_renderers
                )
            });
    }


    runQuery(event, mQuery, mColumns, mDb) {
        console.log("query: " + mQuery);
        const body = {query: {query: mQuery, server_id: mDb}, columnText: {TOKEN_COLUMNS: mColumns}};
        this.dataService.runQuery(body).subscribe(data => {
            console.log(data);
            data['rows'].unshift(data['headings']);
            this.renderTable(data['rows']);
        });
    }

    saveQuery(event, mQuery, mColumns, mDb) {
        console.log("query: " + mQuery);
        var queryText = {query: mQuery, columns: mColumns, serverId: mDb};
        this.openQueryIdDialog(queryText);
    }

    openQueryIdDialog(queryText):void {
        this.dialog.open(DialogDataExampleDialog, {
            data: {
                animal: 'panda'
            }
        });
    }
}

@Component({
    selector: 'mt-dialog-container',
    templateUrl: 'save-query-dialog.html'
})
export class DialogDataExampleDialog {
    constructor(public data:any) {
        console.log(data);
    }
}