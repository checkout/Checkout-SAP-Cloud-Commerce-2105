package com.checkout.hybris.addon.forms;

public class CreditCardDataForm {
	private int expiryYear;
	private int expiryMonth;
	private String accountHolderName;

	private Long creditCardCode;

	public int getExpiryYear() {
		return expiryYear;
	}

	public void setExpiryYear(final int expiryYear) {
		this.expiryYear = expiryYear;
	}

	public int getExpiryMonth() {
		return expiryMonth;
	}

	public void setExpiryMonth(final int expiryMonth) {
		this.expiryMonth = expiryMonth;
	}

	public String getAccountHolderName() {
		return accountHolderName;
	}

	public void setAccountHolderName(final String accountHolderName) {
		this.accountHolderName = accountHolderName;
	}

	public Long getCreditCardCode() {
		return creditCardCode;
	}

	public void setCreditCardCode(final Long creditCardCode) {
		this.creditCardCode = creditCardCode;
	}
}
