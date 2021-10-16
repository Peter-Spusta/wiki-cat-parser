package CategoryParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import Types.Article;
import Types.Category;

public class CategoryClusterer {
	
	static Map<String, Object> categories = new TreeMap<String, Object>();
	static Map<String, Integer> allKeyWords = new TreeMap<String, Integer>();
	
	public static void createClusters(List<Article> articles) {
		fillCategories(articles);
		
		categories.forEach((c1, c1Kw) -> {
			categories.forEach((c2,c2Kw) -> {
				System.out.println(c1 + " ?= " + c2);
				CalculateCategoryDistance((Map<String, Integer>)c1Kw, (Map<String, Integer>)c2Kw);
			});
		});
	}
	
	@SuppressWarnings("unchecked")
	public static void fillCategories(List<Article> articles) {
		//create list of all keywords
		for (Article article : articles) {
			article.getKeyWords().forEach((word, cnt) ->{
				allKeyWords.put(word,0);
			});	
		}
		
		for (Article article : articles) {
			article.getCategories().forEach(cat -> {
				
				Map<String, Integer> keyWords = new TreeMap<String, Integer>();
				//keyWords.putAll(allKeyWords);
				
				keyWords.putAll(article.getKeyWords());
				
				
				if (categories.containsKey(cat.getName())) {
					
					//ak sa druhy raz nasla rovnaka trieda tak sa vahy rovnakych keyWords tychto dvoch vyskytov triedy zrataju
					for (Map.Entry<String, Integer> word : ((TreeMap<String, Integer>)categories.get(cat.getName())).entrySet()) {
						if (keyWords.containsKey(word.getKey())) {
							keyWords.put(word.getKey(), word.getValue()+keyWords.get(word.getKey()));
			        	} else {
			        		keyWords.put(word.getKey(), word.getValue());
			        	}
					}
					//keyWords.putAll(((TreeMap<String, Integer>)categories.get(cat.getName())));
				}
				categories.put(cat.getName(),keyWords);
			});
		}
	}
	
	//pokus s bitsetmi
	/*public static BitSet createBitSetFromKeyWords(BitSet actualBs, Map<String, Integer> keyWords) {
		BitSet bs = new BitSet();
		int index = 0;
		
		if (actualBs != null) {
			keyWords.forEach((word, num) -> {
				if (num != 0) {
					actualBs.set(index, true);
				} else {
					actualBs.set(index, false);
				}
			});
			return actualBs;
		} else {
			keyWords.forEach((word, num) -> {
				if (num != 0) {
					bs.set(index);
				} else {
					bs.set(index, false);
				}
			});
			return bs;
		}
	}*/
	
	public static int CalculateCategoryDistance(Map<String, Integer> c1Kw, Map<String, Integer> c2Kw) {
		int match = 0;
		
		for (Map.Entry<String, Integer> word : c1Kw.entrySet()) {
			if (c2Kw.containsKey(word.getKey())) {
				match++;
			}
		}

		if (match != 0) {
			System.out.println("Look at least something is working");
		}
		return match;
	}

	public static Map<String, Object> getCategories() {
		return categories;
	}

	public static void setCategories(Map<String, Object> categories) {
		CategoryClusterer.categories = categories;
	}

	public static Map<String, Integer> getAllKeyWords() {
		return allKeyWords;
	}

	public static void setAllKeyWords(Map<String, Integer> allKeyWords) {
		CategoryClusterer.allKeyWords = allKeyWords;
	}
	
	
}
