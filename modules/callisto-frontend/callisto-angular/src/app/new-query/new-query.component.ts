import {Component, OnInit} from '@angular/core';
import {MatSnackBar} from '@angular/material/snack-bar';
import {QueryText} from '../model/querytext';
import {Datastore} from '../model/datastore';
import {DataService} from '../service/data.service';
import {Utils} from '../util/utils'
import {FormControl} from '@angular/forms';
import {ApiResponse} from "../model/apiresponse";
import {debounceTime, distinctUntilChanged, filter, map} from "rxjs/operators";

@Component({
  selector: 'app-new-query',
  templateUrl: './new-query.component.html',
  styleUrls: ['./new-query.component.css'],
  providers: [DataService]
})
export class NewQueryComponent implements OnInit {

  queryTextModel: QueryText;
  datastores: Datastore[] = [];
  queryIdField: FormControl = new FormControl();
  queryIdUnavailable = true;
  private searchedQueryText: QueryText;

  constructor(public snackBar: MatSnackBar, private dataService: DataService) {
  }

  ngOnInit() {
    this.queryTextModel = new QueryText();
    this.getDatastores();
    this.checkQueryIdAvailability();
  }

  saveQuery(event, mQueryText: QueryText) {
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
        let apiResponse = response as ApiResponse<Array<Datastore>>;
        apiResponse.payload.forEach(function (datastore: Datastore) {
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
    this.queryIdField.valueChanges.pipe(
      debounceTime(400),
      distinctUntilChanged(),
      filter(term => {
        this.queryIdUnavailable = Utils.checkNullEmpty(term) ? true : this.queryIdUnavailable;
        return Utils.checkNotNullEmpty(term);
      }),
      map(queryId => this.dataService.getQuery(queryId)))
      .subscribe(res => {
        res.subscribe(queryResult => {
          this.queryIdUnavailable = Utils.checkNotNullEmpty(queryResult);
          this.searchedQueryText = queryResult;
        })
      });
  }


}
