package com.checkout.hybris.facades.payment.plaidlink;

import com.checkout.hybris.facades.beans.AchBankInfoDetailsData;
import com.checkout.hybris.facades.beans.PlaidLinkCreationResponse;

import java.io.IOException;

/**
 * Facade for plaidLinkIntegration
 */
public interface CheckoutComPlaidLinkFacade {

  /**
   * Creates a token for the customer linked to the session's cart
   *
   * @return Response of the token creation
   * @throws IOException
   */
  String linkTokenCreate() throws IOException;

  /**
   * Queries the bank account details by using the provided public token
   *
   * @param plaidLinkCreationResponse the plaidLinkCreationResponse
   * @return Bank information related to the given token
   * @throws IOException
   */
  AchBankInfoDetailsData getBankAccountDetailsData(PlaidLinkCreationResponse plaidLinkCreationResponse) throws IOException;
}
