package com.checkout.hybris.occ.validators.paymentdetailswsdto;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.user.AddressWsDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import static org.junit.Assert.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComKlarnaPaymentDetailsWsDTOValidatorTest {

    private static final String AUTHORIZATION_TOKEN = "authorizationToken";
    private static final String BLANK_STRING = "    ";

    @InjectMocks
    private CheckoutComKlarnaPaymentDetailsWsDTOValidator testObj;

    private PaymentDetailsWsDTO paymentDetailsWsDTO = new PaymentDetailsWsDTO();

    private Errors errors;

    @Before
    public void setUp() {
        paymentDetailsWsDTO.setAuthorizationToken("12345678901_abdajkdjal");

        errors = new BeanPropertyBindingResult(paymentDetailsWsDTO, paymentDetailsWsDTO.getClass().getSimpleName());
    }

    @Test
    public void supports_WhenPaymentDetailsWsDTOType_ShouldReturnTrue() {
        assertTrue(testObj.supports(PaymentDetailsWsDTO.class));
    }

    @Test
    public void supports_WhenNotCorrectType_ShouldReturnFalse() {
        assertFalse(testObj.supports(AddressWsDTO.class));
    }

    @Test
    public void validate_WhenAuthorizationTokenIsPopulated_ShouldNotReturnErrors() {
        testObj.validate(paymentDetailsWsDTO, errors);

        assertFalse(errors.hasErrors());
        assertEquals(0, errors.getErrorCount());
    }

    @Test
    public void validate_WhenAuthorizationTokenIsNull_ShouldReturnError() {
        paymentDetailsWsDTO.setAuthorizationToken(null);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(AUTHORIZATION_TOKEN, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenAuthorizationTokenNotPopulated_ShouldReturnError() {
        paymentDetailsWsDTO.setAuthorizationToken(BLANK_STRING);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(AUTHORIZATION_TOKEN, errors.getFieldError().getField());
    }
}
