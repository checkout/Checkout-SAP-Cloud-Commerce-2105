package com.checkout.hybris.core.payment.request.strategies.impl;

import com.checkout.hybris.core.model.CheckoutComAPMPaymentInfoModel;
import com.checkout.payments.AlternativePaymentSource;
import com.checkout.payments.PaymentRequest;
import com.checkout.payments.RequestSource;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.KNET;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComKnetPaymentRequestStrategyTest {

    private static final String CURRENCY_ISO_CODE = "USD";
    private static final Long CHECKOUT_COM_TOTAL_PRICE = 10000L;
    private static final String LANGUAGE_KEY = "language";
    private static final String EN_LANGUAGE_VALUE = "en";

    @InjectMocks
    private CheckoutComKnetPaymentRequestStrategy testObj;

    @Mock
    private CartModel cartMock;
    @Mock
    private CheckoutComAPMPaymentInfoModel checkoutComAPMPaymentInfoMock;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private CommonI18NService commonI18NServiceMock;

    @Test
    public void getRequestSourcePaymentRequest_WhenKnetPayment_ShouldCreateAlternativePaymentRequestWithTypeAndLanguage() {
        when(cartMock.getPaymentInfo()).thenReturn(checkoutComAPMPaymentInfoMock);
        when(checkoutComAPMPaymentInfoMock.getType()).thenReturn(KNET.name());
        when(commonI18NServiceMock.getCurrentLanguage().getIsocode()).thenReturn(EN_LANGUAGE_VALUE);

        final PaymentRequest<RequestSource> result = testObj.getRequestSourcePaymentRequest(cartMock, CURRENCY_ISO_CODE, CHECKOUT_COM_TOTAL_PRICE);

        assertEquals(KNET.name().toLowerCase(), result.getSource().getType());
        assertEquals(EN_LANGUAGE_VALUE, ((AlternativePaymentSource) result.getSource()).get(LANGUAGE_KEY));
    }

    @Test
    public void getStrategyKey_WhenKnet_ShouldReturnKnetType() {
        assertEquals(KNET, testObj.getStrategyKey());
    }
}
