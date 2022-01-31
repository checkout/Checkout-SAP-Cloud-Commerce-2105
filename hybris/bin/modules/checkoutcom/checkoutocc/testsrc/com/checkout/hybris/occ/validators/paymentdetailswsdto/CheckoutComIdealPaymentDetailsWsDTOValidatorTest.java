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
public class CheckoutComIdealPaymentDetailsWsDTOValidatorTest {

    private static final String BLANK_STRING = "   ";
    private static final String BIC_KEY_MAP_FIELD = "bic";

    @InjectMocks
    private CheckoutComIdealPaymentDetailsWsDTOValidator testObj;

    private PaymentDetailsWsDTO paymentDetailsWsDTO = new PaymentDetailsWsDTO();

    private Errors errors;

    @Before
    public void setUp() {
        paymentDetailsWsDTO.setBic("ORD50234E89");

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
    public void validate_WhenBicFieldElevenCharValid_ShouldNotReturnAnyError() {
        testObj.validate(paymentDetailsWsDTO, errors);

        assertFalse(errors.hasErrors());
        assertEquals(0, errors.getErrorCount());
    }

    @Test
    public void validate_WhenAttributeNotFoundOnMap_ShouldReturnError() {
        paymentDetailsWsDTO.setBic(null);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(BIC_KEY_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenBicContainsInvalidCharacters_ShouldReturnError() {
        paymentDetailsWsDTO.setBic("ORD50/E89");

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(BIC_KEY_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenBicIsDoesNotMatchLengthRequirement_ShouldReturnError() {
        paymentDetailsWsDTO.setBic("123456789");

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(BIC_KEY_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenBicIsBlank_ShouldReturnError() {
        paymentDetailsWsDTO.setBic(BLANK_STRING);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(BIC_KEY_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenBicIsExactlyEightChar_ShouldNotReturnError() {
        paymentDetailsWsDTO.setBic("tg4gasg5");

        testObj.validate(paymentDetailsWsDTO, errors);

        assertFalse(errors.hasErrors());
        assertEquals(0, errors.getErrorCount());
    }
}
