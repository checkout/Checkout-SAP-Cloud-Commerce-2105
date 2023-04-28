package com.checkout.hybris.occ.validators.paymentdetailswsdto;

import com.checkout.hybris.core.enums.AchAccountType;
import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsWsDTO;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Objects;

/**
 * Validates ACH paymentDetailsWsDTO
 */
public class CheckoutComAchPaymentDetailsWsDTOValidator implements Validator {

    private static final String ACCOUNT_HOLDER_NAME_FORM_KEY = "accountHolderName";
    private static final String ACCOUNT_TYPE_FORM_KEY = "accountType";
    private static final String ACCOUNT_NUMBER_FORM_KEY = "accountNumber";
    private static final String BANK_CODE_FORM_KEY = "bankCode";
    private static final String PAYMENT_METHOD_FORM_KEY = "paymentMethod";
    private static final String ROUTING_NUMBER_FORM_KEY = "routingNumber";
    private static final String COMPANY_NAME_FORM_KEY = "companyName";
    private static final String CORPORATE_ACCOUNT_TYPE_VALUE = "Corporate";
    private static final String CORP_SAVINGS_ACCOUNT_TYPE_VALUE = "CorpSavings";
    private static final String HOLDER_NAME_MANDATORY = "checkoutcom.occ.ach.accountHolderName.mandatory";
    private static final String HOLDER_NAME_INVALID = "checkoutcom.occ.ach.accountHolderName.invalid";
    private static final String PAYMENTMETHOD_MANDATORY = "checkoutcom.occ.ach.paymentmethod.mandatory";
    private static final String PAYMENTMETHOD_INVALID = "checkoutcom.occ.ach.paymentmethod.invalid";
    private static final String BANKCODE_MANDATORY = "checkoutcom.occ.ach.bankcode.mandatory";
    private static final String BANKCODE_INVALID = "checkoutcom.occ.ach.bankcode.invalid";
    private static final String ROUTINGNUMBER_MANDATORY = "checkoutcom.occ.ach.routingnumber.mandatory";
    private static final String ROUTINGNUMBER_INVALID = "checkoutcom.occ.ach.routingnumber.invalid";
    private static final String ACCOUNTNUMBER_MANDATORY = "checkoutcom.occ.ach.accountnumber.mandatory";
    private static final String ACCOUNTNUMBER_INVALID = "checkoutcom.occ.ach.accountnumber.invalid";
    private static final String ACCOUNTTYPE_INVALID = "checkoutcom.occ.ach.accounttype.invalid";
    private static final String ACCOUNTTYPE_MANDATORY = "checkoutcom.occ.ach.accounttype.mandatory";
    private static final String COMPANYNAME_MANDATORY = "checkoutcom.occ.ach.companyname.mandatory";
    private static final String COMPANYNAME_INVALID = "checkoutcom.occ.ach.companyname.invalid";

    protected final EnumerationService enumerationService;

    public CheckoutComAchPaymentDetailsWsDTOValidator(final EnumerationService enumerationService) {
        this.enumerationService = enumerationService;
    }

    /**
     * {@inheritDoc}
     */
    public boolean supports(final Class<?> clazz) {
        return PaymentDetailsWsDTO.class.isAssignableFrom(clazz);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final Object form, final Errors errors) {
        final PaymentDetailsWsDTO paymentDetailsWsDTO = (PaymentDetailsWsDTO) form;

        validateAccountHolderName(errors, paymentDetailsWsDTO);
        validateAccountType(errors, paymentDetailsWsDTO);
        validateAccountNumber(errors, paymentDetailsWsDTO);
        validatePaymentMethod(errors, paymentDetailsWsDTO);
        validateBankCode(errors, paymentDetailsWsDTO);
        validateRoutingNumber(errors, paymentDetailsWsDTO);
    }

    private void validateAccountHolderName(final Errors errors, final PaymentDetailsWsDTO paymentDetailsWsDTO) {
        if (Objects.isNull(paymentDetailsWsDTO.getAccountHolderName())) {
            errors.rejectValue(ACCOUNT_HOLDER_NAME_FORM_KEY, HOLDER_NAME_MANDATORY);
        } else if (StringUtils.isBlank(paymentDetailsWsDTO.getAccountHolderName())) {
            errors.rejectValue(ACCOUNT_HOLDER_NAME_FORM_KEY, HOLDER_NAME_INVALID);
        }
    }

