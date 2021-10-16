package CategoryParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import FileManager.FileParser;
import FileManager.FileReader;
import FileManager.TextParser;
import Types.Article;

public class CategoryParser {

	public static void main(String[] args) throws Exception {
		List<Article> articles = new ArrayList<Article>();
		articles = getArticles();
		
		articles.forEach(article -> {
			article.printArticle();
		});
		
		CategoryClusterer.fillCategories(articles);
		
		Map<String, Object> categories = CategoryClusterer.getCategories();
		System.out.println(categories);
		Map<String, Integer> allKeyWords = CategoryClusterer.getAllKeyWords();
		System.out.println(allKeyWords);
	}
	
	public static List<Article> getArticles() throws Exception {
		FileReader fileReader = new FileReader();
		FileParser fileParser = new FileParser();
		List<Article> articles = new ArrayList<Article>();
		Article articleFound = null;
		
		fileReader.readFile("C:\\Users\\petos\\OneDrive\\Documents\\FIIT\\Vyhladavanie informacii\\project\\wikiDumpFiles\\enwiki-latest-pages-articles1.xml-p1p41242");
		
		int cnt = 0;
		
		while(fileReader.getBr() != null) {
			
			FileReader.readNextPage();
			
			articleFound = fileParser.parsePage(FileReader.getPage());
			
			if (articleFound == null) {
				continue;
			}
			
			articles.add(articleFound);
			
			TextParser.getWordFrequency(articleFound);		
			
			if (cnt == 100) break;
			cnt ++;
		}
		
		fileReader.closeFile();
		
		addKeywordsToCategories(articles);
		
		return articles;
	}
	
	public static void addKeywordsToCategories(List<Article> articles) {
		articles.forEach(article -> {
			article.getCategories().forEach(category -> {
				category.setKeyWords(article.getKeyWords());
			});
		});
	};

}
