package com.checkout.hybris.facades.payment.converters.populators;

import com.checkout.hybris.facades.beans.GooglePayMerchantConfigurationData;
import com.checkout.hybris.facades.beans.GooglePaySettingsData;
import com.checkout.hybris.facades.beans.GooglePayTransactionInfoData;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComGooglePayPaymentRequestPopulatorTest {

    private static final String FORMAT = "FULL";
    private static final String GOOGLE_PAY_SETTINGS_TYPE = "type";
    private static final Set<String> ALLOWED_AUTH_METHODS = Set.of("am1", "am2");
    private static final Set<String> ALLOWED_CARD_NETWORKS = Set.of("cn1", "cn2");
    private static final String ENVIRONMENT = "env";
    private static final String GATEWAY = "gateway";
    private static final String GATEWAY_MERCHANT_ID = "gatewayMerchantId";
    private static final String MERCHANT_NAME = "merchantName";
    private static final String MERCHANT_ID = "merchantId";

    @InjectMocks
    private CheckoutComGooglePayPaymentRequestPopulator testObj;

    @Mock
    private CartFacade cartFacadeMock;
    @Mock
    private Converter<CartData, GooglePayTransactionInfoData> checkoutComGooglePayTransactionInfoConverter;

    @Mock
    private CartData cartDataMock;

    private final GooglePaySettingsData source = new GooglePaySettingsData();
    private final GooglePayMerchantConfigurationData target = new GooglePayMerchantConfigurationData();


    @Test(expected = IllegalArgumentException.class)
    public void populate_WhenSourceIsNull_ShouldThrowException() {
        testObj.populate(null, target);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_WhenTargetIsNull_ShouldThrowException() {
        testObj.populate(source, null);
    }

    @Test
    public void populate_WhenSourceAndTargetAreNotNull_ShouldPopulateTheTarget() {
        when(cartFacadeMock.getSessionCart()).thenReturn(cartDataMock);

        source.setType(GOOGLE_PAY_SETTINGS_TYPE);
        source.setAllowedAuthMethods(ALLOWED_AUTH_METHODS);
        source.setAllowedCardNetworks(ALLOWED_CARD_NETWORKS);
        source.setEnvironment(ENVIRONMENT);
        source.setGateway(GATEWAY);
        source.setGatewayMerchantId(GATEWAY_MERCHANT_ID);
        source.setMerchantName(MERCHANT_NAME);
        source.setMerchantId(MERCHANT_ID);

        testObj.populate(source, target);

        verify(checkoutComGooglePayTransactionInfoConverter).convert(cartDataMock);

        assertThat(target.getBaseCardPaymentMethod().getType()).isEqualTo(GOOGLE_PAY_SETTINGS_TYPE);
        assertThat(target.getBaseCardPaymentMethod().getParameters().getAllowedAuthMethods()).isEqualTo(ALLOWED_AUTH_METHODS);
        assertThat(target.getBaseCardPaymentMethod().getParameters().getAllowedCardNetworks()).isEqualTo(ALLOWED_CARD_NETWORKS);
        assertThat(target.getBaseCardPaymentMethod().getParameters().getBillingAddressRequired()).isEqualTo(Boolean.TRUE);
        assertThat(target.getBaseCardPaymentMethod().getParameters().getBillingAddressParameters().getFormat()).isEqualTo(FORMAT);
        assertThat(target.getClientSettings().getEnvironment()).isEqualTo(ENVIRONMENT);
        assertThat(target.getGateway()).isEqualTo(GATEWAY);
        assertThat(target.getGatewayMerchantId()).isEqualTo(GATEWAY_MERCHANT_ID);
        assertThat(target.getMerchantName()).isEqualTo(MERCHANT_NAME);
        assertThat(target.getMerchantId()).isEqualTo(MERCHANT_ID);
        assertThat(target.getGatewayMerchantId()).isEqualTo(GATEWAY_MERCHANT_ID);
    }
}
