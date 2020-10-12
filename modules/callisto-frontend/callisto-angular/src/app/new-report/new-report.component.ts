import { Component, OnInit } from '@angular/core';
import { DataService } from '../service/data.service';
import { ReportConfig } from '../model/reportconfig'
import { Utils } from '../util/utils'
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-new-report',
  templateUrl: './new-report.component.html',
  styleUrls: ['./new-report.component.css'],
  providers: [DataService]
})
export class NewReportComponent implements OnInit {

  constructor(public snackBar:MatSnackBar, private dataService: DataService) { }

  metrics = [{key: '', value: ''}];
  reportConfig: ReportConfig = new ReportConfig();

  ngOnInit() {

  }

  deleteMetric(event, index) {
    if(this.metrics.length > index) {
      this.metrics.splice(index,1)
    }
  }

  addEmptyMetric(event) {
    this.metrics.push({key: '', value: ''})
  }

  saveReport(event) {
    this.reportConfig.metrics = {};
    this.metrics
        .filter(e => Utils.checkNotNullEmpty(e.key) && Utils.checkNotNullEmpty(e.value))
        .forEach(e => this.reportConfig.metrics[e.key] = e.value);
    this.dataService.saveReport(this.reportConfig)
      .subscribe(res => {
          this.showSnackbar(res.msg);
        });
  }

  private showSnackbar(msg) {
    this.snackBar.open(msg, 'close', {duration: 2000});
  }
}
