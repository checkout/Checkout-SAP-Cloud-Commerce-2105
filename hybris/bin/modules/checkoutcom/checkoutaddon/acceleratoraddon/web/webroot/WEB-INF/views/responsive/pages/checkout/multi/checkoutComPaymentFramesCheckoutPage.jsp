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

                                    <c:if test="${not empty paymentInfos && 'CARD' == paymentMethod}">
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

                                    <c:url value="/checkout/multi/checkout-com/payment/submit-payment-data"
                                           var="submitPaymentDataUrl"/>
                                    <jsp:include
                                            page="../../../fragments/checkout/checkoutCom_${paymentMethod}_PaymentForm.jsp"/>

                                </ycommerce:testId>
                            </div>
                        </div>
                        <div id="buttons-container" class="form-actions">
                            <button class="btn btn-primary btn-block checkout-next" tabindex="20"
                                    id="submitPaymentForm">
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
