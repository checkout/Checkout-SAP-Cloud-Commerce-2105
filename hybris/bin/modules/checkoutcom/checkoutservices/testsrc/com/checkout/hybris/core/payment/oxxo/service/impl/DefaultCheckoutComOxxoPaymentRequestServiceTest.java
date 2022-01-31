package com.checkout.hybris.core.payment.oxxo.service.impl;

import com.checkout.hybris.core.apm.services.CheckoutComAPMConfigurationService;
import com.checkout.hybris.core.model.CheckoutComAPMConfigurationModel;
import com.checkout.hybris.core.model.CheckoutComOxxoConfigurationModel;
import com.checkout.hybris.core.oxxo.session.request.OxxoPayerRequestDto;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static de.hybris.platform.testframework.Assert.assertEquals;
import static org.mockito.Mockito.when;


@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComOxxoPaymentRequestServiceTest {

    @InjectMocks
    private DefaultCheckoutComOxxoPaymentRequestService testObj;

    @Mock
    private CheckoutComAPMConfigurationService checkoutComAPMConfigurationServiceMock;
    @Mock
    private Converter<CartModel, OxxoPayerRequestDto> checkoutComOxxoRequestPayerDtoConverterMock;

    @Mock
    private CartModel cartModelMock;
    @Mock
    private OxxoPayerRequestDto oxxoPayerRequestDtoMock;
    @Mock
    private CheckoutComOxxoConfigurationModel oxxoApmConfigurationMock;

    @Test
    public void getPayerDto_ShouldReturnPayerDto() {
        when(checkoutComOxxoRequestPayerDtoConverterMock.convert(cartModelMock)).thenReturn(oxxoPayerRequestDtoMock);

        final OxxoPayerRequestDto result = testObj.getPayerDto(cartModelMock);

        assertEquals(result, oxxoPayerRequestDtoMock);
    }

    @Test
    public void getOxxoApmConfiguration_ShouldReturnOxxo() {
        when(checkoutComAPMConfigurationServiceMock.getApmConfigurationByCode(CheckoutComPaymentType.OXXO.name()))
                .thenReturn(Optional.of(oxxoApmConfigurationMock));

        final Optional<CheckoutComAPMConfigurationModel> result = testObj.getOxxoApmConfiguration();

        assertEquals(result, Optional.of(oxxoApmConfigurationMock));
    }
}
