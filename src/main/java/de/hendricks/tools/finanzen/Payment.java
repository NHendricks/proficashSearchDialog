package de.hendricks.tools.finanzen;

import java.math.BigDecimal;
import java.util.ArrayList;

public class Payment implements Cloneable {
	private String account;
	private String date;
	private String amount;
	private String vz;
	private String type;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	private String toBank;
	private String toAccount;
	private String toName;
	private String reason;
	private String reason2;
	private String category;

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getVz() {
		return vz;
	}

	public void setVz(String vz) {
		this.vz = vz;
	}

	public String getToBank() {
		return toBank;
	}

	public void setToBank(String toBank) {
		this.toBank = toBank;
	}

	public String getToAccount() {
		return toAccount;
	}

	public void setToAccount(String toAccount) {
		this.toAccount = toAccount;
	}

	public String getToName() {
		return toName;
	}

	public void setToName(String toName) {
		this.toName = toName;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getReason2() {
		return reason2;
	}

	public void setReason2(String reason2) {
		this.reason2 = reason2;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public ArrayList<String> getRow() {
		ArrayList<String> myRow = new ArrayList<String>();
		myRow.add(account);
		myRow.add(date);
		myRow.add(vz + amount);
		myRow.add(type);
		myRow.add(toBank);
		myRow.add(toAccount);
		myRow.add(toName);
		if (reason.startsWith("KREF") || reason.startsWith("EREF")) {
			myRow.add(reason2.trim() + " " + reason.trim());
		} else {
			myRow.add(reason.trim() + " " + reason2.trim());
		}
		myRow.add(category);
		return myRow;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}

	public BigDecimal getAmountBd() {
		BigDecimal retVal = new BigDecimal(amount);
		return retVal;
	}

	public void setAmountBd(BigDecimal newAmountBd) {
		this.amount = newAmountBd.toString();
	}

}
