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
public class CheckoutComOxxoPaymentDataFormValidatorTest {

    private static final String DOCUMENT_ATTRIBUTE = "document";
    private static final String DOCUMENT_MAP_FIELD = "formAttributes[document]";
    private static final String DOCUMENT_LESS_THAN_18_CHARACTERS = "asfafafa";
    private static final String DOCUMENT_MORE_THAN_18_CHARACTERS = "asfafafaasfasfasfasfasfasfasfasfasf";
    private static final String DOCUMENT_NOT_ALPHANUMERIC_CHARACTERS = "asasasasasasasasa-";
    private static final String DOCUMENT_ALPHANUMERIC_18_CHARACTERS = "asasasasasasasasa1";

    @InjectMocks
    private CheckoutComOxxoPaymentDataFormValidator testObj;

    private PaymentDataForm paymentDataForm = new PaymentDataForm();
    private Map<String, Object> attributesMap = new HashMap();

    private Errors errors;

    @Before
    public void setUp() {
        errors = new BeanPropertyBindingResult(paymentDataForm, paymentDataForm.getClass().getSimpleName());
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
    public void validate_WhenDocumentIsEmpty_ShouldReturnError() {
        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(DOCUMENT_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenDocumentHasLessThan18Characters_ShouldReturnError() {
        attributesMap.put(DOCUMENT_ATTRIBUTE, DOCUMENT_LESS_THAN_18_CHARACTERS);
        paymentDataForm.setFormAttributes(attributesMap);

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(DOCUMENT_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenDocumentIsNull_ShouldReturnError() {
        attributesMap.put(DOCUMENT_ATTRIBUTE, null);
        paymentDataForm.setFormAttributes(attributesMap);

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(DOCUMENT_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenDocumentHasMoreThan18Characters_ShouldReturnError() {
        attributesMap.put(DOCUMENT_ATTRIBUTE, DOCUMENT_MORE_THAN_18_CHARACTERS);
        paymentDataForm.setFormAttributes(attributesMap);

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(DOCUMENT_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenDocumentIsNotAlphanumeric_ShouldReturnError() {
        attributesMap.put(DOCUMENT_ATTRIBUTE, DOCUMENT_NOT_ALPHANUMERIC_CHARACTERS);
        paymentDataForm.setFormAttributes(attributesMap);

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(DOCUMENT_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenDocumentIsAlphanumericAndHas18Characters_ShouldNotReturnError() {
        attributesMap.put(DOCUMENT_ATTRIBUTE, DOCUMENT_ALPHANUMERIC_18_CHARACTERS);
        paymentDataForm.setFormAttributes(attributesMap);

        testObj.validate(paymentDataForm, errors);

        assertFalse(errors.hasErrors());
    }
}
