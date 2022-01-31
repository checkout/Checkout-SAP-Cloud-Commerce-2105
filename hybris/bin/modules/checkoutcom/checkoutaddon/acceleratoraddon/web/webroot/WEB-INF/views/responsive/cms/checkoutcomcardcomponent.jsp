<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="hasAvailablePaymentMethods" value="true" scope="session"/>
<spring:theme var="paymentMethodName" code="checkoutcom.paymentMethod.card"/>

<form:radiobutton id="paymentMethod_CARD" path="paymentMethod" cssClass="available-true" value="CARD" data-required="true" data-redirect="false"/>
<label class="available-true" id="card-payment-method" for="paymentMethod_CARD">
    <c:choose>
        <c:when test="${not empty button.media.url}">
            <img src="${button.media.url}" title="${paymentMethodName}" alt="${paymentMethodName}"/>
            <span id="card-payment-method-caption"><spring:theme
                    code="checkoutcom.multi.paymentMethod.card.caption"/></span>
        </c:when>
        <c:otherwise>
            <div class="card-img-not-found">${paymentMethodName}</div>
        </c:otherwise>
    </c:choose>
</label>
