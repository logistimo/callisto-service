import { Component, OnInit, ElementRef, Inject, Input } from '@angular/core';
import { ResultsService } from '../service/results.service';

import '../../../node_modules/pivottable/dist/pivot.min.js';
import '../../../node_modules/pivottable/dist/pivot.min.css';

import { Utils } from '../util/utils'
import { GraphResult } from '../model/graph-result'

declare var jQuery:any;
declare var $:any;

@Component({
  selector: 'app-graph',
  templateUrl: './graph.component.html',
  styleUrls: ['./graph.component.css']
})
export class GraphComponent implements OnInit {

  private el:ElementRef;
  private result;
  @Input() queryId;

  constructor(@Inject(ElementRef) el:ElementRef, private resultsService : ResultsService) {
    this.el = el;
  }

  ngOnInit() {
    this.resultsService.currentResult
        .subscribe(res => {
          if (Utils.checkNotNullEmpty(res) && res instanceof GraphResult) {
            const graphResult = res as GraphResult;
            this.result = [];
            this.result.push(graphResult.result['headings']);
            this.result = this.result.concat(graphResult.result['rows']);
            this.renderTable();
          }
        });
  }

  renderTable() {

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

    while (targetElement.firstChild) {
      targetElement.removeChild(targetElement.firstChild);
    }

    console.log($.pivotUtilities);
    targetElement.pivotUI(this.result,
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

}
