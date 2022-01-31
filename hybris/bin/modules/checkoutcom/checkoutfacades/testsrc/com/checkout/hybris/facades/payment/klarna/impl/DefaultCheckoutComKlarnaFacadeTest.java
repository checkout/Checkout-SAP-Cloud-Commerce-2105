package com.checkout.hybris.facades.payment.klarna.impl;

import com.checkout.hybris.core.klarna.session.request.KlarnaSessionRequestDto;
import com.checkout.hybris.core.klarna.session.response.KlarnaPaymentMethodCategoryDto;
import com.checkout.hybris.core.klarna.session.response.KlarnaSessionResponseDto;
import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.model.CheckoutComKlarnaConfigurationModel;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentIntegrationService;
import com.checkout.hybris.facades.beans.KlarnaClientTokenData;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComKlarnaFacadeTest {

    private static final String CLIENT_TOKEN = "client_token";
    private static final String PAY_LATER = "pay_later";
    private static final String PAY_OVER_TIME = "pay_over_time";
    private static final String INSTANCE_ID = "instance_id";

    @InjectMocks
    private DefaultCheckoutComKlarnaFacade testObj;

    @Mock
    private CheckoutComPaymentIntegrationService checkoutComPaymentIntegrationServiceMock;
    @Mock
    private Converter<CartModel, KlarnaSessionRequestDto> checkoutComKlarnaSessionRequestDtoConverterMock;
    @Mock
    private CartService cartServiceMock;
    @Mock
    private CartModel cartModelMock;
    @Mock
    private KlarnaSessionRequestDto klarnaRequestSessionMock;
    @Mock
    private KlarnaSessionResponseDto klarnaSessionResponseMock;
    @Mock
    private CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationServiceMock;
    @Mock
    private CheckoutComKlarnaConfigurationModel klarnaConfigurationMock;
    @Mock
    private KlarnaPaymentMethodCategoryDto klarnaPaymentMethodCategory1Mock, klarnaPaymentMethodCategory2Mock;

    @Before
    public void setUp() throws ExecutionException {
        when(cartServiceMock.getSessionCart()).thenReturn(cartModelMock);
        when(cartServiceMock.hasSessionCart()).thenReturn(true);
        when(checkoutComKlarnaSessionRequestDtoConverterMock.convert(cartModelMock)).thenReturn(klarnaRequestSessionMock);
        when(checkoutComPaymentIntegrationServiceMock.createKlarnaSession(klarnaRequestSessionMock)).thenReturn(klarnaSessionResponseMock);
        when(klarnaSessionResponseMock.getClientToken()).thenReturn(CLIENT_TOKEN);
        when(klarnaSessionResponseMock.getPaymentMethodCategories()).thenReturn(Arrays.asList(klarnaPaymentMethodCategory1Mock, klarnaPaymentMethodCategory2Mock));
        when(klarnaPaymentMethodCategory1Mock.getIdentifier()).thenReturn(PAY_OVER_TIME);
        when(klarnaPaymentMethodCategory2Mock.getIdentifier()).thenReturn(PAY_LATER);
        when(checkoutComMerchantConfigurationServiceMock.getKlarnaConfiguration()).thenReturn(klarnaConfigurationMock);
        when(klarnaConfigurationMock.getInstanceId()).thenReturn(INSTANCE_ID);
    }

    @Test
    public void getKlarnaClientToken_WhenIntegrationError_ShouldThrowException() throws ExecutionException {
        when(checkoutComPaymentIntegrationServiceMock.createKlarnaSession(klarnaRequestSessionMock)).thenThrow(new ExecutionException(new Throwable()));

        assertThatThrownBy(() -> testObj.getKlarnaClientToken()).isInstanceOf(ExecutionException.class);
    }

    @Test
    public void getKlarnaClientToken_WhenNoSessionCart_ShouldThrowException() throws ExecutionException {
        when(checkoutComPaymentIntegrationServiceMock.createKlarnaSession(klarnaRequestSessionMock)).thenThrow(new IllegalArgumentException());

        assertThatThrownBy(() -> testObj.getKlarnaClientToken()).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void getKlarnaClientToken_ShouldReturnPopulateClientToken() throws ExecutionException {
        final KlarnaClientTokenData result = testObj.getKlarnaClientToken();

        assertThat(result.getClientToken()).isEqualTo(CLIENT_TOKEN);
        assertThat(result.getInstanceId()).isEqualTo(INSTANCE_ID);
        assertThat(result.getPaymentMethodCategories()).containsExactlyInAnyOrder(PAY_LATER, PAY_OVER_TIME);
        assertThat(result.getSuccess()).isTrue();
    }
}
