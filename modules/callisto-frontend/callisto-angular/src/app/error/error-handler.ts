import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material';

@Injectable()
export class ErrorHandler {

    constructor(public snackbar: MatSnackBar) {}

    public handleError(err:any) {
        if (err.status == 500) {
            var msg = "Server error: ";
            this.snackbar.open(msg + err.error.message, 'close',
                {
                    duration: 3000
                });
        } else if (err.status == 409) {
            var msg = "Conflict: ";
            this.snackbar.open(msg + err.error.message, 'close',
                {
                    duration: 3000
                });
        }
    }
}