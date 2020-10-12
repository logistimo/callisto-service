import {Component, OnInit} from '@angular/core';
import {FormControl} from '@angular/forms';
import {Utils} from '../util/utils'
import {DataService} from '../service/data.service';
import {QueryService} from '../service/query.service';
import {QueryText} from '../model/querytext'
import {debounceTime, distinctUntilChanged, filter, map} from "rxjs/operators";

@Component({
  selector: 'app-query-search',
  templateUrl: './query-search.component.html',
  styleUrls: ['./query-search.component.css'],
  providers: [DataService]
})
export class QuerySearchComponent implements OnInit {
  queryIdField: FormControl = new FormControl();
  resultQueryList;
  queryId: string;
  queryText: QueryText;


  constructor(
    private dataService: DataService,
    private queryService: QueryService
  ) {
    this.queryIdField.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        filter(term => Utils.checkNotNullEmpty(term)),
        map(queryId => this.dataService.searchQueryIdLike(queryId)))
      .subscribe(res => {
        res.subscribe(queryIdList => {
          this.resultQueryList = queryIdList;
        })
      });
  }

  queryIdSelected(event: any) {
    this.dataService.getQuery(this.queryId)
      .subscribe(queryText => {
        this.queryText = queryText as QueryText;
      })
  }

  emitQueryText(event: any) {
    this.queryService.changeState(this.queryText);
  }

  ngOnInit() {
  }

}
