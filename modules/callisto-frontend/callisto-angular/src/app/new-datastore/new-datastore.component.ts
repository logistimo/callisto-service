import { Component, OnInit } from '@angular/core';
import { Datastore } from '../model/datastore'
import {ReactiveFormsModule, FormControl, FormsModule} from '@angular/forms';
import {Utils} from '../util/utils'
import { DataService } from '../service/data.service';
import { MatSnackBar } from '@angular/material';

@Component({
  selector: 'app-new-datastore',
  templateUrl: './new-datastore.component.html',
  styleUrls: ['./new-datastore.component.css'],
  providers: [DataService]
})
export class NewDatastoreComponent implements OnInit {

  private datastoreIdField : FormControl = new FormControl();
  private datastoreModel : Datastore;
  private datastoreIdUnavailable = true;
  private searchedDatastore;
  private _hostsCsv;

  constructor(public snackBar:MatSnackBar, private dataService:DataService) { }

  ngOnInit() {
    this.datastoreModel = new Datastore();
    this.checkDatastoreIdAvailability();
  }

  private checkDatastoreIdAvailability() {
    this.datastoreIdField.valueChanges
        .debounceTime(400)
        .distinctUntilChanged()
        .filter(term => {
          this.datastoreIdUnavailable = Utils.checkNullEmpty(term) ? true : this.datastoreIdUnavailable;
          if(Utils.checkNullEmpty(term)) {
            this.updateDatastoreModel(new Datastore());
          }
          return Utils.checkNotNullEmpty(term);
        })
        .map(datastoreId => this.dataService.getDatastore(datastoreId))
        .subscribe(res => {
          res.subscribe(datastore => {
            if(Utils.checkNotNullEmpty(datastore)) {
              this.datastoreIdUnavailable = true;
              this.updateDatastoreModel(datastore);
            } else {
              this.datastoreIdUnavailable = false;
            }
            this.datastoreIdUnavailable = Utils.checkNotNullEmpty(datastore);
            this.searchedDatastore = datastore;
          })
        });
  }

  private updateDatastoreModel(datastore: Datastore) {
    if(Utils.checkNullEmpty(datastore.hosts)) {
      this._hostsCsv = '';
    }else {
      this._hostsCsv = datastore.hosts.join(", ");
    }
    this.datastoreModel = datastore;
  }

  private getHostsArrayFromCsv() {
    const hosts = [];
    this._hostsCsv.split(",").forEach(host => {
          hosts.push(host.trim())
        }
    );
    return hosts;
  }

  private saveDatastore(event, datastoreModel: Datastore) {
    datastoreModel.hosts = this.getHostsArrayFromCsv();
    this.dataService.saveDatastore(datastoreModel)
        .subscribe(data => {
          this.showSnackbar(data.msg);
        });
  }

  private showSnackbar(msg) {
    this.snackBar.open(msg, 'close', {duration: 2000});
  }


}
