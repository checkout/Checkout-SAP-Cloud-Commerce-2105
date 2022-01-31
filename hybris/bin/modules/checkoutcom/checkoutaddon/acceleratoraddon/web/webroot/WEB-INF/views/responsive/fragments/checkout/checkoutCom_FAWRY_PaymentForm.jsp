<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<form:form id="checkoutComPaymentDataForm"
           modelAttribute="paymentDataForm"
           method="POST"
           action="${submitPaymentDataUrl}"
           cssClass="paymentDataForm">
    <div class="mobile-number">
        <label class="paymentFormFieldLabel" for="formAttributes['${'mobileNumber'}']"><spring:theme
                code="checkoutcom.fawry.mobile.number.label"
                text="MOBILE NUMBER"/></label>

        <c:set var="phoneNumber" value=""/>
        <c:if test="${not empty cartData.checkoutComPaymentInfo.billingAddress.phone}">
            <c:set var="phoneNumber" value="${cartData.checkoutComPaymentInfo.billingAddress.phone}"/>
        </c:if>

        <form:input cssClass="form-control" type="text" id="fawryMobileNumber"
                    path="formAttributes['${'mobileNumber'}']"
                    value="${phoneNumber}"/>
        <form:errors path="formAttributes['${'mobileNumber'}']" cssClass="error-message"/>

        <form:hidden id="type" path="formAttributes['${'type'}']" value="${paymentMethod}"/>
    </div>
</form:form>
