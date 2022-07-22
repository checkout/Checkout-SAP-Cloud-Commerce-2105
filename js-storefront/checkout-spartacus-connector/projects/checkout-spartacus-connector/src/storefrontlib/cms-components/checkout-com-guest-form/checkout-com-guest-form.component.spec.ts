import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import {
  AuthService,
  I18nTestingModule,
  RoutingService,
} from '@spartacus/core';
import { FormErrorsModule } from '@spartacus/storefront';
import { UserRegisterFacade } from '@spartacus/user/profile/root';
import { Observable, of } from 'rxjs';
import createSpy = jasmine.createSpy;
import {CheckoutComGuestFormComponent} from "./checkout-com-guest-form.component";

class MockAuthService implements Partial<AuthService> {
  isUserLoggedIn(): Observable<boolean> {
    return of(true);
  }
}

class MockUserRegisterFacade implements Partial<UserRegisterFacade> {
  registerGuest = createSpy();
}

class MockRoutingService implements Partial<RoutingService> {
  go = jasmine.createSpy('go');
}

describe('GuestRegisterFormComponent', () => {
  let component: CheckoutComGuestFormComponent;
  let fixture: ComponentFixture<CheckoutComGuestFormComponent>;

  let userRegisterFacade: UserRegisterFacade;
  let routingService: RoutingService;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [I18nTestingModule, ReactiveFormsModule, FormErrorsModule],
        declarations: [CheckoutComGuestFormComponent],
        providers: [
          { provide: AuthService, useClass: MockAuthService },
          { provide: UserRegisterFacade, useClass: MockUserRegisterFacade },
          { provide: RoutingService, useClass: MockRoutingService },
        ],
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(CheckoutComGuestFormComponent);

    userRegisterFacade = TestBed.inject(UserRegisterFacade);
    routingService = TestBed.inject(RoutingService);

    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should register customer and redirect to homepage when submit', () => {
    const password = 'StrongPass123!@#';
    component.guestRegisterForm.controls['password'].setValue(password);
    component.guestRegisterForm.controls['passwordconf'].setValue(password);
    component.guid = 'guid';
    component.submit();

    expect(userRegisterFacade.registerGuest).toHaveBeenCalledWith(
      'guid',
      password
    );
    expect(routingService.go).toHaveBeenCalledWith({ cxRoute: 'home' });
  });
});
