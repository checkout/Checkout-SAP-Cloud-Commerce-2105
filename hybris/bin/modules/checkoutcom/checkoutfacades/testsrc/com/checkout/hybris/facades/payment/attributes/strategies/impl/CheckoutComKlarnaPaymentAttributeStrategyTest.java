package com.checkout.hybris.facades.payment.attributes.strategies.impl;

import com.checkout.hybris.facades.beans.KlarnaClientTokenData;
import com.checkout.hybris.facades.payment.klarna.CheckoutComKlarnaFacade;
import de.hybris.bootstrap.annotations.UnitTest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.concurrent.ExecutionException;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.KLARNA;
import static com.checkout.hybris.facades.payment.attributes.strategies.impl.CheckoutComKlarnaPaymentAttributeStrategy.KLARNA_CLIENT_TOKEN_MODEL_ATTRIBUTE;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComKlarnaPaymentAttributeStrategyTest {

    @InjectMocks
    private CheckoutComKlarnaPaymentAttributeStrategy testObj;

    @Mock
    private CheckoutComKlarnaFacade checkoutComKlarnaFacadeMock;
    @Mock
    private KlarnaClientTokenData klarnaClientTokenMock;

    private Model model = new ExtendedModelMap();

    @Test
    public void addPaymentAttributeToModel_ShouldAddTheClientResponseIntoTheModelWithSuccessTrue() throws ExecutionException {
        when(checkoutComKlarnaFacadeMock.getKlarnaClientToken()).thenReturn(klarnaClientTokenMock);

        testObj.addPaymentAttributeToModel(model);

        final KlarnaClientTokenData value = (KlarnaClientTokenData) model.asMap().get(KLARNA_CLIENT_TOKEN_MODEL_ATTRIBUTE);
        assertEquals(klarnaClientTokenMock, value);
    }

    @Test
    public void addPaymentAttributeToModel_WhenIntegrationError_ShouldAddTheClientResponseIntoTheModelWithSuccessFalse() throws ExecutionException {
        when(checkoutComKlarnaFacadeMock.getKlarnaClientToken()).thenThrow(new ExecutionException(new Throwable()));

        testObj.addPaymentAttributeToModel(model);

        final KlarnaClientTokenData value = (KlarnaClientTokenData) model.asMap().get(KLARNA_CLIENT_TOKEN_MODEL_ATTRIBUTE);
        assertNull(value.getClientToken());
        assertEquals(false, value.getSuccess());
    }

    @Test
    public void addPaymentAttributeToModel_WhenIllegalArgumentException_ShouldAddTheClientResponseIntoTheModelWithSuccessFalse() throws ExecutionException {
        when(checkoutComKlarnaFacadeMock.getKlarnaClientToken()).thenThrow(new IllegalArgumentException());

        testObj.addPaymentAttributeToModel(model);

        final KlarnaClientTokenData value = (KlarnaClientTokenData) model.asMap().get(KLARNA_CLIENT_TOKEN_MODEL_ATTRIBUTE);
        assertNull(value.getClientToken());
        assertEquals(false, value.getSuccess());
    }

    @Test
    public void getStrategyKey_WhenKlarna_ShouldReturnKlarnaType() {
        assertEquals(KLARNA, testObj.getStrategyKey());
    }
}
