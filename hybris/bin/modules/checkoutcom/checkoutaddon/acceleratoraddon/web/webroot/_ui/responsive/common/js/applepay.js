ACC.applePay = {
  _autoload: [
    'init',
    ['showOrCreateExpressButton', $(".appleExpressPayButton").length]
  ],
  init: function () {
    var checkbox = $('#paymentMethod_applePayComponent');

    if (checkbox.length === 0 || !window.ApplePaySession || !ApplePaySession.canMakePayments()) {
      this.applePayNotSupported();
      return;
    }
    $('#paymentMethod_applePayComponent').next();
  },

  enablePaymentFlow: function () {
    /* hide the delivery address */
    $('[for="coUseDeliveryAddress"]').closest('.checkbox').hide();
    $('.consents-achPayment').addClass('hidden');
    $('#coBillingCountrySelector').hide();
    $('.achPayment').hide();
    $('#coBillingAddress').hide();
    $('.gpay-card-info-container').remove()

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
      button.on('click', this.createSession.bind(applePaySettings));
      $('.checkout-next').after(button);
    }

    if ($('.gpay-button').length > 0) {
      $('.gpay-button').hide();
    }
    button.show();
  },
  showOrCreateExpressButton: function () {
    let button = $('.applePayButton');
    if (button.length === 0 && window.ApplePaySession) {
      button = $('<a lang="en" class="applePayButton" style="-webkit-appearance: -apple-pay-button; -apple-pay-button-type:plain; -apple-pay-button-style: black; height: 48px;" title="Start Apple Pay" role="link" tabindex="0"></a>');
      button.on('click', this.createSession.bind(applePaySettings));
      if ($(".text-out-of-stock").length === 0) {
        $('.appleExpressPayButton').html(button);
      }
    }
  },
  onApplePayButtonClicked: function () {
    const productCode = $("input[name=productCodePost]").val(), quantity = $(".js-qty-selector-input").val();
    $.ajax({
      type: 'GET',
      url: ACC.config.encodedContextPath + '/checkout/payment/checkout-com/googlepay/clearCartAndAddToCart?quantity=' + quantity + "&productCode=" + productCode,
      contentType: 'application/json',
      success: function () {
        ACC.minicart.updateMiniCartDisplay();

      },
      error: function (err) {
        ACC.applePay.showPaymentError(err.statusMessage);
      }
    });
  },

  createSession: function () {
    const request = this.paymentRequest;
    if ($(".page-productDetails").length) {
      ACC.applePay.onApplePayButtonClicked();
    }
    try {
      this.session = new ApplePaySession(5, request);
    } catch (err) {
      ACC.applePay.applePayNotSupported();
      return;
    }

    this.session.oncancel = function () {
      console.log('cancelled payment');
      ACC.checkoutCom.pageSpinner.end();
      ACC.applePay.showPaymentError();
    }.bind(applePaySettings);

    this.session.onvalidatemerchant = function (event) {
      ACC.applePay.performAuthorizeMerchantRequest(event.validationURL)
      .then(function (merchantSession) {
        this.session.completeMerchantValidation(merchantSession);
      }.bind(applePaySettings))
      .catch(ACC.applePay.showPaymentError.bind(applePaySettings));
    }.bind(applePaySettings);

    this.session.onshippingmethodselected = function (event) {
      var shippingMethod = event.shippingMethod;
      ACC.applePay.performSetDeliveryMethod(shippingMethod)
      .then(function (update) {
        this.session.completeShippingMethodSelection(update);
      }.bind(applePaySettings))
      .catch(ACC.applePay.showPaymentError.bind(applePaySettings));
    }.bind(applePaySettings);

    this.session.onshippingcontactselected = function (event) {
      var shippingContact = event.shippingContact;
      ACC.applePay.performSetShippingContact(shippingContact)
      .then(function (update) {
        this.session.completeShippingContactSelection(update);
      }.bind(applePaySettings))
      .catch(ACC.applePay.showPaymentError.bind(applePaySettings));
    }.bind(applePaySettings);

    this.session.onpaymentauthorized = function (event) {
      ACC.applePay.performAuthorizePaymentRequest(event.payment)
      .then(function (response) {
        var statusCode = response.status === 'SUCCESS' ?
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
      }.bind(applePaySettings))
      .catch(ACC.applePay.showPaymentError.bind(applePaySettings));
    }.bind(applePaySettings);

    ACC.checkoutCom.pageSpinner.start();

    this.session.begin();
  },

  performAuthorizeMerchantRequest: function (validationURL) {
    return new Promise(function (resolve, reject) {
      $.ajax({
        type: 'POST',
        url: ACC.config.encodedContextPath + '/checkout/payment/checkout-com/applepay/request-session',
        data: JSON.stringify({ validationURL: validationURL }),
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
        shippingContact: shippingContact,
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
    var container = $('.main__inner-wrapper');
    var globalAlerts = container.find('> .global-alerts').length >= 1
      ? container.find('> .global-alerts')
      : container.prepend($('<div class="global-alerts"></div>'))
      .find('> .global-alerts');

    globalAlerts.find('.alert.alert-danger').remove();

    var paymentFailureError = errorMessage;
    if (errorMessage === undefined || errorMessage === '') {
      paymentFailureError = applePaySettings.paymentCancelled;
    }

    $('<div class="alert alert-danger">' + paymentFailureError + '</div>')
    .prependTo(globalAlerts);
  },

  performSetDeliveryMethod: function (shippingMethod) {
    console.log(shippingMethod);
    return new Promise(function (resolve, reject) {
      $.ajax({
        type: 'POST',
        url: ACC.config.encodedContextPath + '/checkout/payment/checkout-com/applepay/deliveryMethod',
        data: JSON.stringify(shippingMethod),
        dataType: 'json',
        contentType: 'application/json',
        success: resolve,
        error: reject,
        always: function () {
          ACC.checkoutCom.pageSpinner.end();
        }
      });
    });
  },

  performSetShippingContact: function (shippingContact) {
    return new Promise(function (resolve, reject) {
      $.ajax({
        type: 'POST',
        url: ACC.config.encodedContextPath + '/checkout/payment/checkout-com/applepay/deliveryAddress',
        data: JSON.stringify(shippingContact),
        dataType: 'json',
        contentType: 'application/json',
        success: resolve,
        error: reject,
        always: function () {
          ACC.checkoutCom.pageSpinner.end();
        }
      });
    });
  },
};
