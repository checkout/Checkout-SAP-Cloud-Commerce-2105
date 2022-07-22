package com.checkout.hybris.occ.validators.impl;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.order.data.CartData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.validation.Errors;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComPlaceOrderGoogleCartValidatorTest {

    @Spy
    @InjectMocks
    private CheckoutComPlaceOrderGoogleCartValidator testObj;

    @Mock
    private CartData targetMock;
    @Mock
    private Errors errorsMock;

    @Test
    public void validate_ShouldCallSuperMethods() {
        doNothing().when(testObj).callSuperMethods(errorsMock, targetMock);

        testObj.validate(targetMock, errorsMock);

        verify(testObj).callSuperMethods(errorsMock, targetMock);
    }
}
