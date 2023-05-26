package com.checkout.hybris.occ.validators.paymentdetailswsdto;

import com.checkout.hybris.core.enums.AchAccountType;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.user.AddressWsDTO;
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

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.ACH;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComAchPaymentDetailsWsDTOValidatorTest {

    private static final String ACCOUNT_HOLDER_NAME_MAP_FIELD = "accountHolderName";
    private static final String ACCOUNT_TYPE_MAP_FIELD = "accountType";
    private static final String ACCOUNT_NUMBER_MAP_FIELD = "accountNumber";
    private static final String BANK_CODE_MAP_FIELD = "bankCode";
    private static final String PAYMENT_METHOD_MAP_FIELD = "paymentMethod";
    private static final String ROUTING_NUMBER_MAP_FIELD = "routingNumber";
    private static final String COMPANY_NAME_MAP_FIELD = "companyName";
    private static final String CORPORATE_ACCOUNT_TYPE = "Corporate";
    private static final String BLANK_STRING = "  ";

    @InjectMocks
    private CheckoutComAchPaymentDetailsWsDTOValidator testObj;

    @Mock
    private EnumerationService enumerationServiceMock;
    @Mock
    private AchAccountType achAccountTypeMock;

    private PaymentDetailsWsDTO paymentDetailsWsDTO = new PaymentDetailsWsDTO();

    private Errors errors;

    @Before
    public void setUp() {
        paymentDetailsWsDTO.setAccountHolderName("Mr Tom Trump");
        paymentDetailsWsDTO.setAccountType(CORPORATE_ACCOUNT_TYPE);
        paymentDetailsWsDTO.setAccountNumber("4099999992");
        paymentDetailsWsDTO.setBankCode("123");
        paymentDetailsWsDTO.setPaymentMethod("Payment Method");
        paymentDetailsWsDTO.setRoutingNumber("011075150");
        paymentDetailsWsDTO.setCompanyName("Electronics");
        paymentDetailsWsDTO.setType(ACH.name());

        errors = new BeanPropertyBindingResult(paymentDetailsWsDTO, paymentDetailsWsDTO.getClass().getSimpleName());

        when(enumerationServiceMock.getEnumerationValue(AchAccountType.class, CORPORATE_ACCOUNT_TYPE)).thenReturn(achAccountTypeMock);
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
    public void validate_WhenAccountHolderNameNotFound_ShouldReturnError() {
        paymentDetailsWsDTO.setAccountHolderName(null);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(ACCOUNT_HOLDER_NAME_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenAccountHolderNameBlank_ShouldReturnError() {
        paymentDetailsWsDTO.setAccountHolderName(BLANK_STRING);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(ACCOUNT_HOLDER_NAME_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenAccountTypeNotFound_ShouldReturnError() {
        paymentDetailsWsDTO.setAccountType(null);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(ACCOUNT_TYPE_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenAccountTypeEmpty_ShouldReturnError() {
        when(enumerationServiceMock.getEnumerationValue(AchAccountType.class, "  ")).thenThrow(new UnknownIdentifierException("Account type not found"));
        paymentDetailsWsDTO.setAccountType(BLANK_STRING);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(ACCOUNT_TYPE_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenAccountTypeInvalid_ShouldReturnError() {
        when(enumerationServiceMock.getEnumerationValue(AchAccountType.class, CORPORATE_ACCOUNT_TYPE)).thenThrow(new UnknownIdentifierException("Account type not found"));

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(ACCOUNT_TYPE_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenAccountNumberNotFound_ShouldReturnError() {
        paymentDetailsWsDTO.setAccountNumber(null);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(ACCOUNT_NUMBER_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenAccountNumberEmpty_ShouldReturnError() {
        paymentDetailsWsDTO.setAccountNumber(BLANK_STRING);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(ACCOUNT_NUMBER_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenAccountNumberTooShort_ShouldReturnError() {
        paymentDetailsWsDTO.setAccountNumber("123");

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(ACCOUNT_NUMBER_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenAccountNumberTooLong_ShouldReturnError() {
        paymentDetailsWsDTO.setAccountNumber("111222333444444455");

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(ACCOUNT_NUMBER_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenAccountNumberContainsCharacters_ShouldReturnError() {
        paymentDetailsWsDTO.setAccountNumber("32ffds33");

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(ACCOUNT_NUMBER_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenRoutingNumberNotFound_ShouldReturnError() {
        paymentDetailsWsDTO.setRoutingNumber(null);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(ROUTING_NUMBER_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenRoutingNumberEmpty_ShouldReturnError() {
        paymentDetailsWsDTO.setRoutingNumber(BLANK_STRING);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(ROUTING_NUMBER_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenRoutingNotValid_ShouldReturnError() {
        paymentDetailsWsDTO.setRoutingNumber("233dda");

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(ROUTING_NUMBER_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenCompanyNameNotFound_ShouldReturnError() {
        paymentDetailsWsDTO.setCompanyName(null);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(COMPANY_NAME_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenCompanyNameEmpty_ShouldReturnError() {
        paymentDetailsWsDTO.setCompanyName(BLANK_STRING);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(COMPANY_NAME_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenCompanyNameNotValid_ShouldReturnError() {
        paymentDetailsWsDTO.setCompanyName("gsgssgsgs gsggsgsgsgsgv sggsgaggdagdajas sgagasgjdgjadgjasgjdasgj gsagdgas");

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(COMPANY_NAME_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenBankCodeNotFound_ShouldReturnError() {
        paymentDetailsWsDTO.setBankCode(null);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(BANK_CODE_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenBankCodeEmpty_ShouldReturnError() {
        paymentDetailsWsDTO.setBankCode(BLANK_STRING);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(BANK_CODE_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenBankCodeNotValid_ShouldReturnError() {
        paymentDetailsWsDTO.setBankCode("233dda");

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(BANK_CODE_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenPaymentMethodNotFound_ShouldReturnError() {
        paymentDetailsWsDTO.setPaymentMethod(null);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(PAYMENT_METHOD_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenPaymentMethodEmpty_ShouldReturnError() {
        paymentDetailsWsDTO.setPaymentMethod(BLANK_STRING);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(PAYMENT_METHOD_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenEverythingIsCorrect_ShouldNotReturnErrors() {
        testObj.validate(paymentDetailsWsDTO, errors);

        assertFalse(errors.hasErrors());
    }
}
