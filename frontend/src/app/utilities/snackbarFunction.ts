import { MatSnackBar } from '@angular/material/snack-bar';

export function openSnackbar(
  snackBar: MatSnackBar,
  message: string,
  action: string
) {
  snackBar.open(message, action);
}
