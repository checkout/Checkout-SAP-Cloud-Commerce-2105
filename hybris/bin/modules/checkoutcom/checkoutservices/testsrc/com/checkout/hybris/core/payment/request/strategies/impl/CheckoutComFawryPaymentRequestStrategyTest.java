package com.checkout.hybris.core.payment.request.strategies.impl;

import com.checkout.hybris.core.apm.services.CheckoutComAPMConfigurationService;
import com.checkout.hybris.core.model.CheckoutComFawryConfigurationModel;
import com.checkout.hybris.core.model.CheckoutComFawryPaymentInfoModel;
import com.checkout.payments.AlternativePaymentSource;
import com.checkout.payments.PaymentRequest;
import com.checkout.payments.RequestSource;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.CustomerModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.FAWRY;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComFawryPaymentRequestStrategyTest {

    private static final String SITE_NAME = "siteName";
    private static final String CURRENCY_ISO_CODE = "USD";
    private static final String MOBILE_NUMBER_VALUE = "12345678901";
    private static final String CUSTOMER_EMAIL_VALUE = "test@test.com";
    private static final Long CHECKOUT_COM_TOTAL_PRICE = 10000L;
    private static final String PRODUCT_ID_VALUE = "productId";

    private static final String MOBILE_NUMBER_KEY = "customer_mobile";
    private static final String CUSTOMER_EMAIL_KEY = "customer_email";
    private static final String PRODUCTS_PRODUCT_ID_KEY = "product_id";
    private static final String PRODUCTS_QUANTITY_KEY = "quantity";
    private static final String PRODUCTS_PRICE_KEY = "price";
    private static final String DESCRIPTION_KEY = "description";
    private static final String PRODUCTS_KEY = "products";
    private static final String PRODUCT_DESCRIPTION_VALUE = "product description";

    @InjectMocks
    private CheckoutComFawryPaymentRequestStrategy testObj;

    @Mock
    private CartModel cartMock;
    @Mock
    private CheckoutComFawryPaymentInfoModel fawryPaymentInfoMock;
    @Mock
    private CustomerModel customerMock;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private CMSSiteService cmsSiteServiceMock;
    @Mock
    private CheckoutComAPMConfigurationService checkoutComAPMConfigurationServiceMock;
    @Mock
    private CheckoutComFawryConfigurationModel fawryApmConfigurationMock;

    @Before
    public void setUp() {
        when(cartMock.getUser()).thenReturn(customerMock);
        when(customerMock.getContactEmail()).thenReturn(CUSTOMER_EMAIL_VALUE);
        when(cmsSiteServiceMock.getCurrentSite().getName()).thenReturn(SITE_NAME);
        when(cartMock.getPaymentInfo()).thenReturn(fawryPaymentInfoMock);
        when(fawryPaymentInfoMock.getType()).thenReturn(FAWRY.name());
        when(fawryPaymentInfoMock.getMobileNumber()).thenReturn(MOBILE_NUMBER_VALUE);
        when(checkoutComAPMConfigurationServiceMock.getApmConfigurationByCode(FAWRY.name())).thenReturn(Optional.of(fawryApmConfigurationMock));
        when(fawryApmConfigurationMock.getProductId()).thenReturn(PRODUCT_ID_VALUE);
        when(fawryApmConfigurationMock.getProductDescription()).thenReturn(PRODUCT_DESCRIPTION_VALUE);
    }

    @Test
    public void getRequestSourcePaymentRequest_WhenFawryPayment_ShouldCreateAlternativePaymentRequestWithRequiredAttributes() {
        final PaymentRequest<RequestSource> result = testObj.getRequestSourcePaymentRequest(cartMock, CURRENCY_ISO_CODE, CHECKOUT_COM_TOTAL_PRICE);

        assertEquals(FAWRY.name().toLowerCase(), result.getSource().getType());
        final AlternativePaymentSource source = (AlternativePaymentSource) result.getSource();
        assertEquals(MOBILE_NUMBER_VALUE, source.get(MOBILE_NUMBER_KEY));
        assertEquals(CUSTOMER_EMAIL_VALUE, source.get(CUSTOMER_EMAIL_KEY));
        assertEquals(SITE_NAME, source.get(DESCRIPTION_KEY));

        final HashMap products = (HashMap) ((List) source.get(PRODUCTS_KEY)).get(0);
        assertEquals(PRODUCT_ID_VALUE, products.get(PRODUCTS_PRODUCT_ID_KEY));
        assertEquals(1, products.get(PRODUCTS_QUANTITY_KEY));
        assertEquals(CHECKOUT_COM_TOTAL_PRICE, products.get(PRODUCTS_PRICE_KEY));
        assertEquals(PRODUCT_DESCRIPTION_VALUE, products.get(DESCRIPTION_KEY));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRequestSourcePaymentRequest_WhenFawryConfigurationIsMissing_ShouldThrowException() {
        when(checkoutComAPMConfigurationServiceMock.getApmConfigurationByCode(FAWRY.name())).thenReturn(Optional.empty());

        testObj.getRequestSourcePaymentRequest(cartMock, CURRENCY_ISO_CODE, CHECKOUT_COM_TOTAL_PRICE);
    }

    @Test
    public void getStrategyKey_WhenFawry_ShouldReturnFawryType() {
        assertEquals(FAWRY, testObj.getStrategyKey());
    }
}
