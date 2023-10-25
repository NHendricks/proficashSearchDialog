package de.nhendricks.proficash.fileformat;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.stream.Collectors;

import de.nhendricks.proficash.helper.FileHelper;

public class Metadata {

	static Metadata instance = null;
	ArrayList<Criteria> criterias = new ArrayList<Criteria>();

	public ArrayList<Criteria> getCriterias() {
		return criterias;
	}


	public Metadata(File file) {
		File criteriaFile = new File(file.getAbsolutePath() + ".criteria");
		try {
			String s = FileHelper.getAllLines(criteriaFile).stream().collect(Collectors.joining("\n"));
			BufferedReader br = new BufferedReader(new StringReader(s));
			String line = null;
			while ((line = br.readLine()) != null) {
				try {
					if (!line.startsWith("REM")) {
						Criteria criteria = new Criteria(line);
						criterias.add(criteria);
					}
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}
		} catch (IOException e) {
			System.out.println("No criteria File found at " + criteriaFile.getAbsolutePath());
		}
	}

	public String getCategory(String column, String value) {
		for (Criteria crit : criterias) {
			String category = crit.getCategory(column, value);
			if (category != null) {
				return category;
			}
		}
		return null;
	}

	public static Metadata getInstance(File file) {
		if (instance == null) {
			instance = new Metadata(file);
		}
		return instance;
	}

	public String getCategory(ArrayList<String> myRow) {
		for (int i = 0; i < myRow.size(); i++) {
			String columnValue = myRow.get(i);
			String columnName = Lines2ObjectsFormatter.header[i];
			String newCategory = getCategory(columnName, columnValue);
			if (newCategory != null) {
				return newCategory;
			}
		}
		return "---";
	}

}
