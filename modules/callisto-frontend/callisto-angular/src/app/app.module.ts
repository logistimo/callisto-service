import { BrowserModule } from '@angular/platform-browser';
import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule} from '@angular/forms';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {HttpClientModule, HTTP_INTERCEPTORS} from '@angular/common/http';
import { ResultsService } from './service/results.service'
import { QuerySharingService } from './service/query-sharing.service'
import { QueryService } from './service/query.service'
import { DomainFilterFilterPipe } from './query/filter.pipe'
import { ErrorHandler } from './error/error-handler'
import { RequestInterceptor } from './error/http-interceptor'

import { CdkTableModule } from '@angular/cdk/table';
import {MatExpansionModule} from '@angular/material/expansion';

import { AppComponent } from './app.component';
import { HomeComponent, SaveQueryDialog } from './home/home.component';
import { AppNavbarComponent } from './app-navbar/app-navbar.component';
import { QuerySearchComponent } from './query-search/query-search.component';
import { GraphComponent } from './graph/graph.component';
import { MyNavComponent } from './my-nav/my-nav.component';
import { LayoutModule } from '@angular/cdk/layout';
import { DatastoresComponent } from './datastores/datastores.component';
import { QueryListingComponent } from './query-listing/query-listing.component';
import { QueryComponent } from './query/query.component';
import { QueriesComponent } from './queries/queries.component';
import { NewQueryComponent } from './new-query/new-query.component';
import { NewDatastoreComponent } from './new-datastore/new-datastore.component';
import { ReportsComponent } from './reports/reports.component';
import { ReportDetailComponent } from './report-detail/report-detail.component';
import { NewReportComponent } from './new-report/new-report.component';
import { DataExplorerComponent } from './data-explorer/data-explorer.component';
import {MatTableModule} from "@angular/material/table";
import {MatInputModule} from "@angular/material/input";
import {MatToolbarModule} from "@angular/material/toolbar";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatSelectModule} from "@angular/material/select";
import {MatIconModule} from "@angular/material/icon";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatListModule} from "@angular/material/list";
import {MatSidenavModule} from "@angular/material/sidenav";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {MatSnackBarModule} from "@angular/material/snack-bar";
import {MatMenuModule} from "@angular/material/menu";
import {MatGridListModule} from "@angular/material/grid-list";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {MatDialogModule} from "@angular/material/dialog";
import {MatCardModule} from "@angular/material/card";
import {MatDividerModule} from "@angular/material/divider";
import {MatTooltipModule} from "@angular/material/tooltip";
import {MatButtonModule} from "@angular/material/button";

const appRoutes: Routes = [
  { path: '',
    redirectTo: '/home',
    pathMatch: 'full'
  },
  { path: 'home', component: HomeComponent },
  {
    path: 'datastores',
    component: DatastoresComponent,
  },
  {
    path: 'reports',
    component: ReportsComponent
  },
  {
    path: 'new-report',
    component: NewReportComponent
  },
  {
    path: 'report/:type',
    component: ReportDetailComponent
  },
  {
    path: 'report/:type/:subtype',
    component: ReportDetailComponent
  },
  {
    path: 'datastores/new',
    component: NewDatastoreComponent,
  },
  {
    path: 'queries',
    component: QueriesComponent,
    children: [
      {
        path: '',
        redirectTo: 'search',
        pathMatch: 'full'
      }, {
        path: 'search',
        component: QueryListingComponent
      }, {
        path: 'query/:id',
        component: QueryComponent
      }, {
        path: 'new',
        component: NewQueryComponent
      }
    ]
  }
];

@NgModule({
  declarations: [
    HomeComponent,
    SaveQueryDialog,
    AppNavbarComponent,
    AppComponent,
    QuerySearchComponent,
    GraphComponent,
    MyNavComponent,
    DatastoresComponent,
    QueryListingComponent,
    QueryComponent,
    QueriesComponent,
    DomainFilterFilterPipe,
    NewQueryComponent,
    NewDatastoreComponent,
    ReportsComponent,
    ReportDetailComponent,
    NewReportComponent,
    DataExplorerComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    ReactiveFormsModule,
    FormsModule,
    HttpClientModule,
    MatTableModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatMenuModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatDialogModule,
    MatTooltipModule,
    MatSnackBarModule,
    MatCardModule,
    MatGridListModule,
    MatAutocompleteModule,
    LayoutModule,
    MatSidenavModule,
    MatListModule,
    MatPaginatorModule,
    MatExpansionModule,
    MatDividerModule,
    CdkTableModule,
    RouterModule.forRoot(
      appRoutes,
      {
        enableTracing: true,
        useHash : true,
        onSameUrlNavigation: 'reload'
      }
    )
  ],
  providers: [ErrorHandler, ResultsService, QuerySharingService, QueryService,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: RequestInterceptor,
      multi: true,
    }],
  bootstrap: [AppComponent],
  entryComponents: [
    HomeComponent,
    SaveQueryDialog,
    AppNavbarComponent,
    AppComponent
  ]
})
export class AppModule {
}
