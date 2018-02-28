import { Component, OnInit, ElementRef, Inject, AfterViewInit } from '@angular/core';
import {QueryText} from '../model/querytext'
import {CallistoUser} from '../model/callistouser'
import {ServerConfig} from '../model/serverconfig'
import {Utils} from '../util/utils'
import { DataService } from '../service/data.service';

import '../../../node_modules/pivottable/dist/pivot.min.js';
import '../../../node_modules/pivottable/dist/pivot.min.css';

declare var jQuery:any;
declare var $:any;

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.css'],
    providers: [DataService]
})
export class HomeComponent implements OnInit {

    queryTextModel = new QueryText();
    queryTextModelFinal = new QueryText();

    serverConfigs:ServerConfig[] = [];

    //queryModel:string = '';
    //colsModel:string = '';
    //dbs = [];
    dataEmpty = false;
    private el:ElementRef;

    constructor(private dataService:DataService, @Inject(ElementRef)el:ElementRef) {
        this.el = el;
    }

    ngOnInit() {
        this.dataService.getUser().subscribe((response:Response) => {
            var _dbs = this.serverConfigs;
            var _dbsModel = this.queryTextModel;
            let body = response.json();
            console.log(response)
            /*(body as CallistoUser).server_configs.forEach(function (server:ServerConfig) {
                _dbs.push(server);
                if (Utils.checkNullEmpty(_dbsModel.server_id)) {
                    _dbsModel.server_id = server.id;
                }
            });*/
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

    runQuery(event, mQueryText:QueryText) {
        const body = {
            query: {query: mQueryText.query, server_id: mQueryText.server_id},
            columnText: {TOKEN_COLUMNS: mQueryText.columns}
        };
        this.dataService.runQuery(body).subscribe(data => {
            console.log(data);
            data['rows'].unshift(data['headings']);
            this.renderTable(data['rows']);
        });
    }

    copyQueryModel() {
        this.queryTextModelFinal = this.queryTextModel.clone();
        console.log(this.queryTextModelFinal);
    }

    saveQuery(event, queryTextModelFinal:QueryText) {
        if (Utils.checkNullEmpty(queryTextModelFinal.query_id)) {
            this.dataEmpty = true;
            return;
        }
        console.log(queryTextModelFinal);
        this.dataService.saveQuery(queryTextModelFinal).subscribe(function (data) {
            console.log("save response: " + data);
            return (data);
        })
    }

}
