import { BrowserModule } from '@angular/platform-browser';
import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule, FormControl } from '@angular/forms';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {HttpClientModule} from '@angular/common/http';
import { XHRBackend, RequestOptions, HttpModule} from '@angular/http';
import { HttpService } from './service/http.service'
import { ResultsService } from './service/results.service'
import { QueryService } from './service/query.service'
import { httpServiceFactory } from './factory/HttpServiceFactory'

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
  MatAutocompleteModule, MatSidenavModule, MatListModule
} from '@angular/material';

import { AppComponent } from './app.component';
import { HomeComponent, SaveQueryDialog } from './home/home.component';
import { AppNavbarComponent } from './app-navbar/app-navbar.component';
import { QuerySearchComponent } from './query-search/query-search.component';
import { GraphComponent } from './graph/graph.component';
import { MyNavComponent } from './my-nav/my-nav.component';
import { LayoutModule } from '@angular/cdk/layout';

const appRoutes: Routes = [
  { path: 'home', component: HomeComponent },
  { path: '',
    redirectTo: '/home',
    pathMatch: 'full'
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
    MyNavComponent
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
    RouterModule.forRoot(
      appRoutes,
      {enableTracing: true} // <-- debugging purposes only
    )
  ],
  providers: [ResultsService, QueryService, {
    provide: HttpService,
    useFactory: httpServiceFactory,
    deps: [XHRBackend, RequestOptions]
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
