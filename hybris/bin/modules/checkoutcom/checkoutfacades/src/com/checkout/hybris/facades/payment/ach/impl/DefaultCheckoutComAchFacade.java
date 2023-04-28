package com.checkout.hybris.facades.payment.ach.impl;

import com.checkout.hybris.facades.beans.AchBankInfoDetailsData;
import com.checkout.hybris.facades.beans.AchPaymentInfoData;
import com.checkout.hybris.facades.payment.CheckoutComPaymentInfoFacade;
import com.checkout.hybris.facades.payment.ach.CheckoutComAchFacade;

public class DefaultCheckoutComAchFacade implements CheckoutComAchFacade {

    protected static final String ACH = "ACH";

    private final CheckoutComPaymentInfoFacade checkoutComPaymentInfoFacade;

    public DefaultCheckoutComAchFacade(final CheckoutComPaymentInfoFacade checkoutComPaymentInfoFacade) {
        this.checkoutComPaymentInfoFacade = checkoutComPaymentInfoFacade;
    }

    @Override
    public void setPaymentInfoAchToCart(final AchBankInfoDetailsData achBankInfoDetailsData) {
        final AchPaymentInfoData achPaymentInfoData = (AchPaymentInfoData) checkoutComPaymentInfoFacade.createPaymentInfoData(ACH);
        achPaymentInfoData.setAccountType(achBankInfoDetailsData.getAccountType());
        achPaymentInfoData.setAccountNumber(achBankInfoDetailsData.getAccountNumber());
        achPaymentInfoData.setRoutingNumber(achBankInfoDetailsData.getBankRouting());
        achPaymentInfoData.setBankCode(achBankInfoDetailsData.getBankRouting());
        achPaymentInfoData.setPaymentMethod(ACH);
        achPaymentInfoData.setAccountHolderName(achBankInfoDetailsData.getAccountHolderName());
        achPaymentInfoData.setCompanyName(achBankInfoDetailsData.getCompanyName());
        achPaymentInfoData.setMask(achBankInfoDetailsData.getMask());

        checkoutComPaymentInfoFacade.addPaymentInfoToCart(achPaymentInfoData);
    }
}
