package de.hendricks.tools.finanzen;

import java.text.Format;
import java.text.NumberFormat;

import javax.swing.table.DefaultTableCellRenderer;

public class MyNumberRenderer extends DefaultTableCellRenderer {
	/**
	* 
	*/
	private static final long serialVersionUID = 1L;
	private Format formatter = NumberFormat.getInstance();

	public void setValue(Object value) {
		try {
			if (value != null)
				value = formatter.format(value);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}

		super.setValue(value);
	}
}
