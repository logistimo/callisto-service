import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
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
    MatDialogModule
} from '@angular/material';

import { AppComponent } from './app.component';
import { HomeComponent } from './home/home.component';
import { AppNavbarComponent } from './app-navbar/app-navbar.component';


@NgModule({
    declarations: [
        AppComponent,
        HomeComponent,
        AppNavbarComponent
    ],
    imports: [
        BrowserModule,
        BrowserAnimationsModule,

        MatCheckboxModule,
        MatFormFieldModule,
        MatInputModule,
        MatSelectModule,
        MatMenuModule,
        MatToolbarModule,
        MatButtonModule,
        MatIconModule,
        MatProgressSpinnerModule,
        MatDialogModule
    ],
    providers: [],
    bootstrap: [AppComponent]
})
export class AppModule {
}
