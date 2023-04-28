package com.checkout.hybris.facades.payment.impl;

import com.checkout.hybris.core.model.CheckoutComACHConsentModel;
import com.checkout.hybris.core.payment.ach.service.CheckoutComACHConsentService;
import com.checkout.hybris.facades.beans.AchBankInfoDetailsData;
import com.checkout.hybris.facades.payment.ach.consent.exceptions.CustomerConsentException;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComACHConsentFacadeTest {

    @InjectMocks
    private DefaultCheckoutComACHConsentFacade testObj;

    @Mock
    private CheckoutComACHConsentService checkoutComACHConsentServiceMock;
    @Mock
    private Converter<AchBankInfoDetailsData, CheckoutComACHConsentModel> checkoutComAchConsentReverseConverterMock;

    @Mock
    private AchBankInfoDetailsData achBankInfoDetailsDataMock;
    @Mock
    private CheckoutComACHConsentModel checkoutComACHConsentModelMock;

    @Test
    public void createCheckoutComACHConsent_whenCustomerConsentsIsFalse_shouldThrowCustomerConsentException() {
        final Throwable throwable = catchThrowable(() -> testObj.createCheckoutComACHConsent(achBankInfoDetailsDataMock, false));

        assertThat(throwable).isInstanceOf(CustomerConsentException.class);
    }

    @Test
    public void createCheckoutComACHConsent_whenConsentIsAccepted_shouldConvertAndSaveData() throws CustomerConsentException {
        when(checkoutComAchConsentReverseConverterMock.convert(achBankInfoDetailsDataMock)).thenReturn(checkoutComACHConsentModelMock);

        testObj.createCheckoutComACHConsent(achBankInfoDetailsDataMock, true);

        verify(checkoutComACHConsentServiceMock).saveCheckoutComACHConsent(checkoutComACHConsentModelMock);
    }
}
