import { Component, OnInit } from '@angular/core';
import { DataService } from '../service/data.service';
import { ReportModel } from '../model/reportmodel';
import { Utils } from '../util/utils'
import { RouterModule, Routes, Router, ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-reports',
  templateUrl: './reports.component.html',
  styleUrls: ['./reports.component.css'],
  providers: [DataService]
})
export class ReportsComponent implements OnInit {

  constructor(private dataService: DataService, private router: Router, private route: ActivatedRoute) {

  }

  reports: ReportModel[];

  ngOnInit() {
    this.dataService.getReports()
        .subscribe(res => {
          this.reports = res as Array<ReportModel>;
    });
  }

  navigateToNewReport() {
    this.router.navigate(['../new-report'], { relativeTo: this.route });
  }

  private openReport(report : ReportModel) {
    if(Utils.checkNullEmpty(report.sub_type)) {
      this.router.navigate(['../report', report.type], { relativeTo: this.route });
    } else {
      this.router.navigate(['../report', report.type, report.sub_type], { relativeTo: this.route });
    }
  }

}
