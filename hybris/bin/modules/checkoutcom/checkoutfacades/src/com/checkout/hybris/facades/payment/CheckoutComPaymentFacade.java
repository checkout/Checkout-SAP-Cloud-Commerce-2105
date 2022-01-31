package com.checkout.hybris.facades.payment;


import com.checkout.hybris.facades.beans.WalletPaymentAdditionalAuthInfo;
import com.checkout.hybris.facades.beans.WalletPaymentInfoData;
import com.checkout.hybris.facades.enums.WalletPaymentType;
import com.checkout.payments.GetPaymentResponse;
import de.hybris.platform.acceleratorfacades.payment.PaymentFacade;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;

import java.util.Optional;

/**
 * Facade to handle payment logic
 */
public interface CheckoutComPaymentFacade extends PaymentFacade {

    /**
     * Checks if the session cart matches the 3D secure authorized cart
     *
     * @param paymentDetails the checkout.com payment details
     * @return true if there is a match, false otherwise
     */
    boolean doesSessionCartMatchAuthorizedCart(GetPaymentResponse paymentDetails);

    /**
     * This method returns an optional with the payment details based on the ckoSessionId if the payment can be found,
     * otherwise an optional empty
     *
     * @param ckoSessionId the checkout.com session id
     * @return the payment details from Checkout.com
     */
    Optional<GetPaymentResponse> getPaymentDetailsByCkoSessionId(String ckoSessionId);

    /**
     * Creates an ssl connection factory for apple pay communication. The factory will
     * generate and use a valid keystore based on the merchant's certificates in order
     * to establish a correct ssl communication
     *
     * @return the valid connection factory
     */
    SSLConnectionSocketFactory createApplePayConnectionFactory();

    /**
     * Creates the wallet payment info data with the token got calling checkout.com generate token integration
     *
     * @param walletPaymentAdditionalAuthInfo the payload from wallet widget
     * @param walletPaymentType               the wallet payment type
     * @return WalletPaymentInfoData the payment info data populated with the request token response
     */
    WalletPaymentInfoData createCheckoutComWalletPaymentToken(WalletPaymentAdditionalAuthInfo walletPaymentAdditionalAuthInfo, WalletPaymentType walletPaymentType);
}