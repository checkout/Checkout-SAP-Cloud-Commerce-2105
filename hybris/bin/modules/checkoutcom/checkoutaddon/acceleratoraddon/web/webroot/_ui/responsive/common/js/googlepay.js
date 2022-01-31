ACC.googlePay = {
    _autoload: [
        ["init", $('#paymentMethod_googlePayComponent').length > 0]
    ],

    paymentClient: null,

    baseRequest: {
        apiVersion: 2,
        apiVersionMinor: 0
    },

    init: function () {
        $('label[for=paymentMethod_googlePayComponent]').hide();

        $.getScript('https://pay.google.com/gp/p/js/pay.js')
            .done(this.onGooglePayLoaded.bind(this))
            .fail(this.googlePayNotSupported.bind(this));
    },

    onGooglePayLoaded: function () {
        ACC.googlePay = Object.assign(this, window.googlePaySettings);

        const paymentsClient = this.getGooglePaymentsClient();
        const isReadyToPayRequest = this.getGoogleIsReadyToPayRequest();

        new Promise(
            function (resolve, reject) {
                paymentsClient.isReadyToPay(isReadyToPayRequest)
                    .then(function (response) {
                        if (response.result) {
                            resolve();
                        } else {
                            reject();
                        }
                    })
                    .catch(function () {
                        reject();
                    });
            }
        ).then(function () {
            $('label[for=paymentMethod_googlePayComponent]').show();
        })
            .catch(function () {
                this.googlePayNotSupported();
            }.bind(this));
    },

    enablePaymentFlow: function () {
        /* hide the delivery address */
        $('[for="coUseDeliveryAddress"]').closest('.checkbox').hide();
        $('#coBillingCountrySelector').hide();
        $('#coBillingAddress').hide();

        /* disable the checkout next button */
        $('.checkout-next').hide();

        /* show the Apple Pay button*/
        this.showOrCreateButton();
    },

    showOrCreateButton: function () {
        let button = $('.gpay-button');
        if (button.length === 0) {
            button = $(this.getGooglePaymentsClient().createButton({
                onClick: this.createSession.bind(this)
            }));
            $('.checkout-next').after(button);
        }

        if ($('.applePayButton').length > 0) {
            $('.applePayButton').hide();
        }
        button.show();
    },

    getGooglePaymentsClient: function () {
        return this.paymentsClient || (this.paymentsClient = new google.payments.api.PaymentsClient(this.clientSettings));
    },

    createSession: function () {
        const paymentsClient = this.getGooglePaymentsClient();
        const paymentDataRequest = this.getGooglePaymentDataRequest();

        paymentsClient.loadPaymentData(paymentDataRequest)
            .then(function (paymentData) {
                this.processPayment(paymentData);
            }.bind(this))
            .catch(function () {
                this.showPaymentError()
            }.bind(this));
    },

    getCardPaymentMethod: function () {
        return Object.assign({
                tokenizationSpecification: {
                    type: 'PAYMENT_GATEWAY',
                    parameters: {
                        gateway: this.gateway,
                        gatewayMerchantId: this.gatewayMerchantId
                    }
                }
            },
            this.baseCardPaymentMethod
        );
    },

    getGoogleIsReadyToPayRequest: function () {
        return Object.assign(
            this.baseRequest,
            {
                allowedPaymentMethods: [this.baseCardPaymentMethod]
            }
        );
    },
    getGooglePaymentDataRequest: function () {
        return Object.assign(this.baseRequest, {
            allowedPaymentMethods: [this.getCardPaymentMethod()],
            merchantInfo: {
                merchantName: this.merchantName,
                merchantId: this.merchantId
            },
            transactionInfo: this.transactionInfo
        });
    },

    processPayment: function (paymentData) {
        const paymentMethodData = paymentData.paymentMethodData;
        const billingAddress = paymentMethodData.info.billingAddress;
        const token = JSON.parse(paymentMethodData.tokenizationData.token);

        this.performAuthorizePaymentRequest({billingAddress: billingAddress, token: token})
            .then(function (order) {
                ACC.checkoutCom.redirectToConfirmationPage(order);
            })
            .catch(function (errorMessage) {
                ACC.checkoutCom.pageSpinner.end()
                this.showPaymentError(errorMessage);
            }.bind(this));
    },

    performAuthorizePaymentRequest: function (data) {
        return new Promise(function (resolve, reject) {
            $.ajax({
                type: 'POST',
                url: ACC.config.encodedContextPath + '/checkout/payment/checkout-com/googlepay/placeGooglePayOrder',
                data: JSON.stringify(data),
                success: function (response) {
                    if (response.status === 'SUCCESS') {
                        resolve(response.orderData);
                    } else {
                        reject(response.errorMessage);
                    }
                },
                error: function () {
                    reject();
                },
                beforeSend: function () {
                    ACC.checkoutCom.pageSpinner.start()
                },
                always: function () {
                    ACC.checkoutCom.pageSpinner.end()
                },
                dataType: 'json',
                contentType: 'application/json'
            });

        });
    },

    googlePayNotSupported: function () {
        $('#paymentMethod_applePayComponent').hide()
            .next().hide();
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
            paymentFailureError = ACC.googlePay.paymentCancelled;
        }

        $('<div class="alert alert-danger">' + paymentFailureError + '</div>')
            .prependTo(globalAlerts);
    }

};