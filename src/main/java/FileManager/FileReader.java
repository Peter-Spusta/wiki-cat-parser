package FileManager;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class FileReader {
	
	FileInputStream fstream;
	static List<String> page = new ArrayList<String>();
	static BufferedReader br;
	static String textAsString;
	Iterator<String> it;
	public static int index = 0;

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public Iterator<String> getIt() {
		return it;
	}

	public void setIt(Iterator<String> it) {
		this.it = it;
	}

	public void readFile(String path) throws IOException {
		setFstream(new FileInputStream(path));
		setBr(new BufferedReader(new InputStreamReader(fstream)));
	}
	
	public void closeFile() throws IOException {
		fstream.close();
	}
	
	/*public static File getFileFromPath(String path) {
		return new File(path);
	}*/
	
	public static void readNextPage() throws IOException {
		String strLine;
		Boolean pageFound = false;
		page = new ArrayList<String>();

		while ((strLine = br.readLine()) != null)   {
			
			if (Pattern.matches("\\s*\\<page\\>", strLine)) {
				pageFound = true;
			}
			
			if (pageFound) {
				page.add(strLine);
			}
			
			if (Pattern.matches("\\s*\\</page\\>", strLine)) {
				pageFound = false;
				break;
			}
		}
		
		if(strLine == null) {
			br = null;
		}
	}
	
	public static void readNextPage(List<List<String>> file) throws IOException {
		Boolean pageFound = false;
		page = new ArrayList<String>();

		while(index < file.size())	{
			//System.out.println("++++++++++++++++++++++++++++++++++++++");
			//System.out.println("index: " + index);
			//System.out.println(file.get(index).toString());
			//System.out.println("++++++++++++++++++++++++++++++++++++++");
			if (Pattern.matches(".*\\s*\\<page\\>.*", file.get(index).toString())) {
				pageFound = true;
			}
			//System.out.println("line matches Page: " + pageFound);
			if (pageFound) {
				StringBuffer sb= new StringBuffer(file.get(index).toString());
				sb.deleteCharAt(0);
				sb.deleteCharAt(sb.length()-1);
			//	System.out.println(sb.toString());
				page.add(sb.toString());
			}
			
			if (Pattern.matches(".*\\s*\\</page\\>.*", file.get(index).toString())) {
				pageFound = false;
				//System.out.println("line matches Page: " + pageFound);
				index++;
				break;
			}
			index++;
		}
	}
	
	public static void printPage() {
		page.forEach( row -> { System.out.println(row); });
	}

	public FileInputStream getFstream() {
		return fstream;
	}

	public void setFstream(FileInputStream fstream) {
		this.fstream = fstream;
	}

	public static List<String> getPage() {
		return page;
	}

	public static void setPage(List<String> page) {
		FileReader.page = page;
	}

	public BufferedReader getBr() {
		return br;
	}

	public void setBr(BufferedReader br) {
		this.br = br;
	}	
	
	public String getTextAsString() {
		return textAsString;
	}

	public void setTextAsString(String textAsString) {
		this.textAsString = textAsString;
	}
}
