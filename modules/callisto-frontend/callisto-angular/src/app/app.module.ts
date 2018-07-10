import { BrowserModule } from '@angular/platform-browser';
import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule, FormControl } from '@angular/forms';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {HttpClientModule, HTTP_INTERCEPTORS} from '@angular/common/http';
import { XHRBackend, RequestOptions, HttpModule} from '@angular/http';
import { HttpService } from './service/http.service'
import { ResultsService } from './service/results.service'
import { QuerySharingService } from './service/query-sharing.service'
import { QueryService } from './service/query.service'
import { httpServiceFactory } from './factory/HttpServiceFactory'
import { DomainFilterFilterPipe } from './query/filter.pipe'
import { ErrorHandler } from './error/error-handler'
import { RequestInterceptor } from './error/http-interceptor'

import {
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
  MatDividerModule,
  MatAutocompleteModule, MatSidenavModule, MatListModule, MatPaginatorModule
} from '@angular/material';

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

const appRoutes: Routes = [
  { path: '',
    redirectTo: '/home',
    pathMatch: 'full'
  },
  { path: 'home', component: HomeComponent },
  { path: 'datastores', component: DatastoresComponent },
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
    DomainFilterFilterPipe
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    ReactiveFormsModule,
    FormsModule,
    HttpClientModule,
    HttpModule,

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
    RouterModule.forRoot(
      appRoutes,
      {
        enableTracing: true,
        useHash : true,
        onSameUrlNavigation: 'reload'
      }
    )
  ],
  providers: [ErrorHandler, ResultsService, QuerySharingService, QueryService, {
    provide: HttpService,
    useFactory: httpServiceFactory,
    deps: [XHRBackend, RequestOptions]
  },{
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