    private void validateRoutingNumber(final Errors errors, final PaymentDetailsWsDTO paymentDetailsWsDTO) {
        if (Objects.isNull(paymentDetailsWsDTO.getRoutingNumber())) {
            errors.rejectValue(ROUTING_NUMBER_FORM_KEY, ROUTINGNUMBER_MANDATORY);
        } else if (!isValidRoutingNumber(paymentDetailsWsDTO.getRoutingNumber())) {
            errors.rejectValue(ROUTING_NUMBER_FORM_KEY, ROUTINGNUMBER_INVALID);
        }
    }

    private void validatePaymentMethod(final Errors errors, final PaymentDetailsWsDTO paymentDetailsWsDTO) {
        if (Objects.isNull(paymentDetailsWsDTO.getPaymentMethod())) {
            errors.rejectValue(PAYMENT_METHOD_FORM_KEY, PAYMENTMETHOD_MANDATORY);
        } else if (StringUtils.isBlank(paymentDetailsWsDTO.getPaymentMethod())) {
            errors.rejectValue(PAYMENT_METHOD_FORM_KEY, PAYMENTMETHOD_INVALID);
        }
    }

    private void validateBankCode(final Errors errors, final PaymentDetailsWsDTO paymentDetailsWsDTO) {
        if (Objects.isNull(paymentDetailsWsDTO.getBankCode())) {
            errors.rejectValue(BANK_CODE_FORM_KEY, BANKCODE_MANDATORY);
        } else if (!isValidBankCode(paymentDetailsWsDTO.getBankCode())) {
            errors.rejectValue(BANK_CODE_FORM_KEY, BANKCODE_INVALID);
        }
    }

    private void validateAccountNumber(final Errors errors, final PaymentDetailsWsDTO paymentDetailsWsDTO) {
        if (Objects.isNull(paymentDetailsWsDTO.getAccountNumber())) {
            errors.rejectValue(ACCOUNT_NUMBER_FORM_KEY, ACCOUNTNUMBER_MANDATORY);
        } else if (!isValidAccountNumber(paymentDetailsWsDTO.getAccountNumber())) {
            errors.rejectValue(ACCOUNT_NUMBER_FORM_KEY, ACCOUNTNUMBER_INVALID);
        }
    }

    private void validateAccountType(final Errors errors, final PaymentDetailsWsDTO paymentDetailsWsDTO) {
        if (!StringUtils.isBlank(paymentDetailsWsDTO.getAccountType())) {
            try {
                enumerationService.getEnumerationValue(AchAccountType.class, (String) paymentDetailsWsDTO.getAccountType());
            } catch (final UnknownIdentifierException e) {
                errors.rejectValue(ACCOUNT_TYPE_FORM_KEY, ACCOUNTTYPE_INVALID);
            }
            validateCompanyName(errors, paymentDetailsWsDTO);

        } else {
            errors.rejectValue(ACCOUNT_TYPE_FORM_KEY, ACCOUNTTYPE_MANDATORY);
        }
    }

    private void validateCompanyName(final Errors errors, final PaymentDetailsWsDTO paymentDetailsWsDTO) {
        if (isCompanyNameRequired(paymentDetailsWsDTO.getAccountType())) {
            if (Objects.isNull(paymentDetailsWsDTO.getCompanyName())) {
                errors.rejectValue(COMPANY_NAME_FORM_KEY, COMPANYNAME_MANDATORY);
            } else if (StringUtils.isBlank((paymentDetailsWsDTO.getCompanyName()))
                    || (paymentDetailsWsDTO.getCompanyName()).length() > 40) {
                errors.rejectValue(COMPANY_NAME_FORM_KEY, COMPANYNAME_INVALID);
            }
        }
    }

    /**
     * Checks if the account number has a valid length
     *
     * @param accountNumber the account number string
     * @return true if valid, false otherwise
     */
    protected boolean isValidAccountNumber(final String accountNumber) {
        return accountNumber.matches("^[0-9]{4,17}$");
    }

    /**
     * Checks if the account routing number contains at least one number
     *
     * @param routingNumber the account routing string
     * @return true if valid, false otherwise
     */
    protected boolean isValidRoutingNumber(final String routingNumber) {
        return routingNumber.matches("\\d+");
    }

    /**
     * Checks if the company name is required. It is mandatory when the account type has value Corporate or CorpSavings
     *
     * @param accountType the account type
     * @return true if required, false otherwise
     */
    protected boolean isCompanyNameRequired(final String accountType) {
        return accountType.equalsIgnoreCase(CORPORATE_ACCOUNT_TYPE_VALUE) || accountType.equalsIgnoreCase(CORP_SAVINGS_ACCOUNT_TYPE_VALUE);
    }

    /**
     * Checks if the account bank code contains at least one number
     *
     * @param bankCode the bank code string
     * @return true if valid, false otherwise
     */
    protected boolean isValidBankCode(final String bankCode) {
        return bankCode.matches("\\d+");
    }
}
