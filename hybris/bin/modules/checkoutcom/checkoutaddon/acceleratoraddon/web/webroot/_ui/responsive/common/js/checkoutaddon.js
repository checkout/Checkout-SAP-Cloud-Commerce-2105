ACC.checkoutCom = {
    _autoload: [
        "bindUseDeliveryAddress",
        "bindCountrySelector",
        "bindCreditCardAddressForm",
        "bindSubmitBillingAddressForm",
        "bindPaymentMethodAutoCheck",
        "bindOnBillingCountryChange",
        "onPaymentMethodSelect",
        "bindPaymentFormSubmit",
        "showACHCompanyField",
        "showACHTerms",
        "showSepaMandate"
    ],

    bindPaymentFormSubmit: function () {
        $('#submitPaymentForm').click(
            function (event) {
                event.preventDefault();
                $('#checkoutComPaymentDataForm').attr('action', ACC.config.encodedContextPath + '/checkout/multi/checkout-com/payment/submit-payment-data');
                $("#checkoutComPaymentDataForm").submit();
            });
    },

    bindCountrySelector: function () {
        $('select[id^="billingAddress\\.country"]').on("change", function () {
            $('#coUseDeliveryAddress').prop('checked', false);
        });
    },

    bindUseDeliveryAddress: function () {
        const coUseDeliveryAddress = $("#coUseDeliveryAddress");

        coUseDeliveryAddress.on("change", function () {
            if (coUseDeliveryAddress.is(":checked")) {
                const options = {
                    'countryIsoCode': $('#coUseDeliveryAddress').data("countryisocode"),
                    'useDeliveryAddress': true
                };
                ACC.checkoutCom.enableAddressForm();
                ACC.checkoutCom.displayCreditCardAddressForm(options, ACC.checkoutCom.useDeliveryAddressSelected);
                ACC.checkoutCom.disableAddressForm();
            } else {
                ACC.checkoutCom.clearAddressForm();
                ACC.checkoutCom.enableAddressForm();
            }
        });
        if (coUseDeliveryAddress.is(":checked")) {
            ACC.checkoutCom.disableAddressForm();
        }
    },

    disableAddressForm: function () {
        $('input[id^="billingAddress\\."]').prop('disabled', true);
        $('select[id^="billingAddress\\."]').prop('disabled', true);
    },

    enableAddressForm: function () {
        $('input[id^="billingAddress\\."]').prop('disabled', false);
        $('select[id^="billingAddress\\."]').prop('disabled', false);
    },

    clearAddressForm: function () {
        $('input[id^="billingAddress\\."]').val("");
        $('select[id^="billingAddress\\."]').val("");
    },

    useDeliveryAddressSelected: function () {
        const coUseDeliveryAddress = $("#coUseDeliveryAddress");
        if (coUseDeliveryAddress.is(":checked")) {
            $('select[id^="billingAddress\\.country"]').val(coUseDeliveryAddress.data('countryisocode'));
            ACC.checkoutCom.disableAddressForm();
            ACC.checkoutCom.updatePaymentMethods();
        } else {
            ACC.checkoutCom.clearAddressForm();
            ACC.checkoutCom.enableAddressForm();
            ACC.checkoutCom.updatePaymentMethods();
        }
    },

    bindCreditCardAddressForm: function () {
        $("#coBillingCountrySelector").find(":input").on("change", function () {
            const countrySelection = $(this).val();
            const options = {
                'countryIsoCode': countrySelection,
                'useDeliveryAddress': false
            };
            ACC.checkoutCom.displayCreditCardAddressForm(options);
        });
    },

    displayCreditCardAddressForm: function (options, callback) {
        $.ajax({
            url: ACC.config.encodedContextPath + '/checkout/multi/checkout-com/address/billingaddressform',
            data: options,
            dataType: "html"
        }).done(function (data) {
            $("#coBillingAddress").html($(data).html());
            if (typeof callback === 'function') {
                callback();
            }
        });
    },

    bindSubmitBillingAddressForm: function () {
        $("#nextToPaymentForm").click(
            function (event) {
                event.preventDefault();
                ACC.common.blockFormAndShowProcessingMessage($(this));
                $("#coBillingAddress").filter(":hidden").remove();
                ACC.checkoutCom.enableAddressForm();
                $("#checkoutComPaymentDetailsForm").submit();
            }
        );
    },

    bindPaymentMethodAutoCheck: function () {
        //if there is only one payment method, autoselect that payment method, otherwise unselect all
        var $paymentMethods = $('input[name="paymentMethod"]');
        $paymentMethods.prop('checked', $paymentMethods.length === 1);
    },

    bindOnBillingCountryChange: function () {
        // when change the billing address country, need to make an ajax call to validate all payment methods shown
        $('div#coBillingCountrySelector select').bind('change', function () {
            ACC.checkoutCom.updatePaymentMethods();
        });
    },

    updatePaymentMethods: function () {
        var selectedCountryCode = $('div#coBillingCountrySelector select').val();
        var selectedPaymentMethod = null;
        // the selected payment method has to be send to the BE, otherwise a null value
        if ($('input[name="paymentMethod"]:checked').val()) {
            selectedPaymentMethod = $('input[name="paymentMethod"]:checked').val();
        }

        var deliveryAddressCountryCode = $('#current-selected-country').val();
        if (deliveryAddressCountryCode == selectedCountryCode) {
            return;
        }

        $.ajax({
            url: ACC.config.encodedContextPath + '/checkout/multi/checkout-com/reload-payment-buttons',
            type: 'GET',
            data: {
                countryIsoCode: selectedCountryCode,
                selectedPaymentMethod: selectedPaymentMethod
            },
            success: function (response) {
                var paymentMethodsHtml = $(response).find('#payment-method-container').html();
                // replace the content with the refreshed html content

                $('div#payment-method-container').html(paymentMethodsHtml);
                $('#current-selected-country').val(selectedCountryCode);

                // if now the payment method available is just one, mark that as checked by default otherwise no.
                ACC.checkoutCom.bindPaymentMethodAutoCheck();

                // if the payment method selected before the ajax is not available anymore, show the error message,
                // otherwise mark the same payment method as checked.
                if ($('#previous-selected-payment').val()) {
                    var previousSelectedPayment = $('#previous-selected-payment').val();
                    var $paymentMethod = $('#paymentMethod_' + previousSelectedPayment);
                    if ($paymentMethod.is(':disabled')) {
                        $('#payment-not-available').show();
                    } else {
                        $paymentMethod.prop('checked', true);
                    }
                }

                // add binding again because it was losing it.
                ACC.checkoutCom.onPaymentMethodSelect();
            },
            error: function (request, status, error) {
                console.error(request.responseText);
            }
        });
    },

    onPaymentMethodSelect: function () {
        // Once you select a new payment method, if the error is still shown, hide the error
        $('input[type=radio][name="paymentMethod"]').on('change', function () {

            $('#paymentDataRequired').val($(this).attr("data-required"));
            $('#paymentRedirect').val($(this).attr("data-redirect"));

            if ($('#payment-not-available').is(':visible')) {
                $('#payment-not-available').hide();
            }

            const selectedPaymentMethod = $(this).val();
            if (selectedPaymentMethod === 'applePayComponent') {
                ACC.applePay.enablePaymentFlow();
            } else if (selectedPaymentMethod == 'googlePayComponent') {
                ACC.googlePay.enablePaymentFlow();
            } else {
                ACC.checkoutCom.resetPaymentMethod();
            }
        });
    },

    resetPaymentMethod: function () {
        $('.applePayButton').hide();
        $('.gpay-button').hide();

        $('[for="coUseDeliveryAddress"]').closest('.checkbox').show();
        $('#coBillingCountrySelector').show();
        $('#coBillingAddress').show();

        $('.checkout-next').show();
    },

    showACHCompanyField: function () {
        $('select#accountType').on('change', function () {
            if (this.value === 'Corporate' || this.value === 'CorpSavings') {
                $('#companyNameLabel').show();
                $('#companyName').show();
            } else {
                $('#companyNameLabel').hide();
                $('#companyName').hide();
                $('.company-name-error').hide();
            }
        });
    },

    showACHTerms: function () {
        $('.ach-form-container .ach-filed-required').keyup(function () {
            var $emptyFields = $('.ach-form-container .ach-filed-required').filter(function () {
                return $.trim($(this).val()).length === 0;
            });

            if (!$emptyFields.length) {
                $('div.ach-info').show();
            }
        });

        $('.ach-form-container #accountType').change(function () {
            var $emptyFields = $('.ach-form-container .ach-filed-required').filter(function () {
                return $.trim($(this).val()).length === 0;
            });

            if (!$emptyFields.length) {
                $('div.ach-info').show();
            }
        });
    },

    showSepaMandate: function () {
        $('.sepa-form-container .sepa-required').keyup(function () {
            var $emptyFields = $('.sepa-form-container .sepa-required').filter(function () {
                return $.trim($(this).val()).length === 0;
            });

            if (!$emptyFields.length) {
                $('div.sepa-info').show();
            }
        });
    },

    spinner: $("<img src='" + ACC.config.commonResourcePath + "/images/spinner.gif' />"),

    pageSpinner: {
        createSpinner: function () {
            const prefix = ACC.config.commonResourcePath.replace('/_ui/', '/_ui/addons/checkoutaddon/');
            return $('<img style="width: 32px; height: 32px; margin: auto;" src="' + prefix + '/images/oval-spinner.svg" />')
        },

        start: function () {
            this.overlay = $('<div id="cboxOverlay" style="opacity: 0.7; cursor: pointer; visibility: visible; display: flex;" />');
            this.createSpinner().appendTo(ACC.checkoutCom.pageSpinner.overlay);
            this.overlay.appendTo($('body'));
        },

        end: function () {
            this.overlay.fadeOut(400, function () {
                $(this).remove();
            });
        }
    },

    redirectToConfirmationPage: function (order) {
        const orderId = order.guestCustomer ? order.guid : order.code;

        window.location.href = ACC.config.encodedContextPath + '/checkout/orderConfirmation/' + orderId;
    }
}