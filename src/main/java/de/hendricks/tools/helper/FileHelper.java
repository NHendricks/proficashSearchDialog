package de.hendricks.tools.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


public class FileHelper {

	public static String readFile(File file, boolean onlyFirstLine, boolean skipFirstLine) throws IOException{
		StringBuffer retVal = new StringBuffer();
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		int i = 0;
		while ((line=reader.readLine())!=null){
			i++;
			if (i==1 && skipFirstLine){
				// skip this line
			} else {
				retVal.append(line + "\n");
				if (onlyFirstLine){
					return retVal.toString();
				}				
			}
		}
		reader.close();
		return retVal.toString();
	}
}
