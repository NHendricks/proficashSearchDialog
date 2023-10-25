package de.nhendricks.proficash;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import de.nhendricks.proficash.fileformat.Lines2ObjectsFormatter;
import de.nhendricks.proficash.fileformat.Metadata;
import de.nhendricks.proficash.helper.FileHelper;
import de.nhendricks.proficash.renderer.MyDateRenderer;

public class Main {
	static TableRowSorter<TableModel> sorter;

	static int[] zeilenBreiten = new int[] { 140, 120, 120, 120, 120, 120, 320, 300, 200 };

	static int breiteInsgesamt = 0;

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage  <file>");
			System.out.println("file = exported file from proficash (\"feste Satzlänge 768\")");
			System.out.println("file.criteria = File with categories to be automatically added (see example files in the git repository)");
			System.exit(-1);
		}
		try {
			for (int i : zeilenBreiten) {
				breiteInsgesamt = breiteInsgesamt + i;
			}
			// einlesen
			File baseFile = new File(args[0]);
			String fileContent = FileHelper.getAllLines(baseFile).stream().collect(Collectors.joining("\n"));
			ArrayList<Object> columnNamesList = Lines2ObjectsFormatter.getHeaders();
			ArrayList<Object[]> rowDataList = Lines2ObjectsFormatter.readAllLines(fileContent.toString(), Metadata.getInstance(baseFile));

			columnNamesList = collectRowsSimple(columnNamesList, new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8 });
			rowDataList = collectRows(rowDataList, new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8 });

			String[] columnNames = columnNamesList.toArray(new String[] {});
			Object[][] rowData = rowDataList.toArray(new Object[][] {});

			JFrame f = new JFrame();
			f.setTitle("ProficashSearchDialog");
			// f.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			JPanel hauptPanel = new JPanel();
			hauptPanel.setLayout(new BorderLayout());
			Border border = hauptPanel.getBorder();
			Border margin = new LineBorder(Color.gray,4);
			hauptPanel.setBorder(new CompoundBorder(border, margin));
			f.getContentPane().add(hauptPanel);

			JTable table = new PaymentTable(rowData, columnNames);
			table.getColumnModel().getColumn(1).setCellRenderer(new MyDateRenderer());
			JPanel tablePanel = createTablePanel(table);
			tablePanel.add(new TableFilterPanel(table, sorter), BorderLayout.NORTH);
			tablePanel.setPreferredSize(new Dimension(breiteInsgesamt + 100, 800));
			hauptPanel.add(tablePanel, BorderLayout.CENTER);

			f.pack();
			f.setExtendedState(f.getExtendedState() | JFrame.MAXIMIZED_BOTH);
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
