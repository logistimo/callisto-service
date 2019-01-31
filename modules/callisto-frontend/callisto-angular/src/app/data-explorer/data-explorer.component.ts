import {Component, ElementRef, Inject, Input, OnInit} from '@angular/core';
import {ResultsService} from "../service/results.service";
import {Utils} from "../util/utils";
import {GraphResult} from "../model/graph-result";

@Component({
  selector: 'app-data-explorer',
  templateUrl: './data-explorer.component.html',
  styleUrls: ['./data-explorer.component.css']
})
export class DataExplorerComponent implements OnInit {

  private el:ElementRef;
  data = {result : {rows : [], headings: []}};
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
          console.log(this.data);
        }
      });
  }


}
