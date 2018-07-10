import { Component, OnInit, ViewChild, Output, EventEmitter} from '@angular/core';
import {QueryText} from '../model/querytext'
import {Datastore} from '../model/datastore'
import { DataService } from '../service/data.service';
import { QueryService } from '../service/query.service';
import { PageEvent } from '@angular/material';
import { Utils } from '../util/utils'
import {ReactiveFormsModule, FormControl, FormsModule} from '@angular/forms';
import { RouterModule, Routes, Router, ActivatedRoute } from '@angular/router';
import { QuerySharingService } from '../service/query-sharing.service';

@Component({
  selector: 'app-query-listing',
  templateUrl: './query-listing.component.html',
  styleUrls: ['./query-listing.component.css'],
  providers: [DataService]
})
export class QueryListingComponent implements OnInit {

  private queryIdSearchField: FormControl = new FormControl();
  defaultPageSize = 10;
  page = 0;
  pageSize = this.defaultPageSize;
  totalDataSize;
  datastores : Datastore[] = [];
  searchQueryId;
  multi = false;

  showGraphForQueryId;
  private queries: QueryText[] = [];
  @Output() onRunQuery: EventEmitter<any> = new EventEmitter<any>();

  constructor(private dataService: DataService, private router: Router, private route: ActivatedRoute, private querySharingService : QuerySharingService) {
    this.queryIdSearchField.valueChanges
        .debounceTime(1000)
        .distinctUntilChanged()
        .subscribe(searchedQueryId => {return this.updateQueryListing(searchedQueryId, this.page, this.pageSize)});
  }

  ngOnInit() {
    this.updateQueryListing('', 0, this.defaultPageSize);
    this.dataService.getDatastores().subscribe((response : Response) => {
      var _dbs = this.datastores;
      let datastores = JSON.parse(response['_body']);
      datastores.forEach(function (datastore: Datastore) {
        _dbs.push(datastore);
      });
    });
    this.querySharingService.changeState(null)
  }

  public updateGraphQueryId(queryId : any) : void {
    this.showGraphForQueryId = queryId;
  }

  goToQueryComponent($event, queryText: QueryText) {
    this.router.navigate(['../query' , queryText.query_id], { relativeTo: this.route });
    this.querySharingService.changeState(queryText)
  }

  updateQueryListing(searchQueryId, page, pageSize) {
    var call = Utils.checkNullEmpty(searchQueryId)
        ? this.dataService.getQueries(page, pageSize) : this.dataService.searchQueriesLike(searchQueryId, page, pageSize);
    call
        .map(res => {
          this.totalDataSize = res.totalSize;
          return Utils.checkNotNullEmpty(res.result) ? res.result : []
        })
        .map(queries => {
          if(queries.length == 0 && this.page > 0) {
            this.page = 0;
            this.updateQueryListing(searchQueryId, this.page, pageSize);
          }
          return queries;
        })
        .subscribe((queries:QueryText[]) => {
          this.queries.length = 0;
          var _queries = this.queries;
          queries.forEach(function (query:QueryText) {
            _queries.push(query);
          });
        });
  }

  getDatastoreNameForId(datastoreId) : String {
    for(let datastore of this.datastores) {
      if(datastore.id == datastoreId) {
        return datastore.name;
      }
    }
  }

  onPaginateChange(event) {
    this.page = event.pageIndex;
    this.pageSize = event.pageSize;
    this.updateQueryListing(this.searchQueryId, event.pageIndex, event.pageSize);
  }

}
