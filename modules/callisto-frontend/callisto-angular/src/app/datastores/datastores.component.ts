import { Component, OnInit } from '@angular/core';
import { Datastore } from '../model/datastore';
import { DataService } from '../service/data.service';
import {Utils} from '../util/utils'
import { QueryService } from '../service/query.service';
import { RouterModule, Routes, Router, ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-datastores',
  templateUrl: './datastores.component.html',
  styleUrls: ['./datastores.component.css'],
  providers: [DataService]
})
export class DatastoresComponent implements OnInit {

  constructor(private dataService:DataService, private router: Router, private route: ActivatedRoute) { }

  datastores : Datastore[] = [];

  ngOnInit() {
    this.dataService.getDatastores().subscribe(response => {
      this.datastores = response as Array<Datastore>;
    });
  }

  private navigateToNewDatastore() {
    this.router.navigate(['new'], { relativeTo: this.route });
  }

}
