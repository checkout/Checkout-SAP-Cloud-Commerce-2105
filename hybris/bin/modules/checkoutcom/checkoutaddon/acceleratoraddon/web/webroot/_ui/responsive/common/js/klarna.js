ACC.klarna = {
    _autoload: [
        'onKlarnaInit',
        'downloadKlarnaScript'
    ],

    downloadKlarnaScript: function () {
        if (ACC.klarna.paymentMethod === 'KLARNA') {
            $.getScript('https://x.klarnacdn.net/kp/lib/v1/api.js')
                .done(ACC.klarna.bindKlarnaFormSubmit)
                .fail(ACC.klarna.showKlarnaError);
        }
    },

    onKlarnaInit: function () {
        ACC.klarna = Object.assign(this, window.klarnaSettings);
        if (ACC.klarna.paymentMethod === 'KLARNA') {
            window.klarnaAsyncCallback = function () {
                //init
                try {
                    Klarna.Payments.init({
                            client_token: ACC.klarna.clientToken
                        }
                    );
                } catch (e) {
                    ACC.klarna.showKlarnaError();
                }

                //load
                try {
                    Klarna.Payments.load({
                            container: "#klarnaContainer",
                            payment_method_categories: ACC.klarna.paymentMethodCategories,
                            instance_id: ACC.klarna.klarnaInstanceId
                        },
                        // data
                        {
                            billing_address: {
                                given_name: ACC.klarna.billingAddress.givenName,
                                family_name: ACC.klarna.billingAddress.familyName,
                                email: ACC.klarna.billingAddress.emailAddress,
                                title: ACC.klarna.billingAddress.title,
                                street_address: ACC.klarna.billingAddress.addressLine1,
                                street_address2: ACC.klarna.billingAddress.addressLine2,
                                postal_code: ACC.klarna.billingAddress.postalCode,
                                city: ACC.klarna.billingAddress.city,
                                region: ACC.klarna.billingAddress.region,
                                phone: ACC.klarna.billingAddress.phoneNumber,
                                country: ACC.klarna.billingAddress.countryCode
                            },
                            shipping_address: {
                                given_name: ACC.klarna.deliveryAddress.givenName,
                                family_name: ACC.klarna.deliveryAddress.familyName,
                                email: ACC.klarna.deliveryAddress.emailAddress,
                                title: ACC.klarna.deliveryAddress.title,
                                street_address: ACC.klarna.deliveryAddress.addressLine1,
                                street_address2: ACC.klarna.deliveryAddress.addressLine2,
                                postal_code: ACC.klarna.deliveryAddress.postalCode,
                                city: ACC.klarna.deliveryAddress.city,
                                region: ACC.klarna.deliveryAddress.region,
                                phone: ACC.klarna.deliveryAddress.phoneNumber,
                                country: ACC.klarna.deliveryAddress.countryCode
                            }
                        },
                        // callback
                        function (response, resolve, reject) {
                            if (response.show_form) {
                                if (response.error === '' || response.error === undefined) {
                                    resolve();
                                } else {
                                    //solvable error
                                    ACC.klarna.showKlarnaError();
                                    reject(response.error);
                                }
                            } else {
                                ACC.klarna.showKlarnaError();
                                reject();
                            }
                        }
                    );
                } catch (e) {
                    ACC.klarna.showKlarnaError();
                }
            };
        }
    },

    showKlarnaError: function () {
        $('#klarnaContainer').hide();
        $('#klarnaError').show();
        let backButton = $('<a id="backToPaymentMethods" class="btn btn-primary btn-block" href="' + ACC.config.encodedContextPath + '/checkout/multi/checkout-com/choose-payment-method' + '">' + ACC.klarna.backButton + '</a>');
        if ($('#backToPaymentMethods').length === 0) {
            $('.checkout-next').before(backButton);
        }
        $('#submitKlarnaForm').attr('disabled', 'disabled');
        $('#submitKlarnaForm').removeClass('checkout-next');
    },

    bindKlarnaFormSubmit: function () {
        $('#submitKlarnaForm').click(
            function (event) {
                event.preventDefault();
                try {
                    Klarna.Payments.authorize(
                        // options
                        {
                            instance_id: ACC.klarna.klarnaInstanceId
                        },
                        // callback
                        function (response, resolve, reject) {
                            if (response.approved === true && response.show_form === true) {
                                console.log(response.authorization_token);
                                $('#authorizationToken').val(response.authorization_token);
                                $('#checkoutComPaymentTokenForm').attr('action', ACC.config.encodedContextPath + '/checkout/multi/checkout-com/payment/submit-payment-data');
                                $('#checkoutComPaymentTokenForm').submit();
                                resolve();
                            } else if (response.approved === false && response.show_form === true) {
                                //solvable error
                                let backButton = $('<a id="backToPaymentMethods" class="btn btn-primary btn-block" href="' + ACC.config.encodedContextPath + '/checkout/multi/checkout-com/choose-payment-method' + '">' + ACC.klarna.backButton + '</a>');
                                if ($('#backToPaymentMethods').length === 0) {
                                    $('.checkout-next').before(backButton);
                                    $('#submitKlarnaForm').removeClass('checkout-next');
                                }
                            } else {
                                ACC.klarna.showKlarnaError();
                            }
                        }
                    );
                } catch (e) {
                    //solvable error
                    let backButton = $('<a id="backToPaymentMethods" class="btn btn-primary btn-block" href="' + ACC.config.encodedContextPath + '/checkout/multi/checkout-com/choose-payment-method' + '">' + ACC.klarna.backButton + '</a>');
                    if ($('#backToPaymentMethods').length === 0) {
                        $('.checkout-next').before(backButton);
                    }
                    $('#submitKlarnaForm').attr('disabled', 'disabled');
                    $('#submitKlarnaForm').removeClass('checkout-next');
                }
            });
    },
};
