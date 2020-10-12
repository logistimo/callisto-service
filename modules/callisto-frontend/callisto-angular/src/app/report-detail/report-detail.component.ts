import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {DataService} from '../service/data.service';
import {ReportModel} from '../model/reportmodel'
import {Filter} from '../model/filter'
import {Utils} from '../util/utils'

@Component({
  selector: 'app-report-detail',
  templateUrl: './report-detail.component.html',
  styleUrls: ['./report-detail.component.css'],
  providers: [DataService]
})
export class ReportDetailComponent implements OnInit {

  constructor(private dataService:DataService, private route: ActivatedRoute) {

  }

  private type : string;
  private subType : string;

  report: ReportModel = new ReportModel();
  helpText : string;

  ngOnInit() {
    const url = this.route.snapshot.url;
    if(url.length == 2) {
      this.type = url[1].path;
      this.subType = null;
    } else if(url.length == 3) {
      this.type = url[1].path;
      this.subType = url[2].path;
    }
    this.fetchReport();
    this.fetchFilters();
  }

  private fetchReport():void {
    this.dataService.getReportModel(this.type, this.subType)
      .subscribe(res => {
          this.report = res as ReportModel;
        })
  }

  private fetchFilters():void {
    this.dataService.getDomainFilters()
      .subscribe(res => {
          const filters = res as Array<Filter>;
          this.helpText = filters
              .reduce(function(a:string, b:Filter) {
                const delimiter = Utils.checkNotNullEmpty(a) ? ', ' : '';
                return a + delimiter + b.filter_id;
              }, '');
        })
  }
}
