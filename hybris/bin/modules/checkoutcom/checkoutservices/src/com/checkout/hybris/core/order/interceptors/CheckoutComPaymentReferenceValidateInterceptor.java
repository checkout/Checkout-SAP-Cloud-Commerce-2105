package com.checkout.hybris.core.order.interceptors;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;
import org.apache.commons.lang.StringUtils;

/**
 * This validate interceptor ensures that a cartModel or order contain a valid payment
 * reference before the changes are persisted
 */
public class CheckoutComPaymentReferenceValidateInterceptor implements ValidateInterceptor<CartModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void onValidate(final CartModel cartModel, final InterceptorContext context) throws InterceptorException {
        if (context.isNew(cartModel) || isPaymentReferenceModified(cartModel, context)) {
            final String paymentReference = cartModel.getCheckoutComPaymentReference();
            checkAttributeValid(paymentReference);
        }
    }

    protected boolean isPaymentReferenceModified(final CartModel cartModel, final InterceptorContext context) {
        return context.isModified(cartModel) && context.getDirtyAttributes(cartModel).containsKey(AbstractOrderModel.CHECKOUTCOMPAYMENTREFERENCE);
    }

    protected void checkAttributeValid(final String paymentReference) throws InterceptorException {
        if (StringUtils.isBlank(paymentReference)) {
            throw new InterceptorException("Payment reference cannot be null or empty.");
        }
    }

}
