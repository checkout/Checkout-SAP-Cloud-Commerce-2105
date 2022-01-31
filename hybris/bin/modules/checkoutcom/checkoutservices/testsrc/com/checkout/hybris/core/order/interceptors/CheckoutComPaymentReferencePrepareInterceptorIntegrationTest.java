package com.checkout.hybris.core.order.interceptors;

import de.hybris.bootstrap.annotations.IntegrationTest;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.order.OrderService;
import de.hybris.platform.servicelayer.ServicelayerTest;
import org.fest.assertions.Assertions;
import org.junit.Test;

import javax.annotation.Resource;

@IntegrationTest
public class CheckoutComPaymentReferencePrepareInterceptorIntegrationTest extends ServicelayerTest {

    @Resource
    private OrderService orderService;
    @Resource
    private CartService cartService;

    @Test
    public void cart_ShouldHavePaymentReference() {
        final CartModel cart = cartService.getSessionCart();

        Assertions.assertThat(cart.getCheckoutComPaymentReference()).startsWith(cart.getCode() + "-");
    }

    @Test
    public void orderFromCart_ShouldHaveSamePaymentReferenceAsCart() throws InvalidCartException {
        final CartModel cart = cartService.getSessionCart();
        final OrderModel order = orderService.createOrderFromCart(cart);

        Assertions.assertThat(order.getCheckoutComPaymentReference()).isEqualTo(cart.getCheckoutComPaymentReference());
    }
}