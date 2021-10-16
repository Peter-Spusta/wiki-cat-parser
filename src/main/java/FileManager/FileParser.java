package FileManager;

import java.util.ArrayList;
import java.util.List;

import java.util.regex.Pattern;

import Types.Article;
import Types.Category;

public class FileParser {
	
	public Article parsePage(List<String> page) throws Exception {
		
		Article article = new Article();
		String title = null;
		List<String> text = null;
		List<Category> categories = new ArrayList<Category>();
		Boolean readingText = false;
		
		String titleFound = null;
		String categoryFound = null;
		
		for(String line : page) {
			
			titleFound = parseTitle(line);
			if (titleFound != null) {
				title = titleFound;
			}
			
			if (textStarted(line)) {
				readingText = true;
				text = new ArrayList<String>();
			}
			
			if (readingText) {
				text.add(line);
			}
			
			if (textEnded(line)) {
				readingText = false;
			}
			
			categoryFound = parseCategory(line);
			if (categoryFound != null) {
				categories.add(new Category(categoryFound, null));
			}
			
		};
		
		if (title != null) {
			article.setTitle(title);
		}
		
		if (text != null) {
			article.setText(TextParser.removeStopWords(TextParser.removeUnusableText(text)));
		}
		
		if (!categories.isEmpty()) {
			article.setCategories(categories);
		}
		
		if (article.isEmpty()) {
			return null;
		}
		
		return article;
	}
	
	public String parseTitle(String line) {
		String title = null;
		
		if (Pattern.matches("\\s*\\<title\\>.*<\\/title>", line)) {
			title = line;
		}
		
		if (title != null) {
			title = title.split(">")[1];
			title = title.split("<")[0];
		}
		
		return title;
	}
	
	public String parseCategory(String line) {
		String category = null;
		
		if (Pattern.matches("^\\[\\[Category:.+\\]\\].*", line)) {
			category = line;
		}
		
		if (category != null) {
			category = category.split(":")[1];
			category = category.split("]")[0];
		}
		
		return category;
	}
	
	public boolean textStarted(String line) {
		if (Pattern.matches("^.*\\<text\\s+.*\\>.*", line)) {
			return true;
		}
		return false;
	}
	
	public boolean textEnded(String line) {
		if (Pattern.matches("^.*\\<\\/text\\>", line)) {
			return true;
		}
		return false;
	}
	
	public static void printText(List<String> text) {
		text.forEach( line -> { System.out.println(line); });
	}
}
