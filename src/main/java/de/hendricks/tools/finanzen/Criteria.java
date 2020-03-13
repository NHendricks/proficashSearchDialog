package de.hendricks.tools.finanzen;

import java.util.StringTokenizer;

public class Criteria {
	private String column = null;
	private String substring = null;
	private String category = null;

	public Criteria(String definition) throws Exception {
		StringTokenizer tok = new StringTokenizer(definition, "@");
		if (tok.countTokens() != 3) {
			throw new Exception("Invalid definition " + definition);
		} else {
			column = tok.nextToken();
			substring = tok.nextToken();
			category = tok.nextToken();
		}
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
