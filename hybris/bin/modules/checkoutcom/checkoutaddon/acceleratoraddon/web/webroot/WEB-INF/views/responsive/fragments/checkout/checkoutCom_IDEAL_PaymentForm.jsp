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
    <div class="ideal-form-container">
        <label class="paymentFormFieldLabel" for="formAttributes['${'bic'}']"><spring:theme
                code="checkoutcom.payment.ideal.bic.label"
                text="IBAN"/></label>

        <form:input cssClass="form-control" type="text" id="bic"
                    path="formAttributes['${'bic'}']"/>
        <form:errors path="formAttributes['${'bic'}']" cssClass="error-message"/>

        <form:hidden id="type" path="formAttributes['${'type'}']" value="${paymentMethod}"/>
    </div>
</form:form>
