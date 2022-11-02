package com.checkout.hybris.events.controller;

import com.checkout.hybris.events.facades.CheckoutComEventFacade;
import com.checkout.hybris.events.validators.CheckoutComRequestEventValidator;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComEventControllerTest {

    private static final String EVENT_BODY = "EventBody";
    private static final String ERROR_MESSAGE = "Exception while converting the event body to hmac.";

    @InjectMocks
    private CheckoutComEventController testObj;

    @Mock
    private CheckoutComEventFacade checkoutComEventFacadeMock;
    @Mock
    private CheckoutComRequestEventValidator checkoutComRequestEventValidatorMock;

    @Mock
    private HttpServletRequest requestMock;

    @Test
    public void receiveEvent_whenIsNotValidRequestEvent_shouldDoNothing() throws NoSuchAlgorithmException, InvalidKeyException {
        when(checkoutComRequestEventValidatorMock.isRequestEventValid(requestMock, EVENT_BODY)).thenReturn(false);

        testObj.receiveEvent(requestMock, EVENT_BODY);

        verifyZeroInteractions(checkoutComEventFacadeMock);
    }

    @Test
    public void receiveEvent_whenIsValidRequestEvent_shouldPublishPaymentEvent() throws NoSuchAlgorithmException, InvalidKeyException {
        when(checkoutComRequestEventValidatorMock.isRequestEventValid(requestMock, EVENT_BODY)).thenReturn(true);

        testObj.receiveEvent(requestMock, EVENT_BODY);

        verify(checkoutComEventFacadeMock).publishPaymentEvent(EVENT_BODY);
    }

    @Test
    public void receiveEvent_whenIsNotValidRequestEvent_shouldThrowResponseStatusException() throws NoSuchAlgorithmException, InvalidKeyException {
        when(checkoutComRequestEventValidatorMock.isRequestEventValid(requestMock, EVENT_BODY)).thenThrow(NoSuchAlgorithmException.class);

        final Throwable throwable = catchThrowable(() -> testObj.receiveEvent(requestMock, EVENT_BODY));

        assertThat(throwable)
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining(ERROR_MESSAGE);
        assertThat(((ResponseStatusException) throwable).getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
