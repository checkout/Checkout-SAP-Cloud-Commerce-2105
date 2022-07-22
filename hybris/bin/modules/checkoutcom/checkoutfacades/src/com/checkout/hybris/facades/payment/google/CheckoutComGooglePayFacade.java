package com.checkout.hybris.facades.payment.google;

import com.checkout.hybris.facades.beans.GooglePayIntermediatePaymentData;
import com.checkout.hybris.facades.beans.GooglePayMerchantConfigurationData;
import com.checkout.hybris.facades.beans.GooglePayPaymentDataRequestUpdate;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;

/**
 * Facade for Google Pay functionalities.
 */
public interface CheckoutComGooglePayFacade {

    /**
     * Create a Google payment request for the session cart.
     *
     * @return a Google Pay payment merchant configuration.
     */
    GooglePayMerchantConfigurationData getGooglePayMerchantConfiguration();

    /**
     * Create Google Pay update payment data request.
     *
     * @return a Google Pay update payment data request with total cart amount updated
     */
    GooglePayPaymentDataRequestUpdate getGooglePayPaymentDataRequestUpdate();

    /**
     * Create Google Pay update payment data request with delivery address and payment modes
     *
     * @param googlePayIntermediatePaymentData intermediate payment data for Google Pay
     * @return a Google Pay update payment data with delivery address and delivery methods available
     */
    GooglePayPaymentDataRequestUpdate getGooglePayPaymentDataRequestUpdate(GooglePayIntermediatePaymentData googlePayIntermediatePaymentData);

    /**
     * Create Google Pay update payment data request with delivery info
     *
     * @param googlePayIntermediatePaymentData It will contains the intermediate shipping address returned by Google Pay
     * @return a Google Pay update payment data request validating the intermediate address and returning the delivery methods available
     */
    GooglePayPaymentDataRequestUpdate getGooglePayDeliveryInfo(GooglePayIntermediatePaymentData googlePayIntermediatePaymentData) throws DuplicateUidException;
}
