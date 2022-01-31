package com.checkout.hybris.core.payment.request.strategies.impl;

import com.checkout.hybris.core.enums.OxxoIntegrationType;
import com.checkout.hybris.core.model.CheckoutComOxxoConfigurationModel;
import com.checkout.hybris.core.model.CheckoutComOxxoPaymentInfoModel;
import com.checkout.hybris.core.oxxo.session.request.OxxoPayerRequestDto;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.core.payment.oxxo.service.CheckoutComOxxoPaymentRequestService;
import com.checkout.payments.AlternativePaymentSource;
import com.checkout.payments.PaymentRequest;
import com.checkout.payments.RequestSource;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.OXXO;
import static de.hybris.platform.testframework.Assert.assertEquals;
import static org.mockito.Mockito.when;


@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComOxxoPaymentRequestStrategyTest {

    private static final String ISOCODE = "en";
    private static final String OXXO_TYPE = "oxxo";
    private static final String PAYER_KEY = "payer";
    private static final String COUNTRY_KEY = "country";
    private static final String CURRENCY_ISOCODE = "gbp";
    private static final String DESCRIPTION = "description";
    private static final String DESCRIPTION_KEY = "description";
    private static final String INTEGRATION_TYPE_KEY = "integration_type";

    private static final long AMOUNT = 20L;

    @InjectMocks
    private CheckoutComOxxoPaymentRequestStrategy testObj;

    @Mock
    private CheckoutComOxxoPaymentRequestService checkoutComOxxoPaymentRequestServiceMock;

    @Mock
    private CartModel cartModelMock;
    @Mock
    private CheckoutComOxxoPaymentInfoModel checkoutComOxxoPaymentInfoModelMock;
    @Mock
    private OxxoPayerRequestDto oxxoPayerRequestDtoMock;
    @Mock
    private CheckoutComOxxoConfigurationModel oxxoApmConfigurationMock;
    @Mock
    private AddressModel billingAddressModelMock;
    @Mock
    private CountryModel countryModelMock;

    @Before
    public void setUp() {
        when(checkoutComOxxoPaymentRequestServiceMock.getPayerDto(cartModelMock)).thenReturn(oxxoPayerRequestDtoMock);
        when(checkoutComOxxoPaymentRequestServiceMock.getOxxoApmConfiguration()).thenReturn(Optional.of(oxxoApmConfigurationMock));
        when(oxxoApmConfigurationMock.getDescription()).thenReturn(DESCRIPTION);
        when(oxxoApmConfigurationMock.getIntegrationType()).thenReturn(OxxoIntegrationType.DIRECT);
        when(checkoutComOxxoPaymentInfoModelMock.getType()).thenReturn(OXXO_TYPE);
        when(cartModelMock.getPaymentInfo()).thenReturn(checkoutComOxxoPaymentInfoModelMock);
        when(cartModelMock.getPaymentInfo().getBillingAddress()).thenReturn(billingAddressModelMock);
        when(cartModelMock.getPaymentInfo().getBillingAddress().getCountry()).thenReturn(countryModelMock);
        when(cartModelMock.getPaymentInfo().getBillingAddress().getCountry().getIsocode()).thenReturn(ISOCODE);
    }

    @Test
    public void getRequestSourcePaymentRequest_ShouldSetRequiredAttributes() {
        final PaymentRequest<RequestSource> result = testObj.getRequestSourcePaymentRequest(cartModelMock, CURRENCY_ISOCODE, AMOUNT);

        final AlternativePaymentSource paymentRequestSource = (AlternativePaymentSource) result.getSource();

        assertEquals(paymentRequestSource.get(DESCRIPTION_KEY), DESCRIPTION);
        assertEquals(paymentRequestSource.get(COUNTRY_KEY), ISOCODE);
        assertEquals(paymentRequestSource.get(INTEGRATION_TYPE_KEY), OxxoIntegrationType.DIRECT.getCode());
        assertEquals(paymentRequestSource.get(PAYER_KEY), oxxoPayerRequestDtoMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRequestSourcePaymentRequest_WhenOxxoConfigurationIsMissing_ShouldThrowException() {
        when(checkoutComOxxoPaymentRequestServiceMock.getOxxoApmConfiguration()).thenReturn(Optional.empty());

        testObj.getRequestSourcePaymentRequest(cartModelMock, CURRENCY_ISOCODE, AMOUNT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRequestSourcePaymentRequest_WhenPaymentInfoIsNull_ShouldThrowException() {
        when(cartModelMock.getPaymentInfo()).thenReturn(null);

        testObj.getRequestSourcePaymentRequest(cartModelMock, CURRENCY_ISOCODE, AMOUNT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRequestSourcePaymentRequest_WhenBillingAddressIsNull_ShouldThrowException() {
        when(cartModelMock.getPaymentInfo().getBillingAddress()).thenReturn(null);

        testObj.getRequestSourcePaymentRequest(cartModelMock, CURRENCY_ISOCODE, AMOUNT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRequestSourcePaymentRequest_WhenCountryIsNull_ShouldThrowException() {
        when(cartModelMock.getPaymentInfo().getBillingAddress().getCountry()).thenReturn(null);

        testObj.getRequestSourcePaymentRequest(cartModelMock, CURRENCY_ISOCODE, AMOUNT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRequestSourcePaymentRequest_WhenCountryIsocodeIsEmpty_ShouldThrowException() {
        when(cartModelMock.getPaymentInfo().getBillingAddress().getCountry().getIsocode()).thenReturn(StringUtils.EMPTY);

        testObj.getRequestSourcePaymentRequest(cartModelMock, CURRENCY_ISOCODE, AMOUNT);
    }

    @Test
    public void getStrategyKey_ShouldReturnOxxo() {
        final CheckoutComPaymentType result = testObj.getStrategyKey();

        assertEquals(result, OXXO);
    }
}
