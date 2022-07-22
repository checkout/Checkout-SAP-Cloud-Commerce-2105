package com.checkout.hybris.addon.controllers;

/**
 * Checkout.com Controllers constants
 */
public interface CheckoutaddonControllerConstants {
    /**
     * Class with view name constants
     */
    interface Views {

        interface Pages {

            interface MultiStepCheckout {
                String CheckoutSummaryPage = "pages/checkout/multi/checkoutComSummaryPage";
            }

            interface Guest {
                String CheckoutGuestOrderPage = "pages/guest/checkoutComGuestOrderPage";
                String CheckoutGuestOrderErrorPage = "pages/guest/guestOrderErrorPage";
            }
        }

        interface Fragments {

            interface CheckoutPaymentFrames {
                String CheckoutComPaymentButtonsPage = "pages/checkout/frames/checkoutComPaymentButtonsPage";
            }
        }
    }
}