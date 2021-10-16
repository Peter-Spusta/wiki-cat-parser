package FileManager;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class FileReader {
	
	FileInputStream fstream;
	static List<String> page = new ArrayList<String>();
	static BufferedReader br;
	
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
}
