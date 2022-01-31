package com.checkout.hybris.facades.payment.attributes.strategies.impl;

import com.checkout.hybris.core.enums.SepaPaymentType;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.facades.payment.attributes.mapper.CheckoutComPaymentAttributesStrategyMapper;
import com.checkout.hybris.facades.payment.attributes.strategies.CheckoutComPaymentAttributeStrategy;
import de.hybris.platform.core.model.enumeration.EnumerationValueModel;
import de.hybris.platform.servicelayer.type.TypeService;
import org.springframework.ui.Model;

import java.util.HashMap;

/**
 * Implementation of {@link CheckoutComPaymentAttributeStrategy} for sepa payment
 */
public class CheckoutComSepaPaymentAttributeStrategy extends CheckoutComAbstractPaymentAttributeStrategy {

    protected static final String SEPA_PAYMENT_TYPES_MODEL_ATTRIBUTE = "sepaPaymentTypes";

    protected final TypeService typeService;

    public CheckoutComSepaPaymentAttributeStrategy(final CheckoutComPaymentAttributesStrategyMapper checkoutComPaymentAttributesStrategyMapper,
                                                   final TypeService typeService) {
        super(checkoutComPaymentAttributesStrategyMapper);
        this.typeService = typeService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPaymentAttributeToModel(final Model model) {
        model.addAttribute(SEPA_PAYMENT_TYPES_MODEL_ATTRIBUTE, getSepaPaymentTypes());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CheckoutComPaymentType getStrategyKey() {
        return CheckoutComPaymentType.SEPA;
    }

    /**
     * Gets Sepa payment types with code and name
     *
     * @return the map with sepa payment type code and name
     */
    protected HashMap<String, String> getSepaPaymentTypes() {
        final HashMap<String, String> enumCodeNameMap = new HashMap<>();

        typeService.getEnumerationTypeForCode(SepaPaymentType._TYPECODE).getValues()
                .forEach(enumeration -> {
                    final EnumerationValueModel enumValue = (EnumerationValueModel) enumeration;
                    enumCodeNameMap.put(enumValue.getCode(), enumValue.getName());
                });

        return enumCodeNameMap;
    }
}
