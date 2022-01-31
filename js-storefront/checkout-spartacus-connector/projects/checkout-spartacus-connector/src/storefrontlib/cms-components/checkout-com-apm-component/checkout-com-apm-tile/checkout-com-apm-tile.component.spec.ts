import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import { CheckoutComApmTileComponent } from './checkout-com-apm-tile.component';
import {ApmData, PaymentType} from '../../../../core/model/ApmData';
import {Observable, of} from 'rxjs';
import {CheckoutComApmService} from '../../../../core/services/checkout-com-apm.service';
import createSpy = jasmine.createSpy;

const apm = {code: PaymentType.PayPal};

class CheckoutComApmServiceStub  {
  getSelectedApmFromState = createSpy('getSelectedApmFromState').and.returnValue(of(apm));
  selectApm = createSpy('selectApm').and.stub();
}

describe('CheckoutComApmTileComponent', () => {
  let component: CheckoutComApmTileComponent;
  let fixture: ComponentFixture<CheckoutComApmTileComponent>;
  let checkoutComApmService: CheckoutComApmService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CheckoutComApmTileComponent ],
      providers: [
        { provide: CheckoutComApmService, useClass: CheckoutComApmServiceStub }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CheckoutComApmTileComponent);
    component = fixture.componentInstance;
    component.apm = apm;

    fixture.detectChanges();

    checkoutComApmService = TestBed.inject(CheckoutComApmService);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should handle click event', fakeAsync(()=> {
    fixture.debugElement.nativeElement.click();
    tick();

    expect(checkoutComApmService.selectApm).toHaveBeenCalledWith(
      apm
    );
  }));
});
