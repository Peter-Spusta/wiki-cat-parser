package Types;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

public class Category implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String name = null;
	Map<String, Integer> keyWords = null;
	ArrayList<String> titles = new ArrayList<String>();
	
	public Category(String name, Map<String, Integer> keyWords) {
		this.name = name;
		this.keyWords = keyWords;
	}
	
	public Category(Category cat) {
		this.name = cat.getName();
		this.keyWords = cat.getKeyWords();
	}
	
	public ArrayList<String> getTitles() {
		return titles;
	}

	public void setTitles(ArrayList<String> titles) {
		this.titles = titles;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Map<String, Integer> getKeyWords() {
		return keyWords;
	}
	public void setKeyWords(Map<String, Integer> keyWords) {
		this.keyWords = keyWords;
	}
	public void addTitle(String title) {
		this.titles.add(title);
	}
}
