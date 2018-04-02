import { Component, OnInit, ElementRef, Inject } from '@angular/core';
import { ResultsService } from '../service/results.service';

import '../../../node_modules/pivottable/dist/pivot.min.js';
import '../../../node_modules/pivottable/dist/pivot.min.css';

import { Utils } from '../util/utils'

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

  constructor(@Inject(ElementRef) el:ElementRef, private resultsService : ResultsService) {
    this.el = el;
  }

  ngOnInit() {
    this.resultsService.currentResult.subscribe(res => {
      if(Utils.checkNotNullEmpty(res) && res instanceof Array && res.length > 0) {
        this.result = res;
        this.renderTable();
      }
    })
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
