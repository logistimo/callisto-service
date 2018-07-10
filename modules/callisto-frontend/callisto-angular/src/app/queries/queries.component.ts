import { Component, OnInit } from '@angular/core';
import { RouterModule, Routes, Router, ActivatedRoute, NavigationEnd, DefaultUrlSerializer} from '@angular/router';
import { QuerySharingService } from '../service/query-sharing.service'
import { Utils } from '../util/utils'
import { QueryText } from '../model/querytext'

@Component({
  selector: 'app-queries',
  templateUrl: './queries.component.html',
  styleUrls: ['./queries.component.css']
})
export class QueriesComponent implements OnInit {

  private path: string;
  constructor(private router: Router, private route: ActivatedRoute, private querySharingService : QuerySharingService) {

  }

  ngOnInit() {
    this.querySharingService.currentResult
        .subscribe(res => {
          if (Utils.checkNotNullEmpty(res)) {
            const queryText = res as QueryText;
            this.updatePathDisplay(queryText.query_id)
          } else {
            this.updatePathDisplay('');
          }
        });
  }

  updatePathDisplay(subpath) {
    const delimiter = Utils.checkNotNullEmpty(subpath) ? " / " : ''
    this.path = delimiter + subpath;
  }

}
