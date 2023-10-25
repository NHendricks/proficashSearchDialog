package de.nhendricks.proficash.fileformat;

import java.util.StringTokenizer;

public class Criteria {
	private String column = null;
	private String substring = null;
	private String category = null;

	public Criteria(String definition) {
		StringTokenizer tok = new StringTokenizer(definition, "@");
		column = tok.nextToken();
		substring = tok.nextToken();
		category = tok.nextToken();
	}

	public String getCategory(String column, String value) {
		if (column.equals(this.column)) {
			if (value.indexOf(substring) != -1) {
				return category;
			}
		}
		return null;
	}
}
