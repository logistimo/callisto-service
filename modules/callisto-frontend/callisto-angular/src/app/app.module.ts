import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule, FormControl } from '@angular/forms';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {HttpClientModule} from '@angular/common/http';
import { XHRBackend, RequestOptions, HttpModule} from '@angular/http';
import { HttpService } from './service/http.service'
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
    MatTooltipModule
} from '@angular/material';

import { AppComponent } from './app.component';
import { HomeComponent, SaveQueryDialog } from './home/home.component';
import { AppNavbarComponent } from './app-navbar/app-navbar.component';


@NgModule({
    declarations: [
        HomeComponent,
        SaveQueryDialog,
        AppNavbarComponent,
        AppComponent
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
        MatTooltipModule
    ],
    providers: [{
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
