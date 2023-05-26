package com.checkout.hybris.core.payment.ach.service.impl;

import com.checkout.hybris.core.model.CheckoutComACHConsentModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComACHConsentServiceTest {
    @InjectMocks
    private DefaultCheckoutComACHConsentService testObj;

    @Mock
    private ModelService modelServiceMock;

    @Mock
    private CartService cartServiceMock;

    private final CheckoutComACHConsentModel newConsent = new CheckoutComACHConsentModel();
    private final CheckoutComACHConsentModel oldConsent = new CheckoutComACHConsentModel();
    private final CartModel sessionCart = new CartModel();

    @After
    public void tearDown() throws Exception {
        sessionCart.setAchConsent(null);
    }

    @Test
    public void saveCheckoutComACHConsent_shouldNotCallModelServiceForRemoval_WhenNoConsentIsAssociatedToCart() {
        ensureCartIsInSession();

        testObj.saveCheckoutComACHConsent(newConsent);

        verify(modelServiceMock, never()).remove(any(CheckoutComACHConsentModel.class));
    }

    @Test
    public void saveCheckoutComACHConsent_shouldCallModelServiceForRemoval_WhenConsentIsAssociatedToCart() {
        ensureOldConsentIsLinkedToCart();

        testObj.saveCheckoutComACHConsent(newConsent);

        verify(modelServiceMock).remove(oldConsent);
    }

    @Test
    public void saveCheckoutComACHConsent_shouldLinkCartInSessionToNewConsent() {
        ensureCartIsInSession();

        testObj.saveCheckoutComACHConsent(newConsent);

        assertThat(newConsent.getOrder()).isEqualTo(sessionCart);
    }

    @Test
    public void saveCheckoutComACHConsent_shouldLinkNewConsentToCartInSession_WhenNoPreviousConsentExists() {
        ensureCartIsInSession();

        testObj.saveCheckoutComACHConsent(newConsent);

        assertThat(sessionCart.getAchConsent()).isEqualTo(newConsent);
    }

    @Test
    public void saveCheckoutComACHConsent_shouldLinkNewConsentToCartInSession_WhenPreviousConsentExists() {
        ensureOldConsentIsLinkedToCart();

        testObj.saveCheckoutComACHConsent(newConsent);

        assertThat(sessionCart.getAchConsent()).isEqualTo(newConsent);
    }

    @Test
    public void saveCheckoutComACHConsent_shouldCallModelServiceForSavingBothNewConsentAndSessionCartAndBothRefreshSessionCartAndConsent() {
        ensureCartIsInSession();

        testObj.saveCheckoutComACHConsent(newConsent);

        verify(modelServiceMock).save(newConsent);
        verify(modelServiceMock).save(sessionCart);
        verify(modelServiceMock).refresh(newConsent);
        verify(modelServiceMock).refresh(sessionCart);
    }

    private void ensureCartIsInSession() {
        when(cartServiceMock.getSessionCart()).thenReturn(sessionCart);
    }

    private void ensureOldConsentIsLinkedToCart() {
        ensureCartIsInSession();
        sessionCart.setAchConsent(oldConsent);
    }

}
