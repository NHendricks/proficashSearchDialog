package de.hendricks.tools.helper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.stream.Stream;

public class FileHelper {
	
	public static ArrayList<String> getAllLines(File file) throws IOException {
		ArrayList<String> retval = new ArrayList<String>();
		Stream<String> stream = Files.lines(file.toPath(), Charset.forName("ISO8859-1"));
		stream.forEach(line -> {
			retval.add(line);
		});
		stream.close();
		return retval;
	}

}
