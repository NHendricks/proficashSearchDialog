package de.hendricks.tools.finanzen;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import de.hendricks.tools.finanzen.fileformat.Metadata;
import de.hendricks.tools.finanzen.fileformat.Lines2ObjectsFormatter;
import de.hendricks.tools.helper.FileHelper;

public class AccountFrame {
	static TableRowSorter<TableModel> sorter;

	static int[] zeilenBreiten = new int[] { 140, 120, 120, 120, 120, 120, 320, 300, 200 };

	static int breiteInsgesamt = 0;

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage TabelleKonten file.csv 2010 2011 [-Dfilter=xxx]");
			System.out.println("file.csv = export aus proficash (Datei - Ausführen Export - Umsätze und Salden - feste Satzlänge 768)");
			System.out.println("file.criteria = Datei mit Kategorien je Zeile ein Filter. z.B. Empfänger@DANKE IHR KOELLE-ZOO MS@Tiere oder VWZ@ERDGAS ABSCHLAG@Haus.SWGAS");
			System.out.println("file.");
			System.exit(-1);
		}
		try {
			for (int i : zeilenBreiten) {
				breiteInsgesamt = breiteInsgesamt + i;
			}
			// einlesen
			File baseFile = new File(args[0]);
			String fileContent = FileHelper.getAllLines(baseFile).stream().collect(Collectors.joining("\n"));
			ArrayList<Object> columnNamesList = Lines2ObjectsFormatter.readCsvHeader();
			ArrayList<Object[]> rowDataList = Lines2ObjectsFormatter.readAllLines(fileContent.toString(), Metadata.getInstance(baseFile));

			columnNamesList = collectRowsSimple(columnNamesList, new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8 });
			rowDataList = collectRows(rowDataList, new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8 });

			String[] columnNames = columnNamesList.toArray(new String[] {});
			Object[][] rowData = rowDataList.toArray(new Object[][] {});

			JFrame f = new JFrame();
			f.setTitle("Umsatzfilter");
			// f.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			JPanel hauptPanel = new JPanel();
			hauptPanel.setLayout(new BorderLayout());
			f.getContentPane().add(hauptPanel);

			JTable table = new PaymentTable(rowData, columnNames);
			table.getColumnModel().getColumn(1).setCellRenderer(new MyDateRenderer());
			JPanel tablePanel = createTablePanel(table);
			tablePanel.add(new TableFilterPanel(table, sorter), BorderLayout.NORTH);
			tablePanel.setPreferredSize(new Dimension(breiteInsgesamt + 100, 800));
			hauptPanel.add(tablePanel, BorderLayout.CENTER);

			f.pack();
			f.setVisible(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static JPanel createTablePanel(JTable table) {
		JPanel tablePanel = new JPanel();
		tablePanel.setLayout(new BorderLayout());
		JScrollPane jsp = new JScrollPane(table);
		tablePanel.add(jsp, BorderLayout.CENTER);

		sorter = new TableRowSorter<TableModel>(table.getModel());
		TableColumnModel cm = table.getColumnModel();
		cm.getColumn(0).setPreferredWidth(zeilenBreiten[0]);
		cm.getColumn(1).setPreferredWidth(zeilenBreiten[1]);
		cm.getColumn(2).setPreferredWidth(zeilenBreiten[2]);
		cm.getColumn(3).setPreferredWidth(zeilenBreiten[3]);
		cm.getColumn(4).setPreferredWidth(zeilenBreiten[4]);
		cm.getColumn(5).setPreferredWidth(zeilenBreiten[5]);
		cm.getColumn(6).setPreferredWidth(zeilenBreiten[6]);
		cm.getColumn(7).setPreferredWidth(zeilenBreiten[7]);
		cm.getColumn(8).setPreferredWidth(zeilenBreiten[8]);
		table.setRowSorter(sorter);
		return tablePanel;
	}

	private static ArrayList<Object[]> collectRows(ArrayList<Object[]> rowDataList, int[] is) {
		ArrayList<Object[]> retVal = new ArrayList<Object[]>();
		for (int i = 0; i < rowDataList.size(); i++) {
			Object[] row = rowDataList.get(i);
			ArrayList<Object> rowData = new ArrayList<Object>();
			for (int j = 0; j < row.length; j++) {
				rowData.add(row[j]);
			}
			ArrayList<Object> reducedRow = collectRowsSimple(rowData, is);
			retVal.add(reducedRow.toArray(new Object[] {}));
		}
		return retVal;
	}

	private static ArrayList<Object> collectRowsSimple(ArrayList<Object> columnList, int[] is) {
		ArrayList<Object> retVal = new ArrayList<Object>();
		for (int i = 0; i < is.length; i++) {
			retVal.add(columnList.get(is[i]));
		}
		return retVal;
	}

}
