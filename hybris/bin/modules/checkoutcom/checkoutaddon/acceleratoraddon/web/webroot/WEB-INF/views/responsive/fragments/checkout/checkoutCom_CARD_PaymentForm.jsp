<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<script>
    /*<![CDATA[*/
    const publicKeyValue = '${publicKey}';
    const isABC = '${isABC}';
    /*]]>*/
    document.addEventListener("DOMContentLoaded", function () {
      document.getElementById('submitPaymentForm').id = 'submitCardForm';
    });
</script>

<!-- add frames script -->
<script src="https://cdn.checkout.com/js/framesv2.min.js"></script>

<div class="headline">
    <spring:theme code="checkout.multi.paymentMethod" />
</div>

<form id="payment-form" method="POST" action="https://merchant.com/charge-card">
    <c:set
            var="imageUrlAddon"
            value="${fn:replace(commonResourcePath, 'responsive/common', 'addons/checkoutaddon/responsive/common/images/card-icons')}"
    />

    <sec:authorize access="!hasAnyRole('ROLE_ANONYMOUS')">
        <div class="save_payment_details checkbox">
            <label for="markToSave">
                <input type="checkbox" id="markToSave" tabindex="19" />
                <spring:theme code="checkout.multi.paymentMethod.addPaymentDetails.savePaymentDetailsInAccount" />
            </label>
        </div>
    </sec:authorize>

    <label for="card-holder-name"><spring:theme code="checkoutcom.multi.cardholdername.label" text="Card holder name" /></label>
    <div class="input-container card-holder">
        <input type="text" value="${fn:trim(fn:escapeXml(billingAddress.firstName).concat(' ').concat(fn:escapeXml(billingAddress.lastName)))}" id="cardholdername" required>
    </div>

    <label for="card-number">
        <spring:theme code="checkoutcom.multi.cardnumber.label" text="Card number" />
    </label>
    <p class="cobadgeTooltip hidden">
        <spring:theme code="checkoutcom.multi.cardnumber.preferredBrand"/>
        <span data-toggle="tooltip" data-placement="left" title="<spring:theme code="checkoutcom.multi.cardnumber.tooltip"/>">
            <i class="glyphicon glyphicon-info-sign"></i>
        </span>
    </p>

    <div class="input-container card-number">
        <div class="icon-container">
            <img id="icon-card-number" src="${imageUrlAddon}/card.svg" alt="PAN" />
        </div>
        <div class="card-number-frame"></div>
        <div class="icon-container payment-method">
            <img id="logo-payment-method" alt="Card Logo" />
        </div>
        <div class="icon-container icon-error">
            <img id="icon-card-number-error" src="${imageUrlAddon}/error.svg" alt="Card number error" />
        </div>
        <span class="error-message error-message__card-number"></span>
    </div>

    <div class="date-and-code">
        <div>
            <label for="expiry-date">
                <spring:theme code="checkoutcom.multi.expirydate.label" text="Expiry date" />
            </label>
            <div class="input-container expiry-date">
                <div class="icon-container">
                    <img id="icon-expiry-date" src="${imageUrlAddon}/exp-date.svg" alt="Expiry date" />
                </div>
                <div class="expiry-date-frame"></div>
                <div class="icon-container icon-error">
                    <img id="icon-expiry-date-error" src="${imageUrlAddon}/error.svg" alt="Card Expiry date error" />
                </div>
                <span class="error-message error-message__expiry-date"></span>
            </div>
        </div>

        <div>
            <label for="cvv"><spring:theme code="checkoutcom.multi.securitycode.label" text="Security code" /></label>
            <div class="input-container cvv">
                <div class="icon-container">
                    <img id="icon-cvv" src="${imageUrlAddon}/cvv.svg" alt="CVV" />
                </div>
                <div class="cvv-frame"></div>
                <div class="icon-container icon-error">
                    <img id="icon-cvv-error" src="${imageUrlAddon}/error.svg" alt="Card Security code error" />
                </div>
                <span class="error-message error-message__cvv"></span>
            </div>
        </div>
    </div>

    <div class="form-billing-address">
        <div class="headline">
            <spring:theme code="checkout.multi.paymentMethod.savedCards.billingAddress" />
        </div>
        <c:if test="${not empty billingAddress}">
            <c:if test="${not empty billingAddress.titleCode}">${fn:escapeXml(billingAddress.titleCode)}&nbsp;</c:if><span
                id="billingFirstName"
        >${fn:escapeXml(billingAddress.firstName)}</span>&nbsp;<span id="billingLastName">${fn:escapeXml(billingAddress.lastName)}</span>
                                                         </br><span id="billingLine1">${fn:escapeXml(billingAddress.line1)}</span>,&nbsp;<c:if
                test="${not empty billingAddress.line2}"
        ><br /><span
                id="billingLine2"
        >${fn:escapeXml(billingAddress.line2)}</span>,&nbsp;</c:if>
            <span id="billingCity">${fn:escapeXml(billingAddress.town)}</span>,&nbsp;<c:if
                test="${not empty billingAddress.region.name}"
        ><span
                id="billingState"
        >${fn:escapeXml(billingAddress.region.name)}</span>&nbsp;</c:if><span
                id="billingPostCode"
        >${fn:escapeXml(billingAddress.postalCode)}</span>
                                                         </br>${fn:escapeXml(billingAddress.country.name)}
            <input type="hidden" id="billingCountryCode" value="${fn:escapeXml(billingAddress.country.isocode)}" />
            <c:if test="${not empty billingAddress.phone }"><br /><span
                    id="billingPhone"
            >${fn:escapeXml(billingAddress.phone)}</span></c:if>

            <input type="hidden" id="cardHolderFirstName" value="${fn:escapeXml(billingAddress.firstName)}" />
            <input type="hidden" id="cardHolderLastName" value="${fn:escapeXml(billingAddress.lastName)}" />
        </c:if>
    </div>

</form>


<form:form
        id="checkoutComPaymentTokenForm"
        modelAttribute="paymentDataForm"
        method="POST"
        action="${submitPaymentDataUrl}"
>
    <form:hidden id="paymentToken" path="formAttributes['${'paymentToken'}']" value="" />
    <form:hidden id="number" path="formAttributes['${'number'}']" value="" />
    <form:hidden id="cardBin" path="formAttributes['${'cardBin'}']" value="" />
    <form:hidden id="cardType" path="formAttributes['${'cardType'}']" value="" />
    <form:hidden id="validToMonth" path="formAttributes['${'validToMonth'}']" value="" />
    <form:hidden id="validToYear" path="formAttributes['${'validToYear'}']" value="" />
    <form:hidden id="scheme" path="formAttributes['${'scheme'}']" value="" />
    <form:hidden id="schemeLocal" path="formAttributes['${'schemeLocal'}']" value="" />
    <form:hidden id="saveCard" path="formAttributes['${'saveCard'}']" value="" />
    <form:hidden id="type" path="formAttributes['${'type'}']" value="${paymentMethod}" />
    <form:hidden
            id="accountHolderName" path="formAttributes['${'accountHolderName'}']"
            value="${fn:trim(fn:escapeXml(billingAddress.firstName).concat(' ').concat(fn:escapeXml(billingAddress.lastName)))}"
    />
</form:form>
