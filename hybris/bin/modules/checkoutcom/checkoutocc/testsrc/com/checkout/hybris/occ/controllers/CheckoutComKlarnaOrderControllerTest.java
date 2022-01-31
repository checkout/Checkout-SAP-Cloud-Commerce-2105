package com.checkout.hybris.occ.controllers;

import com.checkout.hybris.facades.payment.klarna.CheckoutComKlarnaFacade;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
@UnitTest
public class CheckoutComKlarnaOrderControllerTest {

    @InjectMocks
    private CheckoutComKlarnaOrderController testObj;

    @Mock
    private CheckoutComKlarnaFacade checkoutComKlarnaFacadeMock;

    @Test
    public void getClientToken_ShouldCallFacade() throws ExecutionException {
        testObj.getClientToken();

        verify(checkoutComKlarnaFacadeMock).getKlarnaClientToken();
    }
}
