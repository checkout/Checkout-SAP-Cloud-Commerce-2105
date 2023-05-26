package com.checkout.hybris.addon.validators.paymentform;

import com.checkout.hybris.addon.forms.PaymentDataForm;
import com.checkout.hybris.core.enums.AchAccountType;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;

/**
 * Validates ACH payment form
 */
public class CheckoutComAchPaymentDataFormValidator extends CheckoutComAbstractPaymentDataFormValidValidator {

    protected static final String ACCOUNT_HOLDER_NAME_FORM_KEY = "accountHolderName";
    protected static final String ACCOUNT_TYPE_FORM_KEY = "accountType";
    protected static final String ACCOUNT_NUMBER_FORM_KEY = "accountNumber";
    protected static final String ROUTING_NUMBER_FORM_KEY = "routingNumber";
    protected static final String COMPANY_NAME_FORM_KEY = "companyName";
    protected static final String CORPORATE_ACCOUNT_TYPE_VALUE = "Corporate";
    protected static final String CORP_SAVINGS_ACCOUNT_TYPE_VALUE = "CorpSavings";
    protected static final String PAYMENT_METHOD_FORM_KEY = "paymentMethod";
    protected static final String BANK_CODE_FORM_KEY = "bankCode";

    protected final EnumerationService enumerationService;

    public CheckoutComAchPaymentDataFormValidator(final EnumerationService enumerationService) {
        this.enumerationService = enumerationService;
    }

    /**
     * {@inheritDoc}
     */
    public boolean supports(final Class<?> clazz) {
        return PaymentDataForm.class.isAssignableFrom(clazz);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final Object form, final Errors errors) {
        final PaymentDataForm paymentDataForm = (PaymentDataForm) form;

        validateAccountHolderName(errors, paymentDataForm);
        validateAccountType(errors, paymentDataForm);
        validateAccountNumber(errors, paymentDataForm);
        validateRoutingNumber(errors, paymentDataForm);
        validatePaymentMethod(errors, paymentDataForm);
        validateBankCode(errors, paymentDataForm);
    }

    private void validateAccountHolderName(final Errors errors, final PaymentDataForm paymentDataForm) {
        if (!paymentDataForm.getFormAttributes().containsKey(ACCOUNT_HOLDER_NAME_FORM_KEY)) {
            errors.rejectValue(getAttributeToReject(ACCOUNT_HOLDER_NAME_FORM_KEY), "checkoutcom.ach.accountHolderName.mandatory");
        } else if (StringUtils.isBlank((String) paymentDataForm.getFormAttributes().get(ACCOUNT_HOLDER_NAME_FORM_KEY))) {
            errors.rejectValue(getAttributeToReject(ACCOUNT_HOLDER_NAME_FORM_KEY), "checkoutcom.ach.accountHolderName.invalid");
        }
    }

    private void validateRoutingNumber(final Errors errors, final PaymentDataForm paymentDataForm) {
        if (!paymentDataForm.getFormAttributes().containsKey(ROUTING_NUMBER_FORM_KEY)) {
            errors.rejectValue(getAttributeToReject(ROUTING_NUMBER_FORM_KEY), "checkoutcom.ach.routingnumber.mandatory");
        } else if (!isValidRoutingNumber((String) paymentDataForm.getFormAttributes().get(ROUTING_NUMBER_FORM_KEY))) {
            errors.rejectValue(getAttributeToReject(ROUTING_NUMBER_FORM_KEY), "checkoutcom.ach.routingnumber.invalid");
        }
    }

    private void validatePaymentMethod(final Errors errors, final PaymentDataForm paymentDataForm) {
        if (!paymentDataForm.getFormAttributes().containsKey(PAYMENT_METHOD_FORM_KEY)) {
            errors.rejectValue(getAttributeToReject(PAYMENT_METHOD_FORM_KEY), "checkoutcom.ach.paymentmethod.mandatory");
        } else if (StringUtils.isBlank((String) paymentDataForm.getFormAttributes().get(PAYMENT_METHOD_FORM_KEY))) {
            errors.rejectValue(getAttributeToReject(PAYMENT_METHOD_FORM_KEY), "checkoutcom.ach.paymentmethod.invalid");
        }
    }

    private void validateBankCode(final Errors errors, final PaymentDataForm paymentDataForm) {
        if (!paymentDataForm.getFormAttributes().containsKey(BANK_CODE_FORM_KEY)) {
            errors.rejectValue(getAttributeToReject(BANK_CODE_FORM_KEY), "checkoutcom.ach.bankcode.mandatory");
        } else if (!isValidBankCode((String) paymentDataForm.getFormAttributes().get(BANK_CODE_FORM_KEY))) {
            errors.rejectValue(getAttributeToReject(BANK_CODE_FORM_KEY), "checkoutcom.ach.bankcode.invalid");
        }
    }

    private void validateAccountNumber(final Errors errors, final PaymentDataForm paymentDataForm) {
        if (!paymentDataForm.getFormAttributes().containsKey(ACCOUNT_NUMBER_FORM_KEY)) {
            errors.rejectValue(getAttributeToReject(ACCOUNT_NUMBER_FORM_KEY), "checkoutcom.ach.accountnumber.mandatory");
        } else if (!isValidAccountNumber((String) paymentDataForm.getFormAttributes().get(ACCOUNT_NUMBER_FORM_KEY))) {
            errors.rejectValue(getAttributeToReject(ACCOUNT_NUMBER_FORM_KEY), "checkoutcom.ach.accountnumber.invalid");
        }
    }

    private void validateAccountType(final Errors errors, final PaymentDataForm paymentDataForm) {
        if (paymentDataForm.getFormAttributes().containsKey(ACCOUNT_TYPE_FORM_KEY)) {
            try {
                enumerationService.getEnumerationValue(AchAccountType.class, (String) paymentDataForm.getFormAttributes().get(ACCOUNT_TYPE_FORM_KEY));
            } catch (final UnknownIdentifierException e) {
                errors.rejectValue(getAttributeToReject(ACCOUNT_TYPE_FORM_KEY), "checkoutcom.ach.accounttype.invalid");
            }
            validateCompanyName(errors, paymentDataForm);

        } else {
            errors.rejectValue(getAttributeToReject(ACCOUNT_TYPE_FORM_KEY), "checkoutcom.ach.accounttype.mandatory");
        }
    }

    protected String getAttributeToReject(final String attributeKey) {
        return "formAttributes['" + attributeKey + "']";
    }

    private void validateCompanyName(final Errors errors, final PaymentDataForm paymentDataForm) {
        if (isCompanyNameRequired((String) paymentDataForm.getFormAttributes().get(ACCOUNT_TYPE_FORM_KEY))) {
            if (!paymentDataForm.getFormAttributes().containsKey(COMPANY_NAME_FORM_KEY)) {
                errors.rejectValue(getAttributeToReject(COMPANY_NAME_FORM_KEY), "checkoutcom.ach.companyname.mandatory");
            } else if (StringUtils.isBlank(((String) paymentDataForm.getFormAttributes().get(COMPANY_NAME_FORM_KEY)))
                    || ((String) paymentDataForm.getFormAttributes().get(COMPANY_NAME_FORM_KEY)).length() > 40) {
                errors.rejectValue(getAttributeToReject(COMPANY_NAME_FORM_KEY), "checkoutcom.ach.companyname.invalid");
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
