package Types;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Article implements Serializable {
	
	private static final long serialVersionUID = 1L;
	String title = null;
	List<String> text = null;
	List<Category> categories = null;
	Map<String, Integer> keyWords = null;
	
	public Map<String, Integer> getKeyWords() {
		return keyWords;
	}

	public void setKeyWords(Map<String, Integer> keyWords) {
		this.keyWords = keyWords;
	}

	public boolean isEmpty() {
		if (this.title == null || this.text == null || this.categories == null ||
			this.categories.isEmpty()) {
			return true;
		}
		
		return false;
	}
	
	public void printCategories() {
		categories.forEach(category -> {
			System.out.print(category.getName() + ", ");
		});
		System.out.println();
	}
	
	public void printArticle() {
		System.out.println(getTitle());
		System.out.println(getKeyWords());
		printCategories();
		System.out.println();
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public List<String> getText() {
		return text;
	}
	public void setText(List<String> text) {
		this.text = text;
	}
	public List<Category> getCategories() {
		return categories;
	}
	public void setCategories(List<Category> categories) {
		this.categories = categories;
	}	
}
