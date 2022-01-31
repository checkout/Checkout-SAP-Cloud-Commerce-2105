package com.checkout.hybris.occ.validators.paymentdetailswsdto;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.user.AddressWsDTO;
import org.junit.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;


@UnitTest
public class CheckoutComOxxoPaymentDetailsWsDTOValidatorTest {

    private static final String DOCUMENT_FIELD = "document";
    private static final String BLANK_STRING = " ";
    private static final String VALID_DOCUMENT = "111111111111111111";

    private final CheckoutComOxxoPaymentDetailsWsDTOValidator testObj = new CheckoutComOxxoPaymentDetailsWsDTOValidator();

    private PaymentDetailsWsDTO paymentDetailsWsDTO = new PaymentDetailsWsDTO();

    private Errors errors = new BeanPropertyBindingResult(paymentDetailsWsDTO, paymentDetailsWsDTO.getClass().getSimpleName());

    @Test
    public void supports_WhenPaymentDetailsWsDTOType_ShouldReturnTrue() {
        assertThat(testObj.supports(PaymentDetailsWsDTO.class)).isTrue();
    }

    @Test
    public void supports_WhenNotCorrectType_ShouldReturnFalse() {
        assertThat(testObj.supports(AddressWsDTO.class)).isFalse();
    }

    @Test
    public void validate_WhenDocumentIsNull_ShouldReturnError() {
        paymentDetailsWsDTO.setDocument(null);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getErrorCount()).isEqualTo(1);
        assertThat(Objects.requireNonNull(errors.getFieldError()).getField()).isEqualTo(DOCUMENT_FIELD);
    }

    @Test
    public void validate_WhenDocumentContainsInvalidCharacters_ShouldReturnError() {
        paymentDetailsWsDTO.setDocument("12fsdfsdf%dfdsf333");

        testObj.validate(paymentDetailsWsDTO, errors);

        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getErrorCount()).isEqualTo(1);
        assertThat(Objects.requireNonNull(errors.getFieldError()).getField()).isEqualTo(DOCUMENT_FIELD);
    }

    @Test
    public void validate_WhenDocumentDoesNotMatchLength_ShouldReturnError() {
        paymentDetailsWsDTO.setDocument("12fsdfsd");

        testObj.validate(paymentDetailsWsDTO, errors);

        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getErrorCount()).isEqualTo(1);
        assertThat(Objects.requireNonNull(errors.getFieldError()).getField()).isEqualTo(DOCUMENT_FIELD);
    }

    @Test
    public void validate_WhenDocumentIsBlank_ShouldReturnError() {
        paymentDetailsWsDTO.setDocument(BLANK_STRING);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getErrorCount()).isEqualTo(1);
        assertThat(Objects.requireNonNull(errors.getFieldError()).getField()).isEqualTo(DOCUMENT_FIELD);
    }

    @Test
    public void validate_WhenDocumentIsEighteenChar_ShouldNotReturnError() {
        paymentDetailsWsDTO.setDocument(VALID_DOCUMENT);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertThat(errors.hasErrors()).isFalse();
        assertThat(errors.getErrorCount()).isZero();
    }
}
