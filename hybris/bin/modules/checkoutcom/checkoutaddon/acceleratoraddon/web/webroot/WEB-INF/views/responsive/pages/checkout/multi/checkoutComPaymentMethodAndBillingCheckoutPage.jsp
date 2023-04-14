<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/responsive/template" %>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags" %>
<%@ taglib prefix="multiCheckout" tagdir="/WEB-INF/tags/responsive/checkout/multi" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="chko-multi-checkout" tagdir="/WEB-INF/tags/addons/checkoutaddon/responsive/checkout/multi" %>

<spring:htmlEscape defaultHtmlEscape="true"/>

<template:page pageTitle="${pageTitle}" hideHeaderLinks="true">

    <div class="row">
        <div class="col-sm-6">
            <div class="checkout-headline">
                <span class="glyphicon glyphicon-lock"></span>
                <spring:theme code="checkout.multi.secure.checkout"/>
            </div>
            <multiCheckout:checkoutSteps checkoutSteps="${checkoutSteps}" progressBarId="${progressBarId}">
                <jsp:body>
                    <ycommerce:testId code="checkoutStepThree">
                        <div class="checkout-paymentmethod">
                            <div class="checkout-indent">
                                <ycommerce:testId code="paymentDetailsForm">

                                    <c:if test="${not empty paymentInfos}">
                                        <div class="form-group">
                                            <c:if test="${not empty paymentInfos}">
                                                <button type="button"
                                                        class="btn btn-default btn-block js-saved-payments">
                                                    <spring:theme
                                                            code="checkout.multi.paymentMethod.addPaymentDetails.useSavedCard"/>
                                                </button>
                                            </c:if>
                                        </div>
                                        <chko-multi-checkout:savedPaymentInfos/>
                                    </c:if>

                                    <c:url value="/checkout/multi/checkout-com/add-payment-details"
                                           var="addPaymentAddressUrl"/>

                                    <form:form id="checkoutComPaymentDetailsForm"
                                               modelAttribute="paymentDetailsForm"
                                               method="post"
                                               action="${addPaymentAddressUrl}"
                                               class="create_update_payment_form">

                                        <div id="payment-method-container">
                                            <chko-multi-checkout:checkoutComPaymentButtons/>
                                        </div>
                                        <chko-multi-checkout:billingAddress/>
                                        <div class="form-additionals">
                                        </div>

                                        <div class="checkbox consents-achPayment hidden" data-account="<spring:theme code='checkout.account.achpayment.popup.title'/> " data-error="<spring:theme code='checkoutcom.billing.address.page.global.field.error'/>">
                                            <label for="customerConsent">
                                                <input type="checkbox" id="customerConsent">
                                                <spring:theme code="checkoutcom.achpayment.checkbox" htmlEscape="false"/>
                                            </label>
                                            <div style="display:none" id="pop-up-consents" data-title="<spring:theme code='checkoutcom.achpayment.popuptitle'/>">
                                                <spring:theme code="checkout.achpayment.popup"/>
                                            </div>
                                        </div>
                                    </form:form>
                                </ycommerce:testId>
                            </div>
                        </div>
                        <div class="form-actions">
                            <button class="btn btn-primary btn-block checkout-next" tabindex="20"
                                    id="nextToPaymentForm">
                                <spring:theme code="checkout.multi.paymentMethod.continue"/>
                            </button>
                        </div>
                    </ycommerce:testId>
                </jsp:body>

            </multiCheckout:checkoutSteps>
        </div>

        <div class="col-sm-6 hidden-xs">
            <multiCheckout:checkoutOrderDetails cartData="${cartData}" showDeliveryAddress="true"
                                                showPaymentInfo="false" showTaxEstimate="false" showTax="true"/>
        </div>

        <div class="col-sm-12 col-lg-12">
            <cms:pageSlot position="SideContent" var="feature" element="div" class="checkout-help">
                <cms:component component="${feature}"/>
            </cms:pageSlot>
        </div>
    </div>

</template:page>
