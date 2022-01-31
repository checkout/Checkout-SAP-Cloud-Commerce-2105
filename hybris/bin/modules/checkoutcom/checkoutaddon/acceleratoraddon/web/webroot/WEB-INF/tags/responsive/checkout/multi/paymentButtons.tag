<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ attribute name="requestCountryCode" required="true" type="java.lang.String" %>
<%@ attribute name="requestCurrencyCode" required="true" type="java.lang.String" %>
<%@ attribute name="paymentDetailsForm" required="false" type="com.checkout.hybris.addon.forms.PaymentDetailsForm" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="hasPaymentButtons" value="false"/>
<c:set var="hasAvailablePaymentMethods" value="false" scope="session"/>

<c:set var="countryCode" value="${requestCountryCode}" scope="request"/>
<c:set var="currencyCode" value="${requestCurrencyCode}" scope="request"/>

<div id="payment-not-available" class="payment-method-error" style="display: none;">
    <span><spring:theme code="checkoutcom.error.selected.payment.not.available"/></span>
</div>

<form:hidden id="paymentRedirect" path="redirect"/>
<form:hidden id="paymentDataRequired" path="dataRequired"/>

<div id="paymentButtons" data-paymentmethod="${paymentDetailsForm.paymentMethod}">
    <cms:pageSlot position="PaymentButtons" var="button" element="div" class="cms-payment-button">
        <c:set var="hasPaymentButtons" value="true"/>
        <cms:component component="${button}" evaluateRestriction="true"/>
    </cms:pageSlot>
</div>
