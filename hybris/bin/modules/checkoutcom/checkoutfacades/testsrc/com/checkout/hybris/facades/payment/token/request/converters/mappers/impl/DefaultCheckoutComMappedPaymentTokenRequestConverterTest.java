package com.checkout.hybris.facades.payment.token.request.converters.mappers.impl;

import com.checkout.hybris.facades.beans.WalletPaymentAdditionalAuthInfo;
import com.checkout.hybris.facades.enums.WalletPaymentType;
import com.checkout.tokens.WalletTokenRequest;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComMappedPaymentTokenRequestConverterTest {

    @InjectMocks
    private DefaultCheckoutComMappedPaymentTokenRequestConverter testObj;

    @Mock
    private Converter<WalletPaymentAdditionalAuthInfo, WalletTokenRequest> converterMock;
    @Mock
    private Map<WalletPaymentType, Converter<WalletPaymentAdditionalAuthInfo, WalletTokenRequest>> convertersMapMock;
    @Mock
    private WalletPaymentAdditionalAuthInfo sourceMock;
    @Mock
    private WalletTokenRequest targetMock;

    @Before
    public void setUp() {
        when(convertersMapMock.get(WalletPaymentType.APPLEPAY)).thenReturn(converterMock);
        when(converterMock.convert(sourceMock)).thenReturn(targetMock);
    }

    @Test
    public void convertWalletTokenRequest_WhenEverythingIsCorrect_ShouldConvertTheSourceProperly() {
        final WalletTokenRequest result = testObj.convertWalletTokenRequest(sourceMock, WalletPaymentType.APPLEPAY);

        assertEquals(targetMock, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void convertWalletTokenRequest_WhenSourceNull_ShouldThrowException() {
        testObj.convertWalletTokenRequest(null, WalletPaymentType.APPLEPAY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void convertWalletTokenRequest_WhenWalletTypeNull_ShouldThrowException() {
        testObj.convertWalletTokenRequest(sourceMock, null);
    }
}