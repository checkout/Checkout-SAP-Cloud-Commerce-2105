package com.checkout.hybris.occ.validators.impl;

import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.core.payment.resolvers.CheckoutComPaymentTypeResolver;
import com.checkout.hybris.occ.validators.paymentdetailswsdto.CheckoutComCardPaymentDetailsWsDTOValidator;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.user.AddressWsDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.HashMap;
import java.util.Map;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.CARD;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComPaymentDetailsWsDTOValidValidatorTest {

    private static final String FORM_ATTRIBUTES_TYPE = "type";

    @InjectMocks
    private CheckoutComPaymentDetailsWsDTOValidValidator testObj;

    @Mock
    private CheckoutComPaymentTypeResolver checkoutComPaymentTypeResolverMock;
    @Mock
    private CheckoutComCardPaymentDetailsWsDTOValidator checkoutComCardPaymentDetailsWsDTOValidatorMock;

    private Errors errors;

    private Map<CheckoutComPaymentType, Validator> validators = new HashMap<>();

    private PaymentDetailsWsDTO paymentDetailsWsDTO = new PaymentDetailsWsDTO();

    @Before
    public void setUp() {
        testObj = new CheckoutComPaymentDetailsWsDTOValidValidator(checkoutComPaymentTypeResolverMock, validators);
        errors = new BeanPropertyBindingResult(paymentDetailsWsDTO, paymentDetailsWsDTO.getClass().getSimpleName());
        paymentDetailsWsDTO.setType(CARD.name());
        validators.put(CARD, checkoutComCardPaymentDetailsWsDTOValidatorMock);
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
    public void validate_WhenValidPaymentType_ShouldCallTheCorrectValidator() {
        when(checkoutComPaymentTypeResolverMock.resolvePaymentMethod(CARD.name())).thenReturn(CARD);
        doNothing().when(checkoutComCardPaymentDetailsWsDTOValidatorMock).validate(CARD, errors);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertFalse(errors.hasErrors());
        verify(checkoutComPaymentTypeResolverMock).resolvePaymentMethod(CARD.name());
        verify(checkoutComCardPaymentDetailsWsDTOValidatorMock).validate(paymentDetailsWsDTO, errors);
    }

    @Test
    public void validate_WhenEmptyAttributesMap_ShouldReturnErrors() {
        paymentDetailsWsDTO.setType(null);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(FORM_ATTRIBUTES_TYPE, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenTypeAttributeNotFound_ShouldReturnErrors() {
        paymentDetailsWsDTO.setType(null);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(FORM_ATTRIBUTES_TYPE, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenInvalidPaymentType_ShouldReturnErrors() {
        paymentDetailsWsDTO.setType("invalid");

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(FORM_ATTRIBUTES_TYPE, errors.getFieldError().getField());
    }
}
