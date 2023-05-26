package com.checkout.hybris.facades.payment.ach.impl;

import com.checkout.hybris.facades.beans.AchBankInfoDetailsData;
import com.checkout.hybris.facades.payment.ach.magicvalues.MagicPostalCodeValueAchCheckoutStrategy;
import com.checkout.hybris.facades.payment.ach.magicvalues.MagicPostalCodeValuesAchCheckoutStrategyFactory;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.order.CartService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class MagicValuesCheckoutComAchFacadeTest {

	private static final String PC_00001 = "PC00001";
	@Spy
	@InjectMocks
	private MagicValuesCheckoutComAchFacade testObj;

	@Mock
	private CartService cartServiceMock;

	@Mock
	private MagicPostalCodeValuesAchCheckoutStrategyFactory magicPostalCodeValuesAchCheckoutStrategyFactory;

	@Mock
	private MyMagicPostalCodeStrategy myMagicPostalCodeStrategyMock;

	@Test
	public void setPaymentInfoAchToCart_shouldCallStrategyWhenStrategyByPostalCodeIsFound() {
		preparingCartWithValidValues();
		ensuringStrategyIsFound();
		ensuringSuperCallToSetPaymentInfoDoesNothing();

		testObj.setPaymentInfoAchToCart(new AchBankInfoDetailsData());

		verify(myMagicPostalCodeStrategyMock).createAchBankInfoDetailsData();
	}

	@Test
	public void setPaymentInfoAchToCart_shouldNotCallStrategyWhenStrategyByPostalCodeIsNotFound() {
		preparingCartWithValidValues();
		ensuringStrategyIsNotFound();
		ensuringSuperCallToSetPaymentInfoDoesNothing();

		testObj.setPaymentInfoAchToCart(new AchBankInfoDetailsData());

		verifyZeroInteractions(myMagicPostalCodeStrategyMock);
	}

	private void ensuringSuperCallToSetPaymentInfoDoesNothing() {
		doNothing().when(testObj).callSuperSetPaymentInfoAchToCart(any(AchBankInfoDetailsData.class));
	}

	private void ensuringStrategyIsFound() {
		when(magicPostalCodeValuesAchCheckoutStrategyFactory.findStrategy(PC_00001)).thenReturn(
				Optional.of(myMagicPostalCodeStrategyMock));
	}

	private void ensuringStrategyIsNotFound() {
		when(magicPostalCodeValuesAchCheckoutStrategyFactory.findStrategy(PC_00001)).thenReturn(
				Optional.empty());
	}

	private void preparingCartWithValidValues() {
		final CartModel cartModel = new CartModel();
		final AddressModel addressModel = new AddressModel();
		addressModel.setPostalcode(PC_00001);
		cartModel.setDeliveryAddress(addressModel);
		when(cartServiceMock.getSessionCart()).thenReturn(cartModel);
	}

	public class MyMagicPostalCodeStrategy implements MagicPostalCodeValueAchCheckoutStrategy {
		@Override
		public AchBankInfoDetailsData createAchBankInfoDetailsData() {
			return null;
		}

		@Override
		public boolean isApplicable(final String postalCode) {
			return false;
		}
	}
}
