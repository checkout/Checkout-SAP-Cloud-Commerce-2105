<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>



<c:set var="hasAvailablePaymentMethods" value="${hasAvailablePaymentMethods or isAvailable}" scope="session"/>


<c:if test="${apmConfiguration.code eq 'ACH'}">
    <script src="https://cdn.plaid.com/link/v2/stable/link-initialize.js"></script>
</c:if>


<form:radiobutton id="paymentMethod_${apmConfiguration.code}" path="paymentMethod" disabled="${!isAvailable}"
                  value="${apmConfiguration.code}" cssClass="available-${isAvailable}" data-required="${isUserDataRequired}"
                  data-redirect="${isRedirect}"/>
<label class="available-${isAvailable}" for="paymentMethod_${apmConfiguration.code}">
    <c:choose>
        <c:when test="${not empty media.url}">
            <img src="${media.url}" title="${apmConfiguration.name}" alt="${apmConfiguration.name}"/>
            <span id="apm-payment-method-caption">${apmConfiguration.name}</span>
        </c:when>
        <c:otherwise>
            <div class="apm-img-not-found">${apmConfiguration.name}</div>
        </c:otherwise>
    </c:choose>
</label>
