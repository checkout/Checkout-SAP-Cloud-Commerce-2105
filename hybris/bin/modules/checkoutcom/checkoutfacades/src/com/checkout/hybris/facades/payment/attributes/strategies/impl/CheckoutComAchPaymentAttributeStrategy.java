package com.checkout.hybris.facades.payment.attributes.strategies.impl;

import com.checkout.hybris.core.enums.AchAccountType;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.facades.payment.attributes.mapper.CheckoutComPaymentAttributesStrategyMapper;
import com.checkout.hybris.facades.payment.attributes.strategies.CheckoutComPaymentAttributeStrategy;
import de.hybris.platform.enumeration.EnumerationService;
import org.springframework.ui.Model;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link CheckoutComPaymentAttributeStrategy} for ach payment
 */
public class CheckoutComAchPaymentAttributeStrategy extends CheckoutComAbstractPaymentAttributeStrategy {

    protected static final String ACH_ACCOUNT_TYPES_MODEL_ATTRIBUTE = "achAccountTypes";

    protected final EnumerationService enumerationService;

    public CheckoutComAchPaymentAttributeStrategy(final CheckoutComPaymentAttributesStrategyMapper checkoutComPaymentAttributesStrategyMapper,
                                                  final EnumerationService enumerationService) {
        super(checkoutComPaymentAttributesStrategyMapper);
        this.enumerationService = enumerationService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPaymentAttributeToModel(final Model model) {
        model.addAttribute(ACH_ACCOUNT_TYPES_MODEL_ATTRIBUTE, getAchAccountTypeCodes());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CheckoutComPaymentType getStrategyKey() {
        return CheckoutComPaymentType.ACH;
    }

    /**
     * Gets the ACH account type codes from the given enumeration
     *
     * @return the list of account type codes
     */
    public List<String> getAchAccountTypeCodes() {
        final List<AchAccountType> enumerationValues = enumerationService.getEnumerationValues(AchAccountType.class);

        return enumerationValues.isEmpty() ? Collections.emptyList() : enumerationValues.stream().map(AchAccountType::getCode).collect(Collectors.toList());
    }
}
