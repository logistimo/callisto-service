import { Component, OnInit, Input, Output, EventEmitter, SimpleChanges } from '@angular/core';
import { DataService } from '../service/data.service';
import { ResultsService } from '../service/results.service';
import { QueryService } from '../service/query.service';
import { QuerySharingService } from '../service/query-sharing.service';
import {QueryText} from '../model/querytext'
import {GraphResult} from '../model/graph-result'
import {QueryResults} from '../model/queryresult'
import {Filter} from '../model/filter'
import {FilterResult} from '../model/filter-result'
import {QueryRequest} from '../model/queryrequest'
import { RouterModule, Routes, Router, ActivatedRoute } from '@angular/router';
import { Utils } from '../util/utils'
import { FilterResultsAdapterUtils } from '../util/filter.results.adapter'
import {ReactiveFormsModule, FormControl, FormsModule, FormArray, FormGroup, AbstractControl } from '@angular/forms';
import {MatSnackBar} from '@angular/material';

@Component({
  selector: 'app-query',
  templateUrl: './query.component.html',
  styleUrls: ['./query.component.css'],
  providers: [ DataService ]
})
export class QueryComponent implements OnInit {

  private query : QueryText;
  private filtersMetadata;
  private domainFilters : Filter[] = [];
  private filterResults = {};
  filterFormGroup: FormGroup;
  private filterDisplayNames;
  private filters = {};
  private columnsFilterId;

  constructor(private dataService:DataService, private resultsService:ResultsService,
              private queryService:QueryService,  private router: Router, private route: ActivatedRoute,
              private querySharingService : QuerySharingService, public snackBar: MatSnackBar) {
    this.filterFormGroup = new FormGroup({
      filterFormArray : new FormArray([])
    });
    this.filtersMetadata = [];
  }

  private onQueryChanged(newQuery) {
    this.query.query = newQuery;
    this.extractFiltersFromQuery(this.query.query);
  }

  getQuery(queryId:string) {
    this.dataService.getQuery(queryId)
        .subscribe(query => {
          this.query = query;
          this.querySharingService.changeState(query)
        }
    )
  }

  runQuery(event, mQueryText:QueryText) {
    const request : QueryRequest = new QueryRequest();
    request.query = new QueryText(mQueryText.query, mQueryText.datastore_id);
    request.columnText = {};
    request.columnText[this.columnsFilterId] =  mQueryText.columns;
    request.filters = this.populateFilters();
    this.dataService.runQuery(request)
        .subscribe(data => {
          const result = new GraphResult();
          result.query_id = mQueryText.query_id;
          result.result = JSON.parse(data);
          this.resultsService.changeState(result)
        });
  }

  populateFilters() {
    for(let key in this.filterDisplayNames) {
      if(Utils.checkNullEmpty(this.filters[key])) {
        this.filters[key] = this.filterDisplayNames[key];
      }
    }
    return this.filters;
  }

  getDomainFilters() {
    this.dataService.getDomainFilters()
        .subscribe(data => {
          if (data instanceof Array) {
            const _domainFilters = this.domainFilters;
            for(var i=0, len = data.length; i< len;i++) {
              const filter: Filter = data[i] as Filter;
              _domainFilters.push(filter);
            }
            this.extractFiltersFromQuery(this.query.query);
          }
          this.setupAutocompleteForFilters();
        });
  }


  ngOnInit() {
    this.filterDisplayNames = {};
    this.querySharingService.currentResult
        .subscribe(res => {
          if (Utils.checkNotNullEmpty(res)) {
            this.query = res as QueryText;
            this.getDomainFilters();
          }
        });
    const url = this.route.snapshot.url;
    if(url[0].path == 'query' && url.length > 1) {
      this.getQuery(url[1].path);
    }
  }

  private extractFiltersFromQuery(query:string) {
    const filterIdentifierPrefix = '{{';
    const filterIdentifierSuffix = '}}';
    this.filtersMetadata = [];
    this.filterFormGroup.controls.filterFormArray = new FormArray([]);
    var index = 0;
    if(Utils.checkNullEmpty(query)) {
      return;
    }
    while (index < query.length) {
      const startIndex = query.indexOf(filterIdentifierPrefix, index);
      const endIndex = query.indexOf(filterIdentifierSuffix, startIndex);
      if(startIndex == -1 || endIndex == -1) {
        break;
      }
      const filterId = query.substr(startIndex, endIndex-startIndex+filterIdentifierPrefix.length).slice(2,-2);
      const filter : Filter = this.findFilterFromFilterId(filterId, this.domainFilters);

      const formControl = new FormControl();
      if(filter.is_column_filter) {
        this.columnsFilterId = filter.filter_id;
      }
      const filterData = {filter: filter, formControl: formControl};
      this.filtersMetadata.push(filterData);
      (this.filterFormGroup.controls.filterFormArray as FormArray).push(formControl);
      index = endIndex + filterIdentifierSuffix.length;
    }
  }

  private findFilterFromFilterId(filterId, filters: Filter[]) {
    for (var i = 0; i < filters.length; i++) {
      if(filterId == filters[i].filter_id) {
        return filters[i];
      }
    }
    return new Filter();
  }

  private filterValueSelected(event, filterId) {
    const filterResult : FilterResult = event.option.value as FilterResult;
    this.filterDisplayNames[filterId] =  filterResult.name;
    this.filters[filterId] = filterResult.value;
  }

  private setupAutocompleteForFilters() {
    const _dataservice = this.dataService;
    const _filterResults = this.filterResults;
    const _filters = this.filters;
    this.filtersMetadata.forEach(function (filterData) {
      filterData.formControl.valueChanges
          .debounceTime(500)
          .distinctUntilChanged()
          .filter(term => Utils.checkNotNullEmpty(term))
          .subscribe(term => {
            _dataservice
                .getFilterResults(term, filterData.filter.filter_id)
                .subscribe(result => {
                  const filterQueryResult : QueryResults = result as QueryResults;
                  _filterResults[filterData.filter.filter_id] = FilterResultsAdapterUtils.getFilterResultsFromQueryResults(filterQueryResult, filterData.filter);
                })
          });
    });
  }

  openSnackBar(message: string, action: string) {
    this.snackBar.open(message, action, {
      duration: 2000,
    });
  }
}
