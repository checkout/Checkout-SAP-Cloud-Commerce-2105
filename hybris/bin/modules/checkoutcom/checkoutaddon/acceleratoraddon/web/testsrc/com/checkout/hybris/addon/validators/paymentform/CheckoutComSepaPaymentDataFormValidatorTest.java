package com.checkout.hybris.addon.validators.paymentform;

import com.checkout.hybris.addon.forms.PaymentDataForm;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.acceleratorstorefrontcommons.forms.ForgottenPwdForm;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComSepaPaymentDataFormValidatorTest {

    private static final String FIRST_NAME_KEY = "firstName";
    private static final String LAST_NAME_KEY = "lastName";
    private static final String PAYMENT_TYPE_KEY = "paymentType";
    private static final String ACCOUNT_IBAN_KEY = "accountIban";
    private static final String ADDRESS_LINE1_KEY = "addressLine1";
    private static final String ADDRESS_LINE2_KEY = "addressLine2";
    private static final String CITY_KEY = "city";
    private static final String POSTAL_CODE_KEY = "postalCode";
    private static final String COUNTRY_KEY = "country";

    @InjectMocks
    private CheckoutComSepaPaymentDataFormValidator testObj;

    private Errors errors;

    private PaymentDataForm paymentDataForm = new PaymentDataForm();
    private Map<String, Object> attributesMap = new HashMap();

    @Before
    public void setUp() {
        errors = new BeanPropertyBindingResult(paymentDataForm, paymentDataForm.getClass().getSimpleName());

        attributesMap.put(FIRST_NAME_KEY, "John");
        attributesMap.put(LAST_NAME_KEY, "Snow");
        attributesMap.put(PAYMENT_TYPE_KEY, "RECURRING_PAYMENT");
        attributesMap.put(ACCOUNT_IBAN_KEY, "GB56789123456");
        attributesMap.put(ADDRESS_LINE1_KEY, "1 Buckingham Palace Road");
        attributesMap.put(ADDRESS_LINE2_KEY, "Royal Palace");
        attributesMap.put(CITY_KEY, "London");
        attributesMap.put(POSTAL_CODE_KEY, "SW12WS");
        attributesMap.put(COUNTRY_KEY, "UK");

        paymentDataForm.setFormAttributes(attributesMap);
    }

    @Test
    public void supports_WhenPaymentDataFormType_ShouldReturnTrue() {
        assertTrue(testObj.supports(PaymentDataForm.class));
    }

    @Test
    public void supports_WhenNotCorrectType_ShouldReturnFalse() {
        assertFalse(testObj.supports(ForgottenPwdForm.class));
    }

    @Test
    public void validate_WhenAttributesAreValid_ShouldNotReturnAnyError() {
        testObj.validate(paymentDataForm, errors);

        assertFalse(errors.hasErrors());
        assertEquals(0, errors.getErrorCount());
    }

    @Test
    public void validate_WhenPaymentFirstNameIsBlank_ShouldReturnError() {
        paymentDataForm.getFormAttributes().replace(FIRST_NAME_KEY, "   ");

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals("formAttributes[" + FIRST_NAME_KEY + "]", errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenLastNameIsBlank_ShouldReturnError() {
        paymentDataForm.getFormAttributes().replace(LAST_NAME_KEY, "   ");

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals("formAttributes[" + LAST_NAME_KEY + "]", errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenPaymentTypeIsBlank_ShouldReturnError() {
        paymentDataForm.getFormAttributes().replace(PAYMENT_TYPE_KEY, "   ");

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals("formAttributes[" + PAYMENT_TYPE_KEY + "]", errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenAccountIbanIsBlank_ShouldReturnError() {
        paymentDataForm.getFormAttributes().replace(ACCOUNT_IBAN_KEY, "   ");

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals("formAttributes[" + ACCOUNT_IBAN_KEY + "]", errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenAddressLine1IsBlank_ShouldReturnError() {
        paymentDataForm.getFormAttributes().replace(ADDRESS_LINE1_KEY, "   ");

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals("formAttributes[" + ADDRESS_LINE1_KEY + "]", errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenCityIsBlank_ShouldReturnError() {
        paymentDataForm.getFormAttributes().replace(CITY_KEY, "   ");

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals("formAttributes[" + CITY_KEY + "]", errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenPostalCodeIsBlank_ShouldReturnError() {
        paymentDataForm.getFormAttributes().replace(POSTAL_CODE_KEY, null);

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals("formAttributes[" + POSTAL_CODE_KEY + "]", errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenCountryIsBlank_ShouldReturnError() {
        paymentDataForm.getFormAttributes().replace(COUNTRY_KEY, null);

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals("formAttributes[" + COUNTRY_KEY + "]", errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenAttributeNotFoundOnMap_ShouldReturnError() {
        paymentDataForm.getFormAttributes().remove(CITY_KEY);

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals("formAttributes[" + CITY_KEY + "]", errors.getFieldError().getField());
    }
}
