import {Component, ElementRef, Inject, Input, OnInit} from '@angular/core';
import {ResultsService} from "../service/results.service";
import {Utils} from "../util/utils";
import {GraphResult} from "../model/graph-result";
import {MatTableDataSource} from "@angular/material";

@Component({
  selector: 'app-data-explorer',
  templateUrl: './data-explorer.component.html',
  styleUrls: ['./data-explorer.component.css']
})
export class DataExplorerComponent implements OnInit {

  private el:ElementRef;
  data = {result : {rows : [], headings: []}};
  dataSource = new MatTableDataSource();
  displayedColumns = [];
  @Input() queryId;

  constructor(@Inject(ElementRef) el:ElementRef, private resultsService : ResultsService) {
    this.el = el;
  }

  ngOnInit() {
    this.resultsService.changeState(null);
    this.resultsService.currentResult
      .subscribe(res => {
        if (Utils.checkNotNullEmpty(res) && res instanceof GraphResult) {
          this.data = res as GraphResult;
          this.dataSource.data = this.transformData(res as GraphResult);
          this.displayedColumns = this.data.result.headings;
          console.log(this.dataSource);
        }
      });
  }

  private transformData(graphResult: GraphResult) {
    let transformedData = [];
    if(Utils.checkNotNullEmpty(graphResult.result)) {
      graphResult.result.rows.map(function(row){
        let transformedRow = {};
        for(let i=0;i<graphResult.result.headings.length;i++) {
          transformedRow[graphResult.result.headings[i]] = row[i];
        }
        transformedData.push(transformedRow);
      });
    }
    return transformedData;
  }


}
