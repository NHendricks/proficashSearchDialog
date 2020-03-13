package de.hendricks.tools.finanzen;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.RowFilter.ComparisonType;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import net.sourceforge.jdatepicker.impl.JDatePanelImpl;
import net.sourceforge.jdatepicker.impl.JDatePickerImpl;
import net.sourceforge.jdatepicker.impl.UtilDateModel;

public class TableFilterPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private static JLabel summeLabel;

	public TableFilterPanel(JTable table, TableRowSorter<TableModel> sorter) {
		setLayout(new BorderLayout());
		GridBagConstraints c = new GridBagConstraints();
		addColumnFilter(c, table, sorter);
	}

	private void addColumnFilter(GridBagConstraints c, JTable table, TableRowSorter<TableModel> sorter) {
		String[] ueberschriften = new String[] { "Konto", "Datum", "Betrag (Syntax -100.01)", "Globale Suche" };
		JPanel jPanel = new JPanel();
		jPanel.setLayout(new GridBagLayout());
		int width = 200;
		for (int i = 0; i < 4; i++) {
			c.gridx = i;
			c.gridy = 0;
			c.fill = GridBagConstraints.WEST;
			JLabel ueberschrift = new JLabel();
			ueberschrift.setText(ueberschriften[i]);
			ueberschrift.setHorizontalAlignment(SwingConstants.CENTER);
			ueberschrift.setPreferredSize(new Dimension(width, 20));
			jPanel.add(ueberschrift, c);
		}

		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.WEST;
		JTextField kontoFilter = new JTextField();
		kontoFilter.setPreferredSize(new Dimension(width, 20));
		jPanel.add(kontoFilter, c);

		c.gridx = 1;
		c.gridy = 1;
		c.fill = GridBagConstraints.WEST;
		UtilDateModel model = new UtilDateModel();
		JDatePanelImpl datePanel = new JDatePanelImpl(model);
		JDatePickerImpl datumVonFilter = new JDatePickerImpl(datePanel);
		datumVonFilter.setPreferredSize(new Dimension(width, 20));
		jPanel.add(datumVonFilter, c);


		c.gridx = 1;
		c.gridy = 2;
		c.fill = GridBagConstraints.WEST;
		UtilDateModel modelBis = new UtilDateModel();
		JDatePanelImpl datePanelBis = new JDatePanelImpl(modelBis);
		JDatePickerImpl datumBisFilter = new JDatePickerImpl(datePanelBis);
		jPanel.add(datumBisFilter, c);

		c.gridx = 2;
		c.gridy = 1;
		c.fill = GridBagConstraints.WEST;
		JTextField betragVonFilter = new JTextField();
		betragVonFilter.setPreferredSize(new Dimension(width, 20));
		jPanel.add(betragVonFilter, c);

		c.gridx = 2;
		c.gridy = 2;
		c.fill = GridBagConstraints.WEST;
		JTextField betragBisFilter = new JTextField();
		betragBisFilter.setPreferredSize(new Dimension(width, 20));
		jPanel.add(betragBisFilter, c);

		// globaler filter 1
		c.gridx = 3;
		c.gridy = 1;
		JTextField globalTextSearch = new JTextField();
		globalTextSearch.setBackground(Color.green);
		globalTextSearch.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
		globalTextSearch.setForeground(table.getTableHeader().getForeground());
		globalTextSearch.setHorizontalAlignment(JLabel.CENTER);
		jPanel.add(globalTextSearch, c);
		globalTextSearch.setPreferredSize(new Dimension(400, 20));

		// globaler filter 1
		c.gridx = 3;
		c.gridy = 2;
		JTextField globalTextSearch2 = new JTextField();
		globalTextSearch2.setBackground(Color.green);
		globalTextSearch2.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
		globalTextSearch2.setForeground(table.getTableHeader().getForeground());
		globalTextSearch2.setHorizontalAlignment(JLabel.CENTER);
		jPanel.add(globalTextSearch2, c);
		globalTextSearch2.setPreferredSize(new Dimension(400, 20));

		MyDocumentListener myDocumentListener = new MyDocumentListener(kontoFilter, globalTextSearch, globalTextSearch2, sorter, table,
				datumVonFilter, datumBisFilter, betragVonFilter, betragBisFilter);
		
		kontoFilter.getDocument().addDocumentListener(myDocumentListener);
		betragVonFilter.getDocument().addDocumentListener(myDocumentListener);
		betragBisFilter.getDocument().addDocumentListener(myDocumentListener);
		datumVonFilter.getModel().addPropertyChangeListener(myDocumentListener);
		datumBisFilter.getModel().addPropertyChangeListener(myDocumentListener);
		globalTextSearch.getDocument().addDocumentListener(myDocumentListener);
		globalTextSearch2.getDocument().addDocumentListener(myDocumentListener);
		
		summeLabel = new JLabel(){
		      public Point getToolTipLocation(MouseEvent event) {
		        return new Point(200, -20);
		      }
		    };
		summeLabel.setForeground(table.getTableHeader().getForeground());
		summeLabel.setBackground(table.getTableHeader().getBackground());
		summeLabel.setFont(table.getTableHeader().getFont());

		summeLabel.setHorizontalAlignment(JLabel.LEFT);
		calculateSum(table);
		summeLabel.setPreferredSize(new Dimension(800, 20));

		c.gridx = 7;
		c.gridy = 1;
		c.gridwidth = 2;
		jPanel.add(summeLabel, c);

		add(jPanel, BorderLayout.WEST);

	}

	private static void calculateSum(JTable table) {
		int nr = 0;
		double summe = 0;
		HashMap<String, Number> years2Sum = new HashMap<>();
		for (int row = 0; row < table.getRowCount(); row++) {
			nr++;
			Number valueAt = (Number) table.getModel().getValueAt(table.convertRowIndexToModel(row), 2);
			Date datum = (Date) table.getModel().getValueAt(table.convertRowIndexToModel(row), 1);
			Calendar cal = new GregorianCalendar();
			cal.setTime(datum);
			String key = "" + cal.get(Calendar.YEAR);
			if (years2Sum.get(key)==null) {
				years2Sum.put(key,  valueAt);
			} else {
				Number currentSum = years2Sum.get(key);
				Number newSum = new BigDecimal((currentSum.doubleValue() + valueAt.doubleValue()));
				years2Sum.put(key,  newSum);
			}
			summe = summe + valueAt.doubleValue();
		}
		summeLabel.setText("Summe (" + nr + "):       " + String.format("%1.2f Ä    ", summe) + "(" + years2Sum.keySet().size() + " Jahre)");
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("<table boder=\"1\" cellpadding=\"0\">");
		
		ArrayList<String> years = new ArrayList<>();
		years.addAll(years2Sum.keySet());
		Collections.sort(years);
		for (String year: years) {
			sb.append("<tr>");
			sb.append("<td align=\"left\">");
			sb.append(year);
			sb.append("</td>");
			sb.append("<td align=\"right\">");
			sb.append(NumberFormat.getInstance().format(years2Sum.get(year)));
			sb.append("</td>");
			sb.append("</tr>");
			
			// sb.append("<br>");
		}
		sb.append("</table>");
		sb.append("</html>");
		summeLabel.setToolTipText(sb.toString());
		
		int defaultDismissTimeout = ToolTipManager.sharedInstance().getDismissDelay();

		summeLabel.addMouseListener(new MouseAdapter() {

		  public void mouseEntered(MouseEvent me) {
		    ToolTipManager.sharedInstance().setDismissDelay(60000);
		  }

		  public void mouseExited(MouseEvent me) {
		    ToolTipManager.sharedInstance().setDismissDelay(defaultDismissTimeout);
		  }
		});
	}

	private class MyDocumentListener implements DocumentListener, PropertyChangeListener {

		JTextField globalTextSearch;
		JTextField globalTextSearch2;
		JTextField betragVonFilter;
		JTextField betragBisFilter;
		JTextField kontoFilter;
		JDatePickerImpl datumVonFilter;
		JDatePickerImpl datumBisFilter;
		TableRowSorter<TableModel> sorter;
		JTable table;

		public MyDocumentListener(JTextField kontoFilter, JTextField globalTextSearch, JTextField globalTextSearch2, TableRowSorter<TableModel> sorter, JTable table,
				JDatePickerImpl datumVonFilter, JDatePickerImpl datumBisFilter, JTextField betragVonFilter,
				JTextField betragBisFilter) {
			super();
			this.kontoFilter = kontoFilter;
			this.globalTextSearch = globalTextSearch;
			this.globalTextSearch2 = globalTextSearch2;
			this.sorter = sorter;
			this.table = table;
			this.datumVonFilter = datumVonFilter;
			this.datumBisFilter = datumBisFilter;
			this.betragVonFilter = betragVonFilter;
			this.betragBisFilter = betragBisFilter;
		}

		@Override
		public void insertUpdate(DocumentEvent notUsedEvent) {
			ArrayList<RowFilter<Object, Object>> filters = new ArrayList<>();

			// genereller Filter 1
			String kontoText = kontoFilter.getText();
			if (!kontoText.equals("")) {
				RowFilter<Object, Object> globalFilter = RowFilter.regexFilter("(?i)" + kontoText, 0);
				filters.add(globalFilter);
			}

			Date parsedVonDate = (Date) datumVonFilter.getModel().getValue();
			if (parsedVonDate != null) {
				// einen Tag abziehen, da wir einschlieﬂlich diesen Datums suchen wollen
				Date compareDate = new Date(parsedVonDate.getTime() - (long)1*1000*60*60*24 );
				RowFilter<Object, Object> datumVonFilter = RowFilter.dateFilter(ComparisonType.AFTER, compareDate, 1);
				filters.add(datumVonFilter);					
			}
			Date parsedBisDate = (Date) datumBisFilter.getModel().getValue();
			if (parsedBisDate != null) {					
				RowFilter<Object, Object> datumFilter = RowFilter.dateFilter(ComparisonType.BEFORE, parsedBisDate, 1);
				filters.add(datumFilter);
			}
			datumBisFilter.setBackground(Color.white);
			if (!"".equals(betragVonFilter.getText())) {
				try {
					Number number = new BigDecimal(betragVonFilter.getText());
					RowFilter<Object, Object> betragFilter = RowFilter.numberFilter(ComparisonType.AFTER, number,
							2);
					filters.add(betragFilter);
					betragVonFilter.setBackground(Color.white);
				} catch (NumberFormatException e1) {
					betragVonFilter.setBackground(Color.red);
				}
			}
			if (!"".equals(betragBisFilter.getText())) {
				try {
					Number number = new BigDecimal(betragBisFilter.getText());
					RowFilter<Object, Object> betragFilter = RowFilter.numberFilter(ComparisonType.BEFORE, number,
							2);
					filters.add(betragFilter);
					betragBisFilter.setBackground(Color.white);
				} catch (NumberFormatException e1) {
					betragBisFilter.setBackground(Color.red);
				}
			}
			// genereller Filter 1
			String text = globalTextSearch.getText();
			if (!text.equals("")) {
				RowFilter<Object, Object> globalFilter = RowFilter.regexFilter("(?i)" + text);
				filters.add(globalFilter);
			}
			// genereller Filter 2
			String text2 = globalTextSearch2.getText();
			if (!text2.equals("")) {
				RowFilter<Object, Object> globalFilter2 = RowFilter.regexFilter("(?i)" + text2);
				filters.add(globalFilter2);
			}
			// sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 0));
			sorter.setRowFilter(RowFilter.andFilter(filters));
			calculateSum(table);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			insertUpdate(e);

		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			insertUpdate(e);
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			insertUpdate(null);
		}
	}
}
