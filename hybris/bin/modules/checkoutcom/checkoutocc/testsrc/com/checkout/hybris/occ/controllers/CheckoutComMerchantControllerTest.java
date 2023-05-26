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
    private static final String IS_ABC_FALSE = "false";

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

    @Test
    public void isMerchantABC_WhenMerchantIsABC_ShouldReturnTrue() {
        when(checkoutComMerchantConfigurationFacadeMock.isCheckoutComMerchantABC()).thenReturn(Boolean.TRUE);

        final ResponseEntity<String> result = testObj.isMerchantABC();

        assertThat(result).hasFieldOrPropertyWithValue("body", Boolean.TRUE.toString());
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void isMerchantABC_WhenMerchantIsNAS_ShouldReturnFalse() {
        when(checkoutComMerchantConfigurationFacadeMock.isCheckoutComMerchantABC()).thenReturn(Boolean.FALSE);

        final ResponseEntity<String> result = testObj.isMerchantABC();

        assertThat(result).hasFieldOrPropertyWithValue("body", IS_ABC_FALSE);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void isMerchantABC_WhenMerchantIsUndefined_ShouldReturnError() {
        when(checkoutComMerchantConfigurationFacadeMock.isCheckoutComMerchantABC()).thenReturn(null);

        final ResponseEntity<String> result = testObj.isMerchantABC();

        assertThat(result).hasFieldOrPropertyWithValue("body", IS_ABC_FALSE);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    
}
