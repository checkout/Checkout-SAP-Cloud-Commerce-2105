package com.checkout.hybris.core.order.interceptors;

import de.hybris.bootstrap.annotations.IntegrationTest;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.ServicelayerTest;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.model.ModelService;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import javax.annotation.Resource;

@IntegrationTest
public class CheckoutComPaymentReferenceValidateInterceptorIntegrationTest extends ServicelayerTest {

    private static final String PAYMENT_REFERENCE = "paymentReference";

    @Resource
    private CartService cartService;
    @Resource
    private ModelService modelService;

    @Test(expected = ModelSavingException.class)
    public void setEmptyPaymentReference_shouldThrowModelSavingException() {
        final CartModel sessionCart = cartService.getSessionCart();
        sessionCart.setCheckoutComPaymentReference(" ");
        modelService.save(sessionCart);
    }

    @Test(expected = ModelSavingException.class)
    public void setNullPaymentReference_shouldThrowModelSavingException() {
        final CartModel sessionCart = cartService.getSessionCart();
        sessionCart.setCheckoutComPaymentReference(null);
        modelService.save(sessionCart);
    }

    @Test
    public void setValidPaymentReference_shouldSaveValidReference() {
        final CartModel sessionCart = cartService.getSessionCart();
        sessionCart.setCheckoutComPaymentReference(PAYMENT_REFERENCE);
        modelService.save(sessionCart);
        Assertions.assertThat(sessionCart.getCheckoutComPaymentReference()).isEqualTo(PAYMENT_REFERENCE);
    }
}
