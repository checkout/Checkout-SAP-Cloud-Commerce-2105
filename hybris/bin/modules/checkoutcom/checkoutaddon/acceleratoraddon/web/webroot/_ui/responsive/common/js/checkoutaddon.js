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
    "showSepaMandate",
  "replaceTooltip"
    ],

  spinner: null,

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
    $(document).on("change", "#customerConsent", function () {
      if ($("#customerConsent").is(":checked")) {
        $('.achPayment').prop('disabled', false);
      } else {
        $('.achPayment').prop('disabled', true);
      }
    })

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
  checkEmptyForm: function (form) {
    const inputs = $(form).find('input');
    let filledFields = true;
    inputs.each(function () {
      if ($(this).val().length <= 0) {
        filledFields = false;
      }
    });
    return filledFields;
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
    let createACHToken = true;
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
      } else if (selectedPaymentMethod == 'ACH') {
        ACC.checkoutCom.resetPaymentMethod();
        ACC.checkoutCom.showACHPopUpayment(createACHToken);
        createACHToken = false;
      } else {
        ACC.checkoutCom.resetPaymentMethod();
      }
    });
  },

  showACHPopUpayment: function (createToken) {
    const textButton = $('.checkout-next').text();
    $('.consents-achPayment').removeClass("hidden");
    $('.checkout-next').hide();
    if ($('.achPayment').length) {
      $(".achPayment").show();
      $('.achPayment').prop('disabled', !$("#customerConsent").is(":checked"));
    } else {
      $('.form-actions').append('<button class="achPayment btn btn-primary btn-block" data-test-id="ach-plaid-open-btn">' + textButton + '</button>');
    }

    let handler = '';
    (async function ($) {
      if (createToken) {
        handler = Plaid.create({
          // Create a new link_token to initialize Link
          token: (await $.post(ACC.config.encodedContextPath + '/checkout/payment/checkout-com/ach/link/token/create')),
          receivedRedirectUri: '',
          onSuccess: function (public_token, metadata) {
            const customerConsents = $("#customerConsent").prop("checked");
            let html = ""
            let selected = ""
            $.each(metadata.accounts, function (key, value) {
              if (key === 0) {
                selected = "checked"
              }
              html += "<div class='form-group accounts-playidLink'><input class='js-account-plaidLink' " + selected + " type='radio' name='accounts' data-value='" + JSON.stringify(value) + "'/>" + value.name + "<div> **** **** " + value.mask + "</div></div>"
            });
            html += "<div class='buttons place-order-plaid-button'><button class='btn btn-primary js-submit-public-token'>" + $('.achPayment').text() + "</button></div>"
            ACC.colorbox.open($(".consents-achPayment").data("account"), {
              html: html,
              width: "80%",
              onComplete: function () {
                ACC.colorbox.resize();
              }
            });
            $(document).on("click", ".js-submit-public-token", function () {
                const data = $('.js-account-plaidLink:checked').data("value");
                metadata.account_id = data.id;
                metadata.account = data;
                ACC.checkoutCom.submitOrder(public_token, metadata, customerConsents)
              }
            );
          },
          onExit: function (err) {
            if (err != null) {
              ACC.checkoutCom.addGlobalMessages(err, "danger");
            }
          },
        });
      }

    })($);

    $(document).on("click", ".achPayment", function () {
        ACC.checkoutCom.enableAddressForm();
        const errorMessage = $(".consents-achPayment").data("error");
        $.ajax({
          url: ACC.config.encodedContextPath + '/checkout/multi/checkout-com/set-payment-details',
          type: 'POST',
          data: $("#checkoutComPaymentDetailsForm").serialize(),
          success: function () {
            handler.open();
          },
          error: function () {
            ACC.checkoutCom.addGlobalMessages(errorMessage, "danger", ".main__inner-wrapper");
            $("html, body").animate({ scrollTop: 0 }, "slow");
          }
        });
      }
    );
    $(document).on("click", ".js-achpayment-consents", function () {
        ACC.colorbox.open($("#pop-up-consents").data('title'), {
          html: $("#pop-up-consents").html(),
          width: "80%",
          onComplete: function () {
            ACC.colorbox.resize();
          }
        });
      }
    );
  },

  addSpinner: function (element) {
    ACC.checkoutCom.spinner = jQuery('<img>').attr({
      'src': ACC.config.commonResourcePath + '/images/spinner.gif', 'alt': 'loading...',
      'style': 'z-index: 1'
    });
    $(element).prepend(ACC.checkoutCom.spinner.addClass('ldrgif'));
  },

  removeSpinner: function () {
    if (ACC.checkoutCom.spinner) {
      $(ACC.checkoutCom.spinner).remove();
    }
  },

  submitOrder: function (public_token, metadata, customerConsents) {
    const $form = $('#checkoutComPaymentDetailsForm');
    ACC.colorbox.close();
    $form.css('opacity', '0.5');
    $form.find('input, select').prop('disabled', true);
    ACC.checkoutCom.addSpinner('.checkout-paymentmethod');
    $.ajax({
      url: ACC.config.encodedContextPath + '/checkout/payment/checkout-com/ach/item/public_token/exchange',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify({
        publicToken: public_token,
        metadata: metadata,
        customerConsents: customerConsents
      }),
      success: function (response) {
        if (Object.keys(response.errors).length) {
          let message = '';
          if (response.errors && response.errors.accErrorMsgs) {
            response.errors.accErrorMsgs.forEach(function (errorMessage) {
              if (message.length) {
                message += '<br/>';
              }
              message += errorMessage;
            });
            ACC.checkoutCom.addGlobalMessages(
              message,
              'danger',
              '.main__inner-wrapper'
            );
          }
        } else {
          window.location.href = ACC.config.encodedContextPath + response.url;
        }
      },
      complete: function () {
        $form.css('opacity', '1');
        $form.find('input, select').prop('disabled', false);
        $('.achPayment').prop('disabled', false);
        ACC.checkoutCom.removeSpinner('.checkout-paymentmethod');
      }
    });
  },
  addGlobalMessages: function (text, type, element) {
    const $element = $(element);
    $element
      .prepend('<div class="alert alert-' + type + ' alert-dismissable getAccAlert">' +
               '<button class="close closeAccAlert" type="button" data-dismiss="alert" aria-hidden="true">×</button>' + text + '</div>');

    window.scrollTo({
      top: $element.offset().top,
      left: 0,
      behavior: 'smooth'
    });
  },

  resetPaymentMethod: function () {
    $('.applePayButton').hide();
    $('.achPayment').hide();
    $('.gpay-card-info-container').remove();
    $('.consents-achPayment').addClass("hidden");

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
  },

  redirectToGooglePay3DSUrl: function (response) {
    window.location.href = response.redirectUrl;
  },

    replaceTooltip() {
        $.widget.bridge('uitooltip', $.ui.tooltip);
        $('[data-toggle="tooltip"]').uitooltip({
            tooltipClass: "cobranded-tooltip"
        });
    }
};
