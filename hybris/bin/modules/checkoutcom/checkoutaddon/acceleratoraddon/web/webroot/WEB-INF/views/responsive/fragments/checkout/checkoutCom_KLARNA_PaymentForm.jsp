<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>

<div id="klarnaContainer" class="klarna-container"></div>

<div id="klarnaError" class="global-alerts" style="display: none">
    <div class="alert alert-danger">
        <spring:theme code="checkoutcom.klarna.not.available.error.message"/>
    </div>
</div>

<c:set var="billingAddress" value="${cartData.checkoutComPaymentInfo.billingAddress}"/>
<c:set var="deliveryAddress" value="${cartData.deliveryAddress}"/>

<form:form id="checkoutComPaymentTokenForm"
           modelAttribute="paymentDataForm"
           method="POST"
           action="${submitPaymentDataUrl}">
    <form:hidden id="authorizationToken" path="formAttributes['${'authorizationToken'}']" value=""/>
    <form:hidden id="type" path="formAttributes['${'type'}']" value="${paymentMethod}"/>
</form:form>

<script>
    document.addEventListener("DOMContentLoaded", function (event) {
        event.preventDefault();
        document.getElementById('submitPaymentForm').id = 'submitKlarnaForm';
    });

    <c:choose>
        <c:when test="${not empty klarnaClientToken and not empty klarnaClientToken.clientToken}">
            let paymentMethodCategoryList = [
                <c:forEach items="${klarnaClientToken.paymentMethodCategories}" var="paymentMethodCategory" varStatus="status">
                '${paymentMethodCategory}'
                <c:if test="${!status.last}">
                ,
                </c:if>
                </c:forEach>
            ];

            window.klarnaSettings = {
                klarnaInstanceId:  '${klarnaClientToken.instanceId}',
                clientToken:  '${klarnaClientToken.clientToken}',
                paymentMethodCategories: paymentMethodCategoryList,
                backButton: '<spring:theme code="checkoutcom.klarna.error.back.button.label"/>',
                paymentMethod: '${paymentMethod}',
                billingAddress: {
                    title: '${ycommerce:encodeJavaScript(billingAddress.title)}',
                    givenName: '${ycommerce:encodeJavaScript(billingAddress.firstName)}',
                    familyName: '${ycommerce:encodeJavaScript(billingAddress.lastName)}',
                    addressLine1: '${ycommerce:encodeJavaScript(billingAddress.line1)}',
                    addressLine2: '${ycommerce:encodeJavaScript(billingAddress.line2)}',
                    city: '${ycommerce:encodeJavaScript(billingAddress.town)}',
                    postalCode: '${ycommerce:encodeJavaScript(billingAddress.postalCode)}',
                    region: '${ycommerce:encodeJavaScript(billingAddress.region.name)}',
                    countryCode: '${ycommerce:encodeJavaScript(billingAddress.country.isocode)}',
                    emailAddress: '${ycommerce:encodeJavaScript(billingAddress.email)}',
                    phoneNumber: '${ycommerce:encodeJavaScript(billingAddress.phone)}'
                },
                deliveryAddress: {
                    title: '${ycommerce:encodeJavaScript(deliveryAddress.title)}',
                    givenName: '${ycommerce:encodeJavaScript(deliveryAddress.firstName)}',
                    familyName: '${ycommerce:encodeJavaScript(deliveryAddress.lastName)}',
                    addressLine1: '${ycommerce:encodeJavaScript(deliveryAddress.line1)}',
                    addressLine2: '${ycommerce:encodeJavaScript(deliveryAddress.line2)}',
                    city: '${ycommerce:encodeJavaScript(deliveryAddress.town)}',
                    postalCode: '${ycommerce:encodeJavaScript(deliveryAddress.postalCode)}',
                    region: '${ycommerce:encodeJavaScript(deliveryAddress.region.name)}',
                    countryCode: '${ycommerce:encodeJavaScript(deliveryAddress.country.isocode)}',
                    emailAddress: '${ycommerce:encodeJavaScript(billingAddress.email)}',
                    phoneNumber: '${ycommerce:encodeJavaScript(deliveryAddress.phone)}'
                }
            };
        </c:when>
        <c:otherwise>
            document.addEventListener("DOMContentLoaded", function (event) {
                var divTemplate = document.createElement('div');
                var currentUrl = window.location.href.replace('payment/payment-method', 'choose-payment-method');
                divTemplate.innerHTML = '<a id="backToPaymentMethods" class="btn btn-primary btn-block" href="' + currentUrl + '"><spring:theme code="checkoutcom.klarna.error.back.button.label"/></a>';
                if (document.querySelector('#backToPaymentMethods') === null || document.querySelector('#backToPaymentMethods') === undefined) {
                    var parentDiv = document.querySelector('#buttons-container');
                    parentDiv.insertBefore(divTemplate.firstChild, parentDiv.childNodes[0]);
                }
                document.querySelector('#klarnaContainer').style.display = 'none';
                document.querySelector('#klarnaError').style.display = 'block';
                document.getElementById('submitKlarnaForm').disabled = true;
                document.getElementById('submitKlarnaForm').classList.remove('checkout-next');
            });
        </c:otherwise>
    </c:choose>
</script>