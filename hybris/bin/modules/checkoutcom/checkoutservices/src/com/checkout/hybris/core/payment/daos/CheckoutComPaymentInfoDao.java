package com.checkout.hybris.core.payment.daos;

import de.hybris.platform.core.model.order.payment.PaymentInfoModel;

import java.util.List;

/**
 * Dao interface to get PaymentInfos from DB
 */
public interface CheckoutComPaymentInfoDao {

    /**
     * Finds the Payment Infos for the given Checkout.com payment id
     *
     * @param paymentId the checkout.com payment id
     * @return List<PaymentInfoModel> belonging to AbstractOrder
     */
    List<PaymentInfoModel> findPaymentInfosByPaymentId(String paymentId);
}
