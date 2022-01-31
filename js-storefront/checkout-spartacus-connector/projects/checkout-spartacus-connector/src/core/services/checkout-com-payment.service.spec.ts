import { TestBed } from '@angular/core/testing';

import { CheckoutComPaymentService } from './checkout-com-payment.service';
import { Store, StoreModule } from '@ngrx/store';
import * as CheckoutComActions from './../store/checkout-com.actions';
import { Address, UserIdService } from '@spartacus/core';
import { Observable, of } from 'rxjs';
import { CHECKOUT_COM_FEATURE } from '../store/checkout-com.state';
import { reducer } from '../store/checkout-com.reducer';
import { HttpErrorResponse } from '@angular/common/http';
import { catchError } from 'rxjs/operators';
import { ApmPaymentDetails, CheckoutComPaymentDetails } from '../../storefrontlib/interfaces';
import { PaymentType } from '../model/ApmData';

const userId = 'current';
const cartId = 'erik';

class MockUserIdService implements Partial<UserIdService> {
  getUserId() {
    return of(userId);
  }
}

const err: HttpErrorResponse = {
  headers: undefined,
  name: 'HttpErrorResponse',
  ok: false,
  statusText: '',
  type: undefined,
  url: undefined,
  message: 'Woops gone wrong',
  status: 500,
  error: 'woops',
};

describe('CheckoutComPaymentService', () => {
  let service: CheckoutComPaymentService;
  let checkoutComStore;
  let userIdService: UserIdService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        StoreModule.forRoot({}),
        StoreModule.forFeature(CHECKOUT_COM_FEATURE, reducer),
      ],
      providers: [
        {provide: UserIdService, useClass: MockUserIdService},
      ],
    });

    service = TestBed.inject(CheckoutComPaymentService);

    checkoutComStore = TestBed.inject(Store);
    userIdService = TestBed.inject(UserIdService);

    spyOn(checkoutComStore, 'dispatch').and.callThrough();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('merchant key', () => {
    it('should get merchant key from state', (done) => {
      const key = 'pk_1234123121';
      checkoutComStore.dispatch(
        new CheckoutComActions.SetOccMerchantKeySuccess(key)
      );

      service.getOccMerchantKeyFromState()
        .subscribe(merchantKey => {
          expect(key).toEqual(key);
          done();
        })
        .unsubscribe();
    });

    it('should dispatch SetOccMerchantKey', () => {
      service.requestOccMerchantKey('current');

      expect(checkoutComStore.dispatch).toHaveBeenCalledWith(
        new CheckoutComActions.SetOccMerchantKey({userId: 'current'})
      );
    });
  });

  describe('canSaveCard', () => {
    it('should allow to save card when not guest checkout', () => {
      expect(service.canSaveCard('current')).toBeTrue();
    });

    it('should not allow to save card when guest checkout', () => {
      spyOn(userIdService, 'getUserId').and.returnValue(of('anonymous'));
      expect(service.canSaveCard('anonymous')).toBeFalse();
    });
  });

  describe('Payment details', () => {

    const paymentDetails: CheckoutComPaymentDetails = {
      cardBin: 'xxxx',
      accountHolderName: 'Erik Slagter',
      cardNumber: '4242424242424242',
      cvn: '100',
      expiryYear: '2022',
      expiryMonth: '12'
    };

    it('should throw exception on failure', (done) => {

      checkoutComStore.dispatch(
        new CheckoutComActions.CreatePaymentDetailsFail(err)
      );

      service.getPaymentDetailsFromState()
        .pipe(catchError(() => {
          done();
          return of(false);
        }))
        .subscribe(res => {
          expect(res).toBeFalse();
        }).unsubscribe();
    });

    it('should handle payment details', () => {

      checkoutComStore.dispatch(
        new CheckoutComActions.CreatePaymentDetailsSuccess(paymentDetails)
      );

      service.getPaymentDetailsFromState()
        .pipe(catchError(() => {
          fail();
          return of(false);
        }))
        .subscribe(res => {
          expect(res).toEqual(paymentDetails);
        });
    });

    it('should dispatch event on create', () => {
      service.createPaymentDetails(paymentDetails, userId, cartId);

      expect(checkoutComStore.dispatch).toHaveBeenCalledWith(
        new CheckoutComActions.CreatePaymentDetails({
          paymentDetails, cartId, userId
        })
      );
    });
  });

  describe('APM payment details', () => {
    const paymentDetails: ApmPaymentDetails = {
      type: PaymentType.iDeal,
      bic: 'INGNL2B',
    };

    it('should dispatch event on create apm payment details', () => {
      service.createApmPaymentDetails(paymentDetails, cartId, userId);

      expect(checkoutComStore.dispatch).toHaveBeenCalledWith(
        new CheckoutComActions.CreateApmPaymentDetails({
          paymentDetails, userId, cartId
        })
      );
    });
  });

  describe('payment address', () => {
    it('should return address from state', (done) => {
      const address: Address = {
        firstName: 'erik',
        lastName: 'slagter',
      };

      checkoutComStore.dispatch(
        new CheckoutComActions.SetPaymentAddressSuccess(address)
      );

      service.getPaymentAddressFromState()
        .pipe(catchError(() => {
          fail();
          return of(false)
        }))
        .subscribe(res => {
          expect(res).toEqual(address);
          done();
        }).unsubscribe();
    })
  });
});
