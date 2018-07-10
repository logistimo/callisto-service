import { Component, OnInit } from '@angular/core';
import {ReactiveFormsModule, FormControl, FormsModule} from '@angular/forms';
import {Utils} from '../util/utils'
import { DataService } from '../service/data.service';
import { QueryService } from '../service/query.service';
import { QueryText } from '../model/querytext'

@Component({
  selector: 'app-query-search',
  templateUrl: './query-search.component.html',
  styleUrls: ['./query-search.component.css'],
  providers: [DataService]
})
export class QuerySearchComponent implements OnInit {
  private queryIdField: FormControl = new FormControl();
  private resultQueryList;
  private queryId : string;
  private queryText : QueryText;


  constructor(
      private dataService:DataService,
      private queryService:QueryService
  ) {
    this.queryIdField.valueChanges
      .debounceTime(300)
      .distinctUntilChanged()
      .filter(term => Utils.checkNotNullEmpty(term))
      .map(queryId => this.dataService.searchQueryIdLike(queryId))
      .subscribe(res => {
          res.subscribe(queryIdList => {
            this.resultQueryList = queryIdList;
          })
        });
  }

  queryIdSelected(event : any) {
    this.dataService.getQuery(this.queryId)
      .subscribe(queryText => {
          this.queryText = queryText as QueryText;
        })
  }

  emitQueryText(event : any) {
    this.queryService.changeState(this.queryText);
  }

  ngOnInit() {
  }

}
