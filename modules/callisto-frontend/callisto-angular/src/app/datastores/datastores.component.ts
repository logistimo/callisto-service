import { Component, OnInit } from '@angular/core';
import { Datastore } from '../model/datastore';
import { DataService } from '../service/data.service';
import {Utils} from '../util/utils'
import { QueryService } from '../service/query.service';

@Component({
  selector: 'app-datastores',
  templateUrl: './datastores.component.html',
  styleUrls: ['./datastores.component.css'],
  providers: [DataService]
})
export class DatastoresComponent implements OnInit {

  constructor(private dataService:DataService, private queryService: QueryService) { }

  datastores : Datastore[] = [];

  ngOnInit() {
    this.dataService.getDatastores().subscribe((response:Response) => {
      var _dbs = this.datastores;
      let datastores = JSON.parse(response['_body']);
      datastores.forEach(function (datastore: Datastore) {
        _dbs.push(datastore);
      });
    });
  }

}
