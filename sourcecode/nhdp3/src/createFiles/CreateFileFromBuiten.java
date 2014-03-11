package createFiles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ckling.text.Text;

import ckling.db.Database;

public class CreateFileFromBuiten {

	public static void main (String[] args) throws SQLException, IOException {

		Database db = new Database();
		Database db2 = new Database();

		int count = 0;
		String last = null;
		File file = new File("/home/c/buiten.txt");
		FileWriter writer = new FileWriter(file );
		Text text = new Text();
		text.setLang("du");
		text.setStem(true);
		text.setStopwords(false);
		
		List<String> lines = new LinkedList<String>();
		
		FileReader reader = new FileReader("/home/c/work/jgibblda/models/buiten/data.txt");
		BufferedReader bReader = new BufferedReader(reader);
		
		
		
		String line;
		while((line = bReader.readLine()) != null) {
		
			String[] split = line.split("\t");
			//System.out.println(line);
			System.out.println(split[30]);
			text.setText(split[30]);
			
			String terms = "";
			Iterator<String> iterator = text.getTerms();
			while (iterator.hasNext()) {
				terms += " " + iterator.next(); 
			}
			terms = terms.trim();
			
			//String text = split[31].replace(" ","_");
			//String text = split[30];
			String lat = split[22].replace(",", ".");
			String lon = split[23].replace(",", ".");
		
			lines.add(lat + " " + lon + " " +terms);
		}
	
		Collections.shuffle(lines); 
		count = lines.size();
		writer.write(count);
		writer.write("\n");
		writer.flush();
		
		for (Iterator<String> outputLine = lines.iterator(); outputLine.hasNext(); ) {
			
			writer.write(outputLine.next());
			//no break after last line
			if (outputLine.hasNext()) {
				writer.write("\n");
			}
			writer.flush();
			
		}

		writer.close();


	}
	
}
