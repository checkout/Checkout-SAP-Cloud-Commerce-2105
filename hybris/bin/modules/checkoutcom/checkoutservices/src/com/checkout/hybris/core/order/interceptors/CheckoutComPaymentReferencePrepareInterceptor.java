package com.checkout.hybris.core.order.interceptors;

import com.checkout.hybris.core.order.strategies.CheckoutComPaymentReferenceGenerationStrategy;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This prepare interceptor ensures that a payment reference code is generated
 * and saved into the cart or order
 */
public class CheckoutComPaymentReferencePrepareInterceptor implements PrepareInterceptor<AbstractOrderModel> {

    protected static final Logger LOG = LogManager.getLogger(CheckoutComPaymentReferencePrepareInterceptor.class);

    protected final CheckoutComPaymentReferenceGenerationStrategy paymentReferenceGenerationStrategy;

    public CheckoutComPaymentReferencePrepareInterceptor(final CheckoutComPaymentReferenceGenerationStrategy paymentReferenceGenerationStrategy) {
        this.paymentReferenceGenerationStrategy = paymentReferenceGenerationStrategy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPrepare(final AbstractOrderModel abstractOrder, final InterceptorContext context) throws InterceptorException {
        if (context.isNew(abstractOrder)) {
            LOG.debug("Setting payment reference [{}] on the order.", abstractOrder.getCode());
            abstractOrder.setCheckoutComPaymentReference(paymentReferenceGenerationStrategy.generatePaymentReference(abstractOrder));
        }
    }
}
