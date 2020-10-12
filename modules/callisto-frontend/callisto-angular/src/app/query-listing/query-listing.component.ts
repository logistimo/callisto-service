import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {QueryText} from '../model/querytext'
import {Datastore} from '../model/datastore'
import {DataService} from '../service/data.service';

import {Utils} from '../util/utils'
import {FormControl} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {QuerySharingService} from '../service/query-sharing.service';
import {ApiResponse} from "../model/apiresponse";
import {debounceTime, distinctUntilChanged, map} from "rxjs/operators";
import {MatSnackBar} from "@angular/material/snack-bar";

@Component({
  selector: 'app-query-listing',
  templateUrl: './query-listing.component.html',
  styleUrls: ['./query-listing.component.css'],
  providers: [DataService]
})
export class QueryListingComponent implements OnInit {

  queryIdSearchField: FormControl = new FormControl();
  defaultPageSize = 10;
  page = 0;
  pageSize = this.defaultPageSize;
  totalDataSize;
  datastores: Datastore[] = [];
  searchQueryId;
  multi = false;

  showGraphForQueryId;
  queries: QueryText[] = [];
  @Output() onRunQuery: EventEmitter<any> = new EventEmitter<any>();

  constructor(private dataService: DataService, private router: Router, private route: ActivatedRoute,
              private querySharingService: QuerySharingService, public snackBar: MatSnackBar) {
    this.queryIdSearchField.valueChanges
      .pipe(
        debounceTime(1000),
        distinctUntilChanged())
      .subscribe(searchedQueryId => {
        return this.updateQueryListing(searchedQueryId, this.page, this.pageSize)
      });
  }

  ngOnInit() {
    this.updateQueryListing('', 0, this.defaultPageSize);
    this.dataService.getDatastores()
      .subscribe(response => {
        var _dbs = this.datastores;
        let apiResponse = response as ApiResponse<Array<Datastore>>;
        apiResponse.payload.forEach(function (datastore: Datastore) {
          _dbs.push(datastore);
        });
      });
    this.querySharingService.changeState(null)
  }

  public updateGraphQueryId(queryId: any): void {
    this.showGraphForQueryId = queryId;
  }

  navigateToQueryComponent($event, queryText: QueryText) {
    this.router.navigate(['../query', queryText.query_id], {relativeTo: this.route});
    this.querySharingService.changeState(queryText)
  }

  deleteQuery(event, queryText: QueryText) {
    this.dataService.deleteQuery(queryText.query_id)
      .subscribe(res => {
          this.showSnackbar(res.msg);
          this.updateQueryListing(this.searchQueryId, this.page, this.pageSize);
        }
      );
  }

  updateQueryListing(searchQueryId, page, pageSize) {
    const queries = Utils.checkNullEmpty(searchQueryId)
      ? this.dataService.getQueries(page, pageSize) : this.dataService.searchQueriesLike(searchQueryId, page, pageSize);
    queries.pipe(
      map(res => {
        this.totalDataSize = res['total_size'];
        return Utils.checkNotNullEmpty(res.result) ? res.result : []
      }),
      map(queries => {
        if (queries.length == 0 && this.page > 0) {
          this.page = 0;
          this.updateQueryListing(searchQueryId, this.page, pageSize);
        }
        return queries;
      }))
      .subscribe((queries: QueryText[]) => {
        this.queries.length = 0;
        var _queries = this.queries;
        queries.forEach(function (query: QueryText) {
          _queries.push(query);
        });
      });
  }

  getDatastoreNameForId(datastoreId): String {
    for (let datastore of this.datastores) {
      if (datastore.id == datastoreId) {
        return datastore.name;
      }
    }
  }

  onPaginateChange(event) {
    this.page = event.pageIndex;
    this.pageSize = event.pageSize;
    this.updateQueryListing(this.searchQueryId, event.pageIndex, event.pageSize);
  }

  goToSaveQuery(event) {
    this.router.navigate(['../new'], {relativeTo: this.route});
  }

  private showSnackbar(msg) {
    this.snackBar.open(msg, 'close', {duration: 2000});
  }

}
