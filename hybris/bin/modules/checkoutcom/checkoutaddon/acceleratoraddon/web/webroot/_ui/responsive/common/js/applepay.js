ACC.applePay = {
    _autoload: [
        'init'
    ],

    init: function () {
        const checkbox = $('#paymentMethod_applePayComponent');

        if (checkbox.length === 0 || !window.ApplePaySession || !ApplePaySession.canMakePayments()) {
            this.applePayNotSupported();
            return;
        }

        ACC.applePay = Object.assign(this, window.applePaySettings);
        $('#paymentMethod_applePayComponent').next().show();
    },

    enablePaymentFlow: function () {
        /* hide the delivery address */
        $('[for="coUseDeliveryAddress"]').closest('.checkbox').hide();
        $('#coBillingCountrySelector').hide();
        $('#coBillingAddress').hide();

        /* hide the checkout next button */
        $('.checkout-next').hide();

        /* show the Apple Pay button*/
        this.showOrCreateButton();
    },

    applePayNotSupported: function () {
        $('#paymentMethod_applePayComponent').hide()
            .next().hide();

        ACC.checkoutCom.resetPaymentMethod();
    },

    showOrCreateButton: function () {
        let button = $('.applePayButton');
        if (button.length === 0) {
            button = $('<a lang="en" class="applePayButton" style="-webkit-appearance: -apple-pay-button; -apple-pay-button-type:plain; -apple-pay-button-style: black; height: 48px;" title="Start Apple Pay" role="link" tabindex="0"></a>');
            button.on('click', this.createSession.bind(this));
            $('.checkout-next').after(button);
        }

        if ($('.gpay-button').length > 0) {
            $('.gpay-button').hide();
        }
        button.show();
    },

    createSession: function () {
        const request = this.paymentRequest;

        try {
            this.session = new ApplePaySession(5, request);
        } catch (err) {
            this.applePayNotSupported();

            return;
        }

        this.session.oncancel = function () {
            console.log('cancelled payment');
            ACC.checkoutCom.pageSpinner.end();
            this.showPaymentError();
        }.bind(this);

        this.session.onvalidatemerchant = function (event) {
            this.performAuthorizeMerchantRequest(event.validationURL)
                .then(function (merchantSession) {
                    this.session.completeMerchantValidation(merchantSession);
                }.bind(this))
                .catch(this.showPaymentError.bind(this));
        }.bind(this);

        this.session.onpaymentauthorized = function (event) {
            console.log('Authorized payment by customer', event);

            this.performAuthorizePaymentRequest(event.payment)
                .then(function (response) {
                    console.log('Authorized payment by backend', response);
                    const statusCode = response.status === 'SUCCESS' ?
                        ApplePaySession.STATUS_SUCCESS :
                        ApplePaySession.STATUS_FAILURE;

                    this.session.completePayment({
                        status: statusCode
                    });

                    if (statusCode === ApplePaySession.STATUS_SUCCESS) {
                        ACC.checkoutCom.redirectToConfirmationPage(response.orderData);
                    } else {
                        ACC.checkoutCom.pageSpinner.end();
                        throw new Error(response.errorMessage);
                    }
                }.bind(this))
                .catch(this.showPaymentError.bind(this));
        }.bind(this);

        ACC.checkoutCom.pageSpinner.start();

        this.session.begin();
    },

    performAuthorizeMerchantRequest: function (validationURL) {
        return new Promise(function (resolve, reject) {
            $.ajax({
                type: 'POST',
                url: ACC.config.encodedContextPath + '/checkout/payment/checkout-com/applepay/request-session',
                data: JSON.stringify({validationURL: validationURL}),
                dataType: 'json',
                contentType: 'application/json',
                success: resolve,
                error: reject
            });
        });
    },

    performAuthorizePaymentRequest: function (payment) {
        return new Promise(function (resolve, reject) {
            var shippingContact = payment.shippingContact;
            var billingAddress = payment.billingContact;
            var tokePaymentData = payment.token.paymentData;

            var applePayAuthorisationRequest = {
                shippingAddress: shippingContact,
                billingContact: billingAddress,
                token: {
                    paymentData: tokePaymentData
                }
            };

            $.ajax({
                type: 'POST',
                url: ACC.config.encodedContextPath + '/checkout/payment/checkout-com/applepay/placeApplePayOrder',
                data: JSON.stringify(applePayAuthorisationRequest),
                dataType: 'json',
                contentType: 'application/json',
                success: resolve,
                error: reject,
                always: function () {
                    ACC.checkoutCom.pageSpinner.end();
                },
                timeout: 30000 /* call this.session.completePayment() within 30 seconds or the payment is cancelled */
            });
        });
    },

    showPaymentError: function (errorMessage) {
        const container = $('.main__inner-wrapper');
        const globalAlerts = container.find('> .global-alerts').length >= 1
            ? container.find('> .global-alerts')
            : container.prepend($('<div class="global-alerts"></div>'))
                .find('> .global-alerts');

        globalAlerts.find('.alert.alert-danger').remove();

        var paymentFailureError = errorMessage;
        if (errorMessage === undefined || errorMessage === '') {
            paymentFailureError = ACC.applePay.paymentCancelled;
        }

        $('<div class="alert alert-danger">' + paymentFailureError + '</div>')
            .prependTo(globalAlerts);
    }

};
