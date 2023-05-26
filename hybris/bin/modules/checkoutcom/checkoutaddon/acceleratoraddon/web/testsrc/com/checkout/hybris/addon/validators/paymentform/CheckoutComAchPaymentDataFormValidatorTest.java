package com.checkout.hybris.addon.validators.paymentform;

import com.checkout.hybris.addon.forms.PaymentDataForm;
import com.checkout.hybris.core.enums.AchAccountType;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.acceleratorstorefrontcommons.forms.ForgottenPwdForm;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.util.HashMap;
import java.util.Map;

import static com.checkout.hybris.addon.validators.paymentform.CheckoutComAchPaymentDataFormValidator.*;
import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.ACH;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;


@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComAchPaymentDataFormValidatorTest {

    private static final String ACCOUNT_HOLDER_NAME_MAP_FIELD = "formAttributes[accountHolderName]";
    private static final String ACCOUNT_TYPE_MAP_FIELD = "formAttributes[accountType]";
    private static final String ACCOUNT_NUMBER_MAP_FIELD = "formAttributes[accountNumber]";
    private static final String ROUTING_NUMBER_MAP_FIELD = "formAttributes[routingNumber]";
    private static final String COMPANY_NAME_MAP_FIELD = "formAttributes[companyName]";
    private static final String CORPORATE_ACCOUNT_TYPE = "Corporate";
    private static final String BANK_CODE_MAP_FIELD = "formAttributes[bankCode]";
    private static final String PAYMENT_METHOD_MAP_FIELD = "formAttributes[paymentMethod]";

    @InjectMocks
    private CheckoutComAchPaymentDataFormValidator testObj;

    @Mock
    private EnumerationService enumerationServiceMock;
    @Mock
    private AchAccountType achAccountTypeMock;

    private PaymentDataForm paymentDataForm = new PaymentDataForm();
    private Map<String, Object> attributesMap = new HashMap();

    private Errors errors;

    @Before
    public void setUp() {
        attributesMap.put(ACCOUNT_HOLDER_NAME_FORM_KEY, "Mr Tom Trump");
        attributesMap.put(ACCOUNT_TYPE_FORM_KEY, CORPORATE_ACCOUNT_TYPE);
        attributesMap.put(ACCOUNT_NUMBER_FORM_KEY, "4099999992");
        attributesMap.put(ROUTING_NUMBER_FORM_KEY, "011075150");
        attributesMap.put(COMPANY_NAME_FORM_KEY, "Electronics");
        attributesMap.put(BANK_CODE_FORM_KEY, "123");
        attributesMap.put(PAYMENT_METHOD_FORM_KEY, "Payment Method");
        attributesMap.put("type", ACH.name());
        paymentDataForm.setFormAttributes(attributesMap);

        errors = new BeanPropertyBindingResult(paymentDataForm, paymentDataForm.getClass().getSimpleName());

        when(enumerationServiceMock.getEnumerationValue(AchAccountType.class, CORPORATE_ACCOUNT_TYPE)).thenReturn(achAccountTypeMock);
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
    public void validate_WhenAccountHolderNameNotFound_ShouldReturnError() {
        attributesMap.remove(ACCOUNT_HOLDER_NAME_FORM_KEY);

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(ACCOUNT_HOLDER_NAME_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenAccountHolderNameBlank_ShouldReturnError() {
        attributesMap.put(ACCOUNT_HOLDER_NAME_FORM_KEY, "  ");

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(ACCOUNT_HOLDER_NAME_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenAccountTypeNotFound_ShouldReturnError() {
        attributesMap.remove(ACCOUNT_TYPE_FORM_KEY);

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(ACCOUNT_TYPE_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenAccountTypeEmpty_ShouldReturnError() {
        when(enumerationServiceMock.getEnumerationValue(AchAccountType.class, "  ")).thenThrow(new UnknownIdentifierException("Account type not found"));
        attributesMap.put(ACCOUNT_TYPE_FORM_KEY, "  ");

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(ACCOUNT_TYPE_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenAccountTypeInvalid_ShouldReturnError() {
        when(enumerationServiceMock.getEnumerationValue(AchAccountType.class, CORPORATE_ACCOUNT_TYPE)).thenThrow(new UnknownIdentifierException("Account type not found"));

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(ACCOUNT_TYPE_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenAccountNumberNotFound_ShouldReturnError() {
        attributesMap.remove(ACCOUNT_NUMBER_FORM_KEY);

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(ACCOUNT_NUMBER_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenAccountNumberEmpty_ShouldReturnError() {
        attributesMap.put(ACCOUNT_NUMBER_FORM_KEY, "   ");

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(ACCOUNT_NUMBER_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenAccountNumberTooShort_ShouldReturnError() {
        attributesMap.put(ACCOUNT_NUMBER_FORM_KEY, "123");

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(ACCOUNT_NUMBER_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenAccountNumberTooLong_ShouldReturnError() {
        attributesMap.put(ACCOUNT_NUMBER_FORM_KEY, "111222333444444455");

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(ACCOUNT_NUMBER_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenAccountNumberContainsCharacters_ShouldReturnError() {
        attributesMap.put(ACCOUNT_NUMBER_FORM_KEY, "32ffds33");

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(ACCOUNT_NUMBER_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenRoutingNumberNotFound_ShouldReturnError() {
        attributesMap.remove(ROUTING_NUMBER_FORM_KEY);

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(ROUTING_NUMBER_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenRoutingNumberEmpty_ShouldReturnError() {
        attributesMap.put(ROUTING_NUMBER_FORM_KEY, "   ");

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(ROUTING_NUMBER_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenRoutingNotValid_ShouldReturnError() {
        attributesMap.put(ROUTING_NUMBER_FORM_KEY, "233dda");

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(ROUTING_NUMBER_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenCompanyNameNotFound_ShouldReturnError() {
        attributesMap.remove(COMPANY_NAME_FORM_KEY);

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(COMPANY_NAME_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenCompanyNameEmpty_ShouldReturnError() {
        attributesMap.put(COMPANY_NAME_FORM_KEY, "   ");

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(COMPANY_NAME_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenCompanyNameNotValid_ShouldReturnError() {
        attributesMap.put(COMPANY_NAME_FORM_KEY, "gsgssgsgs gsggsgsgsgsgv sggsgaggdagdajas sgagasgjdgjadgjasgjdasgj gsagdgas");

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(COMPANY_NAME_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenBankCodeNotFound_ShouldReturnError() {
        attributesMap.remove(BANK_CODE_FORM_KEY);

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(BANK_CODE_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenBankCodeEmpty_ShouldReturnError() {
        attributesMap.put(BANK_CODE_FORM_KEY, "   ");

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(BANK_CODE_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenBankCodeNotValid_ShouldReturnError() {
        attributesMap.put(BANK_CODE_FORM_KEY, "233dda");

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(BANK_CODE_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenPaymentMethodNotFound_ShouldReturnError() {
        attributesMap.remove(PAYMENT_METHOD_FORM_KEY);

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(PAYMENT_METHOD_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenPaymentMethodEmpty_ShouldReturnError() {
        attributesMap.put(PAYMENT_METHOD_FORM_KEY, "   ");

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(PAYMENT_METHOD_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenEverythingIsCorrect_ShouldNotReturnErrors() {
        testObj.validate(paymentDataForm, errors);

        assertFalse(errors.hasErrors());
    }
}
