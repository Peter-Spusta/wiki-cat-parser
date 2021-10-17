package Types;
import java.util.Map;

public class Category {
	String name = null;
	Map<String, Integer> keyWords = null;
	
	public Category(String name, Map<String, Integer> keyWords) {
		this.name = name;
		this.keyWords = keyWords;
	}
	
	public Category(Category cat) {
		this.name = cat.getName();
		this.keyWords = cat.getKeyWords();
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
}
