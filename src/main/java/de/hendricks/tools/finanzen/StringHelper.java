package de.hendricks.tools.finanzen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.StringTokenizer;

public class StringHelper {

	static Hashtable<String, BigDecimal> sums = new Hashtable<String, BigDecimal>();
	static String filter = System.getProperty("filter");
	static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

	public static ArrayList<Object[]> readCsv(String s, Metadata metadata) throws IOException {
		ArrayList<Object[]> myList = new ArrayList<Object[]>();
		BufferedReader br = new BufferedReader(new StringReader(s));
		String line = null;
		while ((line = br.readLine()) != null) {
			String account = line.substring(0, 10);
			String date = line.substring(10, 20);
			String year = line.substring(16, 20);
			String amount = line.substring(45, 57);
			String vz = line.substring(57, 58);
			if ("R".equals(vz)) {
				// bei sparkassen-Umsätzen aus älterer Zeit komischerweise
				vz = line.substring(58, 59);
			}
			String type = line.substring(82, 109);
			String toBank = line.substring(119, 127);
			String toAccount = line.substring(131, 154);
			String toName = line.substring(212, 266);
			String reason = line.substring(266, 321).trim();
			String reason2 = line.substring(347, 700).trim();
			int indexoOfVerwendungszweck = reason.indexOf("SVWZ+");
			if (indexoOfVerwendungszweck != -1) {
				reason = reason.substring(indexoOfVerwendungszweck + 4);
			}
			int indexOfVerwendungszweck2 = reason2.indexOf("SVWZ+");
			if (indexOfVerwendungszweck2 != -1) {
				reason2 = reason2.substring(indexOfVerwendungszweck2 + 5);
			}

			amount = amount.trim().replace(",", ".");
			Payment payment = new Payment();
			payment.setAccount(account);
			payment.setDate(date);
			payment.setAmount(amount);
			payment.setVz(vz);
			payment.setType(type);
			payment.setToBank(toBank);
			payment.setToAccount(toAccount);
			payment.setToName(toName);
			payment.setReason(reason);
			payment.setReason2(reason2);
			payment.setCategory(metadata.getCategory(payment.getRow()));

			ArrayList<Payment> splittedPayments = split(payment);
			// ArrayList<Payment> splittedPayments = null;
			// ggf. splitten (Kindergeld und Gehalt sind 1 Umsatz)
			if (splittedPayments == null) {
				splittedPayments = new ArrayList<Payment>();
				splittedPayments.add(payment);
			}
			for (Payment subPayment : splittedPayments) {
				BigDecimal sum = sums.get(subPayment.getCategory());
				if (sum == null) {
					sum = new BigDecimal(0);
				}
				BigDecimal amountBd = new BigDecimal(0);
				try {
					amountBd = new BigDecimal(subPayment.getVz() + subPayment.getAmount());
				} catch (Throwable e) {
					System.out.println("Could not convert to BigDecimal:" + subPayment.getVz() + subPayment.getAmount()
							+ " in line " + line);
					e.printStackTrace();
				}
				sum = sum.add(amountBd);
				sums.put(subPayment.getCategory(), sum);
				// Sonderlocke nur xxx-Umsätze anzeigen
				if (filter == null || filter != null && filter.equals(subPayment.getCategory())) {
					Object[] thisRowDataAsObjects = format2DateOrNumberObjectIfNeeded(
							subPayment.getRow().toArray(new String[] {}));

					myList.add(thisRowDataAsObjects);
				}
				// zusätzlich zur ober-Kategorie summieren
				if (subPayment.getCategory().indexOf(".") != -1) {
					String parentCategory = subPayment.getCategory().substring(0,
							subPayment.getCategory().indexOf("."));
					sum = sums.get(parentCategory);
					if (sum == null) {
						sum = new BigDecimal(0);
					}
					sum = sum.add(amountBd);
					sums.put(parentCategory, sum);
				} else {
					// Sonstige-Unterkategorie pflegen
					String parentCategory = subPayment.getCategory() + ".xxx";
					sum = sums.get(parentCategory);
					if (sum == null) {
						sum = new BigDecimal(0);
					}
					sum = sum.add(amountBd);
					sums.put(parentCategory, sum);
				}
			}
		}
		ArrayList<String> categories = new ArrayList<String>();
		for (String category : sums.keySet()) {
			categories.add(category);
		}
		Collections.sort(categories);
		for (String category : categories) {
			BigDecimal sum = sums.get(category);
			String formattedSum = "" + sum;
			while (formattedSum.length() < 10) {
				formattedSum = " " + formattedSum;
			}
			while (category.length() < 50) {
				category = category + " ";
			}
			if (category.indexOf(".") == -1) {
				// Haupt-Kategorie
				System.out.println("==========================");
				System.out.println(category + ":" + formattedSum);
			} else {
				System.out.println(category + ":" + formattedSum);
			}
		}
		return myList;
	}

	private static Object[] format2DateOrNumberObjectIfNeeded(String[] array) {
		Object[] retVal = new Object[array.length];
		for (int i = 0; i < array.length; i++) {
			if (i == 1) {
				// datum
				String datum = array[i];
				try {
					retVal[i] = sdf.parse(datum);
				} catch (ParseException e) {
					e.printStackTrace();
					retVal[i] = new Date(0);
				}
			} else if (i == 2) {
				// Number
				String amount = array[i];
				Number number = new BigDecimal(amount);
				retVal[i] = number;
			} else {
				retVal[i] = array[i];
			}
		}
		return retVal;
	}

	private static ArrayList<Payment> split(Payment payment) {
		if (payment.getCategory().indexOf("/") == -1) {
			return null;
		}
		ArrayList<Payment> payments = new ArrayList<Payment>();
		StringTokenizer tok = new StringTokenizer(payment.getCategory(), "_");
		while (tok.hasMoreTokens()) {
			String subcat = tok.nextToken();
			StringTokenizer tokSlash = new StringTokenizer(subcat, "/");
			if (tokSlash.countTokens() != 2) {
				System.out.println("Falsche Sub-Kategorie:" + subcat);
			} else {
				String subcatName = tokSlash.nextToken();
				String value = tokSlash.nextToken();
				if (value.equals("Rest")) {
					payment.setCategory(subcatName);
				} else {
					BigDecimal valueBd = new BigDecimal(value);
					// Vom Umsatz abziehen
					if (payment.getAmountBd().compareTo(valueBd) > 0) {
						BigDecimal subtracted = payment.getAmountBd().subtract(valueBd);

						payment.setAmountBd(subtracted);
						// neuer Eintrag
						try {
							Payment newPayment = (Payment) payment.clone();
							newPayment.setAmountBd(valueBd);
							newPayment.setCategory(subcatName);
							payments.add(newPayment);
						} catch (CloneNotSupportedException e) {
							e.printStackTrace();
							System.out.println("Clone not successfull.");
							System.exit(-1);
						}
					}
				}
			}
		}
		payments.add(payment);
		return payments;
	}

	public static String[] header = new String[] { "Konto", "Datum", "Betrag", "Typ", "BLZ", "Konto", "Empfänger",
			"VWZ", "Kategorie" };

	public static ArrayList<Object> readCsvHeader() throws IOException {
		ArrayList<Object> myList = new ArrayList<Object>();
		myList.add(header[0]);
		myList.add(header[1]);
		myList.add(header[2]);
		myList.add(header[3]);
		myList.add(header[4]);
		myList.add(header[5]);
		myList.add(header[6]);
		myList.add(header[7]);
		myList.add(header[8]);
		return myList;
	}
}
