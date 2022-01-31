package com.checkout.hybris.core.order.daos;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.order.daos.OrderDao;

import java.util.Optional;

/**
 * Dao interface to get Orders from DB
 */
public interface CheckoutComOrderDao extends OrderDao {

    /**
     * Find the Abstract Order for the given reference number
     *
     * @param paymentReference the checkout.com payment reference number
     * @return Optional<AbstractOrderModel>
     */
    Optional<AbstractOrderModel> findAbstractOrderForPaymentReferenceNumber(String paymentReference);
}
