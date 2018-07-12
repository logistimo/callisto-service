import { Component, OnInit } from '@angular/core';
import { RouterModule, Routes, Router, ActivatedRoute, NavigationEnd, DefaultUrlSerializer} from '@angular/router';
import { QuerySharingService } from '../service/query-sharing.service'
import { Utils } from '../util/utils'
import { QueryText } from '../model/querytext'
import {NewQueryComponent} from '../new-query/new-query.component'
import {QueryListingComponent} from '../query-listing/query-listing.component'

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
          }
        });
  }

  updatePathDisplay(subpath) {
    const delimiter = Utils.checkNotNullEmpty(subpath) ? " / " : ''
    this.path = delimiter + subpath;
  }

  componentAdded(event) {
    if(event instanceof NewQueryComponent) {
      this.updatePathDisplay('New query');
    } else if(event instanceof QueryListingComponent) {
      this.updatePathDisplay('');
    }
  }

}
