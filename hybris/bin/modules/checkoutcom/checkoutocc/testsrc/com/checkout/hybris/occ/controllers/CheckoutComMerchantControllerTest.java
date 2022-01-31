package com.checkout.hybris.occ.controllers;

import com.checkout.hybris.facades.merchant.CheckoutComMerchantConfigurationFacade;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComMerchantControllerTest {

    private static final String PUBLIC_KEY = "publicKey";

    @InjectMocks
    private CheckoutComMerchantController testObj;

    @Mock
    private CheckoutComMerchantConfigurationFacade checkoutComMerchantConfigurationFacadeMock;

    @Test
    public void getMerchantKey_WhenPublicKeyIsNotEmpty_ShouldReturnMerchantPublicKey() {
        when(checkoutComMerchantConfigurationFacadeMock.getCheckoutComMerchantPublicKey()).thenReturn(PUBLIC_KEY);

        final ResponseEntity<String> result = testObj.getMerchantKey();

        assertThat(result).hasFieldOrPropertyWithValue("body", PUBLIC_KEY);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getMerchantKey_WhenPublicKeyIsEmpty_ShouldReturnError() {
        when(checkoutComMerchantConfigurationFacadeMock.getCheckoutComMerchantPublicKey()).thenReturn("");

        final ResponseEntity<String> result = testObj.getMerchantKey();

        assertThat(result).hasFieldOrPropertyWithValue("body", "Merchant key is empty or null");
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
