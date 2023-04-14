package com.checkout.hybris.facades.payment.ach.impl;

import com.checkout.hybris.facades.beans.AchBankInfoDetailsData;
import com.checkout.hybris.facades.beans.AchPaymentInfoData;
import com.checkout.hybris.facades.payment.CheckoutComPaymentInfoFacade;
import de.hybris.bootstrap.annotations.UnitTest;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComAchFacadeTest {

    private static final String UBS = "UBS";
    private static final String ACH = "ACH";
    private static final String MASK = "4440";
    private static final String SAVINGS = "SAVINGS";
    private static final String BANK_ROUTING = "7823892";
    private static final String MIKE_HAMMER = "Mike Hammer";
    private static final String ACCOUNT_NUMBER = "123890190222";

    @InjectMocks
    private DefaultCheckoutComAchFacade testObj;

    @Mock
    private CheckoutComPaymentInfoFacade checkoutComPaymentInfoFacadeMock;

    @Test
    public void setPaymentInfoAchToCart_shouldCreateAnAchPaymentInfoDataAndCallAddPaymentInfoToCartWithIt() {
        final AchBankInfoDetailsData achBankInfoDetailsData = createBankInfoDetailsData();
        final AchPaymentInfoData achPaymentInfoData = createPaymentInfoData();

        testObj.setPaymentInfoAchToCart(achBankInfoDetailsData);

        assertThat(achPaymentInfoData.getPaymentMethod()).isEqualTo(ACH);
        assertThat(achPaymentInfoData.getAccountType()).isEqualTo(SAVINGS);
        assertThat(achPaymentInfoData.getAccountNumber()).isEqualTo(ACCOUNT_NUMBER);
        assertThat(achPaymentInfoData.getRoutingNumber()).isEqualTo(BANK_ROUTING);
        assertThat(achPaymentInfoData.getBankCode()).isEqualTo(BANK_ROUTING);
        assertThat(achPaymentInfoData.getAccountHolderName()).isEqualTo(MIKE_HAMMER);
        assertThat(achPaymentInfoData.getMask()).isEqualTo(MASK);
        verify(checkoutComPaymentInfoFacadeMock).addPaymentInfoToCart(achPaymentInfoData);
    }

    private AchPaymentInfoData createPaymentInfoData() {
        final AchPaymentInfoData achPaymentInfoData = new AchPaymentInfoData();
        when(checkoutComPaymentInfoFacadeMock.createPaymentInfoData(ACH)).thenReturn(achPaymentInfoData);
        return achPaymentInfoData;
    }

    private AchBankInfoDetailsData createBankInfoDetailsData() {
        final AchBankInfoDetailsData achBankInfoDetailsData = new AchBankInfoDetailsData();
        achBankInfoDetailsData.setInstitutionName(UBS);
        achBankInfoDetailsData.setAccountNumber(ACCOUNT_NUMBER);
        achBankInfoDetailsData.setMask(MASK);
        achBankInfoDetailsData.setBankRouting(BANK_ROUTING);
        achBankInfoDetailsData.setAccountHolderName(MIKE_HAMMER);
        achBankInfoDetailsData.setAccountType(SAVINGS);
        return achBankInfoDetailsData;
    }
}
