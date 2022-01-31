import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CheckoutComOrderConfirmationThankYouMessageComponent } from './checkout-com-order-confirmation-thank-you-message.component';
import { I18nTestingModule, MockTranslatePipe, Order, ORDER_TYPE } from '@spartacus/core';
import { Observable, of } from 'rxjs';
import { CheckoutFacade } from '@spartacus/checkout/root';

class MockCheckoutFacade {
  clearCheckoutData(){}

  getOrderDetails(): Observable<Order> {
    return of({
      code: 'test-code-412',
      guid: 'guid',
      guestCustomer: true,
      paymentInfo: { billingAddress: { email: 'test@test.com' } },
      replenishmentOrderCode: 'test-repl-code',
    });
  }

  getCurrentOrderType(): Observable<ORDER_TYPE> {
    return of(ORDER_TYPE.PLACE_ORDER);
  }
}

describe('CheckoutComOrderConfirmationThankYouMessageComponent', () => {
  let component: CheckoutComOrderConfirmationThankYouMessageComponent;
  let fixture: ComponentFixture<CheckoutComOrderConfirmationThankYouMessageComponent>;
  let checkoutService: CheckoutFacade;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CheckoutComOrderConfirmationThankYouMessageComponent, MockTranslatePipe],
      providers: [
        I18nTestingModule,
        {provide: CheckoutFacade, useClass: MockCheckoutFacade}
      ]
    })
      .compileComponents();

    checkoutService = TestBed.inject(CheckoutFacade);
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CheckoutComOrderConfirmationThankYouMessageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create without QR code', () => {
    expect(component).toBeTruthy();

    const qrCodeElement = fixture.nativeElement.querySelector('ngx-qrcode');

    expect(qrCodeElement).toBeNull();

    const overlay = fixture.nativeElement.querySelector('cx-spinner');
    expect(overlay).toBeNull();
  });

  it('should show BenefitPay QR code', () => {
    spyOn(checkoutService, 'getOrderDetails').and.returnValue(of({
      code: '1234',
      qrCodeData: 'somedata'
    } as Order));

    fixture = TestBed.createComponent(CheckoutComOrderConfirmationThankYouMessageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    const qrCodeElement = fixture.nativeElement.querySelector('ngx-qrcode');
    expect(qrCodeElement).toBeTruthy();
  });

  it('should show spinner when order has not been received', () => {
    spyOn(checkoutService, 'getOrderDetails').and.returnValue(of({
    }));

    fixture = TestBed.createComponent(CheckoutComOrderConfirmationThankYouMessageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    const overlay = fixture.nativeElement.querySelector('cx-spinner');
    expect(overlay).not.toBeNull();
  });

  it('should clear checkout data when thank you message is destroyed', () => {
    spyOn(checkoutService, 'clearCheckoutData').and.stub();

    component.ngOnDestroy();

    expect(checkoutService.clearCheckoutData).toHaveBeenCalled();
  });
});
