package com.checkout.hybris.addon.validators.impl;

import com.checkout.hybris.addon.forms.PaymentDataForm;
import com.checkout.hybris.addon.validators.paymentform.CheckoutComFawryPaymentDataFormValidator;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.core.payment.resolvers.CheckoutComPaymentTypeResolver;
import com.google.common.collect.ImmutableMap;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.acceleratorstorefrontcommons.forms.ForgottenPwdForm;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Collections;
import java.util.Map;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.FAWRY;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComPaymentDataFormValidValidatorTest {

    private static final String FORM_ATTRIBUTES_TYPE = "formAttributes[type]";

    @InjectMocks
    private CheckoutComPaymentDataFormValidValidator testObj;

    private PaymentDataForm paymentDataForm = new PaymentDataForm();
    private Errors errors;

    @Mock
    private CheckoutComPaymentTypeResolver paymentResolverMock;
    @Mock
    private Map<CheckoutComPaymentType, Validator> validatorsMock;
    @Mock
    private CheckoutComFawryPaymentDataFormValidator checkoutComFawryPaymentDataFormValidatorMock;

    @Before
    public void setUp() {
        paymentDataForm.setFormAttributes(ImmutableMap.of("type", FAWRY.name()));
        errors = new BeanPropertyBindingResult(paymentDataForm, paymentDataForm.getClass().getSimpleName());
        when(validatorsMock.get(FAWRY)).thenReturn(checkoutComFawryPaymentDataFormValidatorMock);
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
    public void validate_WhenValidPaymentType_ShouldCallTheCorrectValidator() {
        when(paymentResolverMock.resolvePaymentMethod(FAWRY.name())).thenReturn(FAWRY);
        doNothing().when(checkoutComFawryPaymentDataFormValidatorMock).validate(FAWRY, errors);

        testObj.validate(paymentDataForm, errors);

        assertFalse(errors.hasErrors());
        verify(paymentResolverMock).resolvePaymentMethod(FAWRY.name());
        verify(checkoutComFawryPaymentDataFormValidatorMock).validate(paymentDataForm, errors);
    }

    @Test
    public void validate_WhenEmptyAttributesMap_ShouldReturnErrors() {
        paymentDataForm.setFormAttributes(Collections.emptyMap());

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(FORM_ATTRIBUTES_TYPE, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenTypeAttributeNotFound_ShouldReturnErrors() {
        paymentDataForm.setFormAttributes(ImmutableMap.of("number", "122323"));

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(FORM_ATTRIBUTES_TYPE, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenInvalidPaymentType_ShouldReturnErrors() {
        paymentDataForm.setFormAttributes(ImmutableMap.of("type", "invalid"));

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(FORM_ATTRIBUTES_TYPE, errors.getFieldError().getField());
    }
}
