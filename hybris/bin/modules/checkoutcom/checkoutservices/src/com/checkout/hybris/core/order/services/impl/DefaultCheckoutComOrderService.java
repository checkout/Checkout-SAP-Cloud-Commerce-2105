package com.checkout.hybris.core.order.services.impl;

import com.checkout.hybris.core.order.services.CheckoutComOrderService;
import de.hybris.platform.commerceservices.order.CommerceCheckoutService;
import de.hybris.platform.commerceservices.service.data.CommerceCheckoutParameter;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;

import java.math.BigDecimal;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Default implementation of the {@link CheckoutComOrderService}
 */
public class DefaultCheckoutComOrderService implements CheckoutComOrderService {

    protected final CommerceCheckoutService commerceCheckoutService;

    public DefaultCheckoutComOrderService(CommerceCheckoutService commerceCheckoutService) {
        this.commerceCheckoutService = commerceCheckoutService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommerceCheckoutParameter createCommerceCheckoutParameter(final AbstractOrderModel abstractOrderModel, final PaymentInfoModel paymentInfoModel, final BigDecimal authorisationAmount) {
        validateParameterNotNull(paymentInfoModel, "PaymentInfoModel cannot be null");
        validateParameterNotNull(abstractOrderModel, "AbstractOrderModel cannot be null");
        validateParameterNotNull(authorisationAmount, "Authorisation amount cannot be null");

        final CommerceCheckoutParameter parameter = new CommerceCheckoutParameter();
        parameter.setEnableHooks(true);
        if (abstractOrderModel instanceof CartModel) {
            parameter.setCart((CartModel) abstractOrderModel);
        }
        parameter.setPaymentInfo(paymentInfoModel);
        parameter.setAuthorizationAmount(authorisationAmount);
        parameter.setPaymentProvider(commerceCheckoutService.getPaymentProvider());
        return parameter;
    }
}