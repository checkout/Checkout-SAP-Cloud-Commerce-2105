package com.checkout.hybris.test.interceptor;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;
import de.hybris.platform.servicelayer.internal.dao.GenericDao;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.Map;

/**
 * This Validate interceptor ensures that a payment info is saved in an abstract order and it contains a
 * Checkout payment ID, this one is persisted in the cart for testing purposes
 */
public class CheckoutComPaymentIdValidateInterceptor implements ValidateInterceptor<PaymentInfoModel> {

    private final ModelService modelService;
    private final GenericDao<AbstractOrderModel> abstractOrderGenericDao;

    public CheckoutComPaymentIdValidateInterceptor(final ModelService modelService,
                                                   final GenericDao<AbstractOrderModel> abstractOrderGenericDao) {
        this.modelService = modelService;
        this.abstractOrderGenericDao = abstractOrderGenericDao;
    }

    @Override
    public void onValidate(final PaymentInfoModel paymentInfo, final InterceptorContext interceptorContext) {
        if (!interceptorContext.isNew(paymentInfo) && paymentInfo.getPaymentId() != null) {
            final AbstractOrderModel order = abstractOrderGenericDao.find(Map.of(AbstractOrderModel.PAYMENTINFO, paymentInfo)).stream()
                    .findFirst()
                    .orElse(null);

            if (order != null) {
                order.setCheckoutPaymentId(paymentInfo.getPaymentId());
                modelService.save(order);
            }
        }
    }
}
