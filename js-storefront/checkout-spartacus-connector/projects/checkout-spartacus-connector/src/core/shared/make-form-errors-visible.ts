import { FormGroup } from '@angular/forms';

export function makeFormErrorsVisible(form: FormGroup) {
  try {
    if (!form || !form.controls) {
      return;
    }
    for (const ctrlName in form.controls) {
      if (form.controls.hasOwnProperty(ctrlName)) {
        const ctrl = form.controls[ctrlName];
        if (ctrl instanceof FormGroup) {
          makeFormErrorsVisible(ctrl);
        } else {
          ctrl.markAsTouched();
          ctrl.markAsDirty();
          ctrl.updateValueAndValidity();
        }
      }
    }
  } catch (e) {}
}
