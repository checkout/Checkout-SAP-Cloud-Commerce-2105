package com.checkout.hybris.facades.hooks;

import com.checkout.hybris.core.model.CheckoutComACHConsentModel;
import com.checkout.hybris.core.model.CheckoutComAchPaymentInfoModel;
import com.checkout.hybris.core.model.CheckoutComOxxoPaymentInfoModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commerceservices.service.data.CommerceCheckoutParameter;
import de.hybris.platform.commerceservices.service.data.CommerceOrderResult;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComACHConsentPlaceOrderHookTest {

	@InjectMocks
	private DefaultCheckoutComACHConsentPlaceOrderHook testObj;

	@Mock
	private ModelService modelServiceMock;
	private final CommerceCheckoutParameter commerceCheckoutParameter = new CommerceCheckoutParameter();
	private final CommerceOrderResult commerceOrderResult = new CommerceOrderResult();
	private final CartModel cart = new CartModel();
	private final CheckoutComACHConsentModel consent = new CheckoutComACHConsentModel();
	private PaymentInfoModel paymentInfo;
	private final OrderModel order = new OrderModel();
	
	@Test
	public void afterPlaceOrder_shouldDoNothing_WhenPaymentInfoIsNotOfClassCheckoutComAchPaymentInfoModel() throws InvocationTargetException, NoSuchMethodException, InstantiationException,
			IllegalAccessException {
		ensureCartIsAssociatedWithCommerceCheckoutParameter();
		ensureOrderModelIsAssociatedWithCommerceOrderResult();
		associateConsent(cart, consent);
		setPaymentInfoOfType(CheckoutComOxxoPaymentInfoModel.class);

		testObj.afterPlaceOrder(commerceCheckoutParameter, commerceOrderResult);

		verifyZeroInteractions(modelServiceMock);
	}

	@Test
	public void afterPlaceOrder_shouldLinkAchConsentToOrder_WhenPaymentInfoIsOfCheckoutComAchPaymentInfoModelClass() throws InvocationTargetException, NoSuchMethodException, InstantiationException,
			IllegalAccessException {
		ensureCartIsAssociatedWithCommerceCheckoutParameter();
		ensureOrderModelIsAssociatedWithCommerceOrderResult();
		associateConsent(cart, consent);
		setPaymentInfoOfType(CheckoutComAchPaymentInfoModel.class);

		testObj.afterPlaceOrder(commerceCheckoutParameter, commerceOrderResult);

		assertThat(order.getAchConsent()).isEqualTo(consent);
		assertThat(consent.getOrder()).isEqualTo(order);
		verify(modelServiceMock).save(order);
		verify(modelServiceMock).save(consent);
	}

	@Test
	public void beforePlaceOrder_shouldRemoveConsent_WhenPaymentInfoIsNotAchPayment() throws InvalidCartException,
			InvocationTargetException, NoSuchMethodException,
			InstantiationException, IllegalAccessException {
		ensureCartIsAssociatedWithCommerceCheckoutParameter();
		associateConsent(cart, consent);
		setPaymentInfoOfType(CheckoutComOxxoPaymentInfoModel.class);

		testObj.beforePlaceOrder(commerceCheckoutParameter);

		verify(modelServiceMock).remove(consent);
	}

	@Test
	public void beforePlaceOrder_shouldDoNothing_WhenPaymentInfoIsAchPayment() throws InvalidCartException,
			InvocationTargetException, NoSuchMethodException,
			InstantiationException, IllegalAccessException {
		ensureCartIsAssociatedWithCommerceCheckoutParameter();
		associateConsent(cart, consent);
		setPaymentInfoOfType(CheckoutComAchPaymentInfoModel.class);

		testObj.beforePlaceOrder(commerceCheckoutParameter);

		verifyZeroInteractions(modelServiceMock);
	}

	private void associateConsent(final AbstractOrderModel order, final CheckoutComACHConsentModel consent) {
		order.setAchConsent(consent);
	}

	private void ensureCartIsAssociatedWithCommerceCheckoutParameter() {
		commerceCheckoutParameter.setCart(cart);
	}

	private void setPaymentInfoOfType(final Class paymentInfoModelClass) throws NoSuchMethodException,
			InvocationTargetException, InstantiationException, IllegalAccessException {
		paymentInfo = (PaymentInfoModel) paymentInfoModelClass.getDeclaredConstructor().newInstance();
		cart.setPaymentInfo(paymentInfo);
	}

	private void ensureOrderModelIsAssociatedWithCommerceOrderResult() {
		commerceOrderResult.setOrder(order);
	}
}
