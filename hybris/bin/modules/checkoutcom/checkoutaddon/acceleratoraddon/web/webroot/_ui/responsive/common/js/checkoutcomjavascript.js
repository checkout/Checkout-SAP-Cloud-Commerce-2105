window.onload = function () {
    if (typeof Frames === "undefined") return;

    var payButton = document.getElementById('submitCardForm');
    var ACC = window.parent.ACC;
    var $ = window.parent.$;
    var prefix = ACC.config.commonResourcePath.replace("/_ui/", "/_ui/addons/checkoutaddon/");

    Frames.init({
        publicKey: publicKeyValue,
        modes: [ Frames.modes.FEATURE_FLAG_SCHEME_CHOICE],
        cardholder: {
            name: document.getElementById("cardHolderFirstName").value + ' ' + document.getElementById("cardHolderLastName").value,
            billingAddress: {}
        },
        schemeChoice: isABC === 'false'
    });

    var logos = generateLogos();

    function generateLogos() {
        var logos = {};
        logos["card-number"] = {
            src: "card",
            alt: "card number logo"
        };
        logos["expiry-date"] = {
            src: "exp-date",
            alt: "expiry date logo"
        };
        logos["cvv"] = {
            src: "cvv",
            alt: "cvv logo"
        };
        return logos;
    }

    var errors = {};
    errors["card-number"] = "Please enter a valid card number";
    errors["expiry-date"] = "Please enter a valid expiry date";
    errors["cvv"] = "Please enter a valid cvv code";

    Frames.addEventHandler(
      Frames.Events.FRAME_VALIDATION_CHANGED,
      onValidationChanged
    );

    function onValidationChanged(event) {
        var e = event.element;

        if (event.isValid || event.isEmpty) {
            if (e === "card-number" && !event.isEmpty) {
                showPaymentMethodIcon();
            }
            setDefaultIcon(e);
            clearErrorIcon(e);
            clearErrorMessage(e);
        } else {
            if (e === "card-number") {
                clearPaymentMethodIcon();
            }
            setDefaultErrorIcon(e);
            setErrorIcon(e);
            setErrorMessage(e);
        }
    }

    function clearErrorMessage(el) {
        var selector = ".error-message__" + el;
        var message = document.querySelector(selector);
        message.textContent = "";
    }

    function clearErrorIcon(el) {
        var logo = document.getElementById("icon-" + el + "-error");
        logo.style.removeProperty("display");
    }

    function showPaymentMethodIcon(parent, pm) {
        if (parent) parent.classList.add("show");

        var logo = document.getElementById("logo-payment-method");
        if (pm) {
            var name = pm.toLowerCase();
            logo.setAttribute("src", prefix + "/images/card-icons/" + name + ".svg");
            logo.setAttribute("alt", pm || "payment method");
        }
        logo.style.removeProperty("display");
    }

    function clearPaymentMethodIcon(parent) {
        if (parent) parent.classList.remove("show");

        var logo = document.getElementById("logo-payment-method");
        logo.style.setProperty("display", "none");
    }

    function setErrorMessage(el) {
        var selector = ".error-message__" + el;
        var message = document.querySelector(selector);
        message.textContent = errors[el];
    }

    function setDefaultIcon(el) {
        var selector = "icon-" + el;
        var logo = document.getElementById(selector);
        logo.setAttribute("src", prefix + "/images/card-icons/" + logos[el].src + ".svg");
        logo.setAttribute("alt", logos[el].alt);
    }

    function setDefaultErrorIcon(el) {
        var selector = "icon-" + el;
        var logo = document.getElementById(selector);
        logo.setAttribute("src", prefix + "/images/card-icons/" + logos[el].src + "-error.svg");
        logo.setAttribute("alt", logos[el].alt);
    }

    function setErrorIcon(el) {
        var logo = document.getElementById("icon-" + el + "-error");
        logo.style.setProperty("display", "block");
    }

    Frames.addEventHandler(
      Frames.Events.CARD_VALIDATION_CHANGED,
      cardValidationChanged
    );

    function cardValidationChanged(event) {
        payButton.disabled = !Frames.isCardValid();
    }

    Frames.addEventHandler(
      Frames.Events.CARD_TOKENIZATION_FAILED,
      onCardTokenizationFailed
    );

    function onCardTokenizationFailed(error) {
        console.log("CARD_TOKENIZATION_FAILED: %o", error);
        Frames.enableSubmitForm();
    }

    Frames.addEventHandler(
      Frames.Events.CARD_BIN_CHANGED,
      onCardBinChanged
    )

    function onCardBinChanged(event) {
        const cobadgeTooltip = $('.cobadgeTooltip');
        if(event.isCoBadged) {
            cobadgeTooltip.removeClass("hidden");
        } else {
            $('.cobadgeTooltip').addClass("hidden");
        }
    }

    Frames.addEventHandler(Frames.Events.CARD_TOKENIZED, onCardTokenized);


    function onCardTokenized(event) {
        document.getElementById("paymentToken").value = event.token;
        document.getElementById("number").value = event.bin + event.last4;
        document.getElementById("cardBin").value = event.bin;
        document.getElementById("cardType").value = event.scheme;
        document.getElementById("schemeLocal").value = event.preferred_scheme;
        document.getElementById("validToMonth").value = event.expiry_month;
        document.getElementById("validToYear").value = event.expiry_year;
        document.getElementById("accountHolderName").value = $('#cardholdername').val();
        var markToSave = document.getElementById("markToSave");
        if (markToSave !== null) {
            document.getElementById("saveCard").value = markToSave.value === 'on' ? true : false;
        }

        $('#checkoutComPaymentTokenForm')
          .attr('action', ACC.config.encodedContextPath + '/checkout/multi/checkout-com/payment/submit-payment-data')
          .submit();
    }

    Frames.addEventHandler(
      Frames.Events.PAYMENT_METHOD_CHANGED,
      paymentMethodChanged
    );

    function paymentMethodChanged(event) {
        var pm = event.paymentMethod;
        var container = document.querySelector(".icon-container.payment-method");

        if (!pm) {
            clearPaymentMethodIcon(container);
        } else {
            clearErrorIcon("card-number");
            showPaymentMethodIcon(container, pm);
        }
    };

    payButton.addEventListener("click", function (event) {
        event.preventDefault();
        Frames.submitCard();
    }, false);

    $('#cardholdername').on('change', function () {
        Frames.cardholder = {
            name: $(this).val()
        }
    });
};
