import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CheckoutComFramesInputComponent } from './checkout-com-frames-input.component';
import { StoreModule } from '@ngrx/store';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { FrameElementIdentifier } from '../checkout-com-frames-form/interfaces';
import { CSS_CLASS_CARD_NUMBER, CSS_CLASS_CVV, CSS_CLASS_EXPIRY_DATE } from './interfaces';

describe('CheckoutComFramesInputComponent', () => {
  let component: CheckoutComFramesInputComponent;
  let fixture: ComponentFixture<CheckoutComFramesInputComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CheckoutComFramesInputComponent],
      imports: [StoreModule.forRoot({}), ReactiveFormsModule],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CheckoutComFramesInputComponent);
    component = fixture.componentInstance;
    component.fieldName = 'inputName';
    component.fieldType = FrameElementIdentifier.CardNumber;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should add ctrl to the existing form', () => {
    component.form = new FormGroup({});
    fixture.detectChanges();
    expect(component.form.controls[component.fieldName]).toBeTruthy();
  });

  it('should add ctrl to the own form', () => {
    fixture.detectChanges();
    expect(component.form).toBeTruthy();
    expect(component.form.controls[component.fieldName]).toBeTruthy();
  });

  it('should set css class by fieldType', () => {
    fixture.detectChanges();
    const cssClass = component.cssClassByFieldType;
    expect(fixture.nativeElement.getElementsByClassName(cssClass).length).toBeGreaterThanOrEqual(1);
  });

  [
    {cssClass: CSS_CLASS_CARD_NUMBER, fieldType: FrameElementIdentifier.CardNumber},
    {cssClass: CSS_CLASS_CVV, fieldType: FrameElementIdentifier.Cvv},
    {cssClass: CSS_CLASS_EXPIRY_DATE, fieldType: FrameElementIdentifier.ExpiryDate},
    {cssClass: null, fieldType: null},
  ].forEach((parameters) => {
    it(`should map field type ${parameters.fieldType} to css class ${parameters.cssClass}`, () => {
      component.fieldType = parameters.fieldType;
      expect(component.cssClassByFieldType).toEqual(parameters.cssClass);
    })
  })
});
