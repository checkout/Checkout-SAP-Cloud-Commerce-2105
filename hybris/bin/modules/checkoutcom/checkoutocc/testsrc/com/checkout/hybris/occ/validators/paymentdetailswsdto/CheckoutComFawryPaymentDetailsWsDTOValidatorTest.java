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
public class CheckoutComFawryPaymentDetailsWsDTOValidatorTest {

    private static final String MOBILE_NUMBER_MAP_FIELD = "mobileNumber";

    @InjectMocks
    private CheckoutComFawryPaymentDetailsWsDTOValidator testObj;

    private PaymentDetailsWsDTO paymentDetailsWsDTO = new PaymentDetailsWsDTO();

    private Errors errors;

    @Before
    public void setUp() {
        paymentDetailsWsDTO.setMobileNumber("12345678901");

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
    public void validate_WhenMobileNumberIsValid_ShouldNotReturnAnyError() {
        testObj.validate(paymentDetailsWsDTO, errors);

        assertFalse(errors.hasErrors());
        assertEquals(0, errors.getErrorCount());
    }

    @Test
    public void validate_WhenAttributeNotFoundOnMap_ShouldReturnError() {
        paymentDetailsWsDTO.setMobileNumber(null);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(MOBILE_NUMBER_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenMobileNumberContainsInvalidCharacters_ShouldReturnError() {
        paymentDetailsWsDTO.setMobileNumber("12dfr245678");

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(MOBILE_NUMBER_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenMobileIsNotElevenCharacters_ShouldReturnError() {
        paymentDetailsWsDTO.setMobileNumber("12345");

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(MOBILE_NUMBER_MAP_FIELD, errors.getFieldError().getField());
    }
}
