<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/responsive/template" %>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="chko-multi-checkout" tagdir="/WEB-INF/tags/addons/checkoutaddon/responsive/checkout/multi" %>

<input type="hidden" id="previous-selected-payment" value="${selectedPaymentMethod}">

<form:form id="checkoutComPaymentDetailsForm"
           modelAttribute="paymentDetailsForm"
           method="post"
           action="${addPaymentAddressUrl}"
           class="create_update_payment_form">

    <div id="payment-method-container">
        <chko-multi-checkout:checkoutComPaymentButtons/>
    </div>

</form:form>

