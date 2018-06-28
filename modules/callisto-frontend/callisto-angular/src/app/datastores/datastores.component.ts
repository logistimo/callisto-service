import { Component, OnInit } from '@angular/core';
import { Datastore } from '../model/datastore';


@Component({
  selector: 'app-datastores',
  templateUrl: './datastores.component.html',
  styleUrls: ['./datastores.component.css']
})
export class DatastoresComponent implements OnInit {


  datastores: Datastore[] = [{
    id: "1",
    name: "My cassandra",
    description : ""
  }, {
    id: "2",
    name: "Assets DB",
    description : ""
  }, {
    id: "3",
    name: "Logi DB",
    description : ""
  }];

  constructor() { }

  ngOnInit() {
    console.log("Hello I am here");
  }

}
