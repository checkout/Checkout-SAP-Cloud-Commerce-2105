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
public class CheckoutComSepaPaymentDetailsWsDTOValidatorTest {

    private static final String FIRST_NAME_KEY = "firstName";
    private static final String LAST_NAME_KEY = "lastName";
    private static final String PAYMENT_TYPE_KEY = "paymentType";
    private static final String ACCOUNT_IBAN_KEY = "accountIban";
    private static final String ADDRESS_LINE1_KEY = "addressLine1";
    private static final String CITY_KEY = "city";
    private static final String POSTAL_CODE_KEY = "postalCode";
    private static final String COUNTRY_KEY = "country";
    private static final String BLANK_STRING = "   ";

    @InjectMocks
    private CheckoutComSepaPaymentDetailsWsDTOValidator testObj;

    private Errors errors;

    private PaymentDetailsWsDTO paymentDetailsWsDTO = new PaymentDetailsWsDTO();

    @Before
    public void setUp() {
        errors = new BeanPropertyBindingResult(paymentDetailsWsDTO, paymentDetailsWsDTO.getClass().getSimpleName());
        paymentDetailsWsDTO.setFirstName("John");
        paymentDetailsWsDTO.setLastName("Snow");
        paymentDetailsWsDTO.setPaymentType("RECURRING_PAYMENT");
        paymentDetailsWsDTO.setAccountIban("GB56789123456");
        paymentDetailsWsDTO.setAddressLine1("1 Buckingham Palace Road");
        paymentDetailsWsDTO.setAddressLine2("Royal Palace");
        paymentDetailsWsDTO.setCity("London");
        paymentDetailsWsDTO.setPostalCode("SW12WS");
        paymentDetailsWsDTO.setCountry("UK");
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
    public void validate_WhenAttributesAreValid_ShouldNotReturnAnyError() {
        testObj.validate(paymentDetailsWsDTO, errors);

        assertFalse(errors.hasErrors());
        assertEquals(0, errors.getErrorCount());
    }

    @Test
    public void validate_WhenPaymentFirstNameIsBlank_ShouldReturnError() {
        paymentDetailsWsDTO.setFirstName(BLANK_STRING);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(FIRST_NAME_KEY, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenLastNameIsBlank_ShouldReturnError() {
        paymentDetailsWsDTO.setLastName(BLANK_STRING);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(LAST_NAME_KEY, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenPaymentTypeIsBlank_ShouldReturnError() {
        paymentDetailsWsDTO.setPaymentType(BLANK_STRING);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(PAYMENT_TYPE_KEY, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenAccountIbanIsBlank_ShouldReturnError() {
        paymentDetailsWsDTO.setAccountIban(BLANK_STRING);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(ACCOUNT_IBAN_KEY, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenAddressLine1IsBlank_ShouldReturnError() {
        paymentDetailsWsDTO.setAddressLine1(BLANK_STRING);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(ADDRESS_LINE1_KEY, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenCityIsBlank_ShouldReturnError() {
        paymentDetailsWsDTO.setCity(BLANK_STRING);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(CITY_KEY, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenPostalCodeIsBlank_ShouldReturnError() {
        paymentDetailsWsDTO.setPostalCode(null);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(POSTAL_CODE_KEY, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenCountryIsBlank_ShouldReturnError() {
        paymentDetailsWsDTO.setCountry(null);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(COUNTRY_KEY, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenAttributeNotFoundOnMap_ShouldReturnError() {
        paymentDetailsWsDTO.setCity(null);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(CITY_KEY, errors.getFieldError().getField());
    }
}
