ACC.googlePay = {
  _autoload: [
    ["init", $('#paymentMethod_googlePayComponent').length > 0],
    ["onGooglePayExpressLoaded", $('.gpay-express-button').length > 0]
  ],

  paymentClient: null,

  baseRequest: {
    apiVersion: 2,
    apiVersionMinor: 0
  },

  init: function () {
    $('label[for=paymentMethod_googlePayComponent]').hide();
    ACC.googlePay.onGooglePayLoaded();
  },


  onGooglePayLoaded: function () {
    const paymentsClient = ACC.googlePay.getGooglePaymentsClient();
    const isReadyToPayRequest = ACC.googlePay.getGoogleIsReadyToPayRequest();

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
      ACC.googlePay.googlePayNotSupported();
    }.bind(googlePaySettings));
  },

  onGooglePayExpressLoaded: function () {
    const paymentsClient = ACC.googlePay.getGoogleExpressPaymentsClient();
    const isReadyToPayRequest = ACC.googlePay.getGoogleIsReadyToPayRequest();
    new Promise(
      function (resolve, reject) {
        paymentsClient.isReadyToPay(isReadyToPayRequest)
        .then(function (response) {
          if (response.result) {
            ACC.googlePay.showOrCreateExpressButton();
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
      ACC.googlePay.googlePayNotSupported();
    }.bind(googlePaySettings));
  },

  enablePaymentFlow: function () {
    /* hide the delivery address */
    $('[for="coUseDeliveryAddress"]').closest('.checkbox').hide();
    $('.consents-achPayment').addClass('hidden');
    $('#coBillingCountrySelector').hide();
    $('.achPayment').hide();
    $('#coBillingAddress').hide();

    /* disable the checkout next button */
    $('.checkout-next').hide();

    ACC.googlePay.showOrCreateButton();
  },

  showOrCreateButton: function () {
    let button = $('.gpay-button');
    if (button.length === 0) {
      button = $(ACC.googlePay.getGooglePaymentsClient().createButton({
        onClick: ACC.googlePay.createSession.bind(googlePaySettings)
      }));
      $('.checkout-next').after(button);
    }
    if ($('.applePayButton').length > 0) {
      $('.applePayButton').hide();
    }
    button.show();
  },

  showOrCreateExpressButton: function () {
    let button = $(ACC.googlePay.getGoogleExpressPaymentsClient().createButton({
      onClick: ACC.googlePay.createExpressSession.bind(googlePaySettings)
    }));
    if ($(".text-out-of-stock").length === 0) {
      $('.gpay-express-button').html(button);
    }
  },
  getGooglePaymentsClient: function () {
    return googlePaySettings.paymentsClient || (googlePaySettings.paymentsClient = new google.payments.api.PaymentsClient(googlePaySettings.clientSettings));
  },

  getGoogleExpressPaymentsClient: function () {
    const onPaymentDataChanged = ACC.googlePay.onPaymentDataChanged.bind(googlePaySettings);
    const onPaymentAuthorized = ACC.googlePay.onPaymentAuthorized.bind(googlePaySettings);

    googlePaySettings.clientSettings.paymentDataCallbacks = {
      onPaymentDataChanged,
    }
    return googlePaySettings.paymentsClient || (googlePaySettings.paymentsClient = new google.payments.api.PaymentsClient(googlePaySettings.clientSettings));
  },

  createSession: function () {
    const paymentsClient = ACC.googlePay.getGooglePaymentsClient();
    const paymentDataRequest = ACC.googlePay.getGooglePaymentDataRequest();
    paymentsClient.loadPaymentData(paymentDataRequest)
    .then(function (paymentData) {
      ACC.googlePay.processPayment(paymentData);
    }.bind(googlePaySettings))
    .catch(function (err) {
      ACC.googlePay.showPaymentError(err.statusMessage)
    }.bind(googlePaySettings));
  },

  createExpressSession: function () {
    const paymentsClient = ACC.googlePay.getGoogleExpressPaymentsClient();
    const paymentDataRequest = ACC.googlePay.getGoogleExpressPaymentDataRequest();
    if ($(".page-productDetails").length) {
      const productCode = $("input[name=productCodePost]").val(), quantity = $(".js-qty-selector-input").val();
      $.ajax({
        type: 'GET',
        url: ACC.config.encodedContextPath + '/checkout/payment/checkout-com/googlepay/clearCartAndAddToCart?quantity=' + quantity + "&productCode=" + productCode,
        contentType: 'application/json',
        success: function () {
          ACC.minicart.updateMiniCartDisplay()
          paymentsClient.loadPaymentData(paymentDataRequest)
          .then(function (paymentData) {
            ACC.googlePay.processPayment(paymentData);
          }.bind(googlePaySettings))
          .catch(function (err) {
            ACC.googlePay.showPaymentError(err.statusMessage)
          }.bind(googlePaySettings));
        },
        error: function (err) {
          ACC.googlePay.showPaymentError(err.statusMessage);
        }
      });
    } else {
      paymentsClient.loadPaymentData(paymentDataRequest)
      .then(function (paymentData) {
        ACC.googlePay.processPayment(paymentData);
      }.bind(googlePaySettings))
      .catch(function (err) {
        ACC.googlePay.showPaymentError(err.statusMessage)
      }.bind(googlePaySettings));
    }
  },

  getCardPaymentMethod: function () {
    return Object.assign({
        tokenizationSpecification: {
          type: 'PAYMENT_GATEWAY',
          parameters: {
            gateway: googlePaySettings.gateway,
            gatewayMerchantId: googlePaySettings.gatewayMerchantId
          }
        }
      },

      googlePaySettings.baseCardPaymentMethod
    );

  },

  getGoogleIsReadyToPayRequest: function () {
    return Object.assign(
      this.baseRequest,
      {
        allowedPaymentMethods: [googlePaySettings.baseCardPaymentMethod]
      }
    );
  },
  getGooglePaymentDataRequest: function () {
    return Object.assign(this.baseRequest, {
      allowedPaymentMethods: [ACC.googlePay.getCardPaymentMethod()],
      merchantInfo: {
        merchantName: googlePaySettings.merchantName,
        merchantId: googlePaySettings.merchantId
      },
      transactionInfo: googlePaySettings.transactionInfo,
      shippingAddressRequired: true,
      emailRequired: true,
    });
  },

  getGoogleExpressPaymentDataRequest: function () {
    return Object.assign(this.baseRequest, {
      allowedPaymentMethods: [ACC.googlePay.getCardPaymentMethod()],
      merchantInfo: {
        merchantName: googlePaySettings.merchantName,
        merchantId: googlePaySettings.merchantId
      },
      transactionInfo: googlePaySettings.transactionInfo,
      callbackIntents: ["SHIPPING_ADDRESS", "SHIPPING_OPTION"],
      shippingAddressRequired: true,
      shippingOptionRequired: true,
      emailRequired: true,
    });

  },

  processPayment: function (paymentData) {
    const paymentMethodData = paymentData.paymentMethodData;
    const billingAddress = paymentMethodData.info.billingAddress;
    const shippingAddress = paymentData.shippingAddress;
    const email = paymentData.email;
    const token = JSON.parse(paymentMethodData.tokenizationData.token);
    console.log(paymentData);
    ACC.googlePay.performAuthorizePaymentRequest({
      token,
      billingAddress,
      shippingAddress,
      email
    })
    .then(function (order) {
      ACC.checkoutCom.redirectToConfirmationPage(order);
    })
    .catch(function (errorMessage) {
      ACC.checkoutCom.pageSpinner.end()
      ACC.googlePay.showPaymentError(errorMessage.statusMessage);
    }.bind(googlePaySettings));
  },

  performAuthorizePaymentRequest: function (data) {
    return new Promise(function (resolve, reject) {
      $.ajax({
        type: 'POST',
        url: ACC.config.encodedContextPath + '/checkout/payment/checkout-com/googlepay/placeGooglePayOrder',
        data: JSON.stringify(data),
        success: function (response) {
          if (response.redirectUrl) {
            ACC.checkoutCom.redirectToGooglePay3DSUrl(response);
            resolve(response.orderData);
          }
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
    $('#paymentMethod_googlePayComponent').hide()
    .next().hide();
  },

  showPaymentError: function (errorMessage) {
    if (errorMessage !== undefined) {
      const container = $('.main__inner-wrapper');
      const globalAlerts = container.find('> .global-alerts').length >= 1
        ? container.find('> .global-alerts')
        : container.prepend($('<div class="global-alerts"></div>'))
        .find('> .global-alerts');

      globalAlerts.find('.alert.alert-danger').remove();

      var paymentFailureError = errorMessage;
      if (errorMessage === '') {
        paymentFailureError = ACC.googlePay.paymentCancelled;
      }
      setTimeout(() => {
        $('<div class="alert alert-danger">' + paymentFailureError + '</div>')
        .prependTo(globalAlerts);
      }, 1000);

    }
  },
  onPaymentAuthorized: function (paymentData) {
    return new Promise(function (resolve) {
      // handle the response
      ACC.googlePay.processPayment(paymentData)
      .then(function () {
        resolve({ transactionState: 'SUCCESS' });
      })
      .catch(function () {
        resolve({
          transactionState: 'ERROR',
          error: {
            intent: 'PAYMENT_AUTHORIZATION',
            message: 'Insufficient funds',
            reason: 'PAYMENT_DATA_INVALID'
          }
        });
      });
    });
  },
  onPaymentDataChanged: function (paymentData) {
    return new Promise(function (resolve, reject) {
      $.ajax({
        type: 'POST',
        url: ACC.config.encodedContextPath + '/checkout/payment/checkout-com/googlepay/deliveryInfo',
        data: JSON.stringify(paymentData),
        dataType: 'json',
        contentType: 'application/json',
        success: resolve,
        error: reject
      });
    });
  },
};
