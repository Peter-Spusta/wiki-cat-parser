package FileManager;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.AttributeFactory;
import org.apache.lucene.util.Version;

import Types.Article;

import org.apache.commons.text.similarity.JaccardSimilarity;

public class TextParser {

	public static List<String> removeStopWords(List<String> text) throws Exception {
		List<String> cleanedText = new ArrayList<String>();

		for(String line : text) {
			List<String> tokens = new ArrayList<String>();
			AttributeFactory factory = AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY;
			Tokenizer tokenizer = new StandardTokenizer(factory);
			tokenizer.setReader(new StringReader(line));
			CharArraySet stopWords = EnglishAnalyzer.getDefaultStopSet();
		    TokenStream tokenStream = new StopFilter(tokenizer, stopWords);
		    CharTermAttribute charTermAttribute = tokenizer.addAttribute(CharTermAttribute.class);
		    try {
		    	tokenStream.reset();
		        while (tokenStream.incrementToken()) {
		            String term = charTermAttribute.toString();
		            
		            tokens.add(term);
		        }
		        tokenStream.end();
		        tokenStream.close();
	
		        tokenizer.close();  
			} catch (IOException e) {
				e.printStackTrace();
			}
	    cleanedText.addAll(tokens);
		}
		return cleanedText;	
	}
	
	public static List<String> removeUnusableText(List<String> text) {
		List<String> cleanedText = new ArrayList<String>();
		for(String line : text) {
			line = line.replaceAll("\\{\\{.*\\}\\}", "");
			line = line.replaceAll("&.+?;", "");
			line = line.replaceAll("^.{0,1}\\|.*", "");
			line = line.replace("'s", "");
			line = line.replace("\\* \\[.*?]", "");
			
			if (!line.isBlank()) {
				cleanedText.add(line);
			}
			
			if (Pattern.matches(".*==.See.also.==.*", line)) {
				break;
			}
		};
		
		return cleanedText;
	}
	
	//get word frequency and remove some stop words and solve word in plural vs singular
	public static void getWordFrequency(Article article) {
        Map<String,Integer> mp = new TreeMap<>();
        JaccardSimilarity jaccardSimilarity = new JaccardSimilarity();
        
        for (String word : article.getText())
        {
        	word = wordToLowerCase(word);
        	
        	//take care of plural words
        	String wordWithoutS = "";
        	boolean hasS = false;
        	if (word.charAt(word.length() - 1) == 's') {
        		hasS = true;
        		wordWithoutS = word.substring(0, word.length() - 1);
        	}
        	
        	if (word.equals("would") || word.equals("while") || word.equals("which") || 
        		word.equals("were") || word.equals("what") || word.equals("where") || 
        		word.equals("the")|| word.equals("has") || word.equals("had") ||
        		word.equals("have") || word.equals("in") || word.equals("among") ||
        		word.equals("other") || word.equals("also") || word.equals("during") || 
        		word.equals("used") || word.equals("uses") || word.equals("from") ||
        		word.equals("name") || word.equals("may") || word.equals("ref") ||
        		word.equals("he") || word.equals("his") || word.equals("she") ||
        		word.equals("an") || word.equals("new") || word.equals("more") || 
        		word.equals("because") || word.equals("often") || word.equals("who") || 
        		word.equals("since") || word.equals("best") || word.equals("till") ||
        		word.equals("untill") || word.equals("can") || word.equals("all") ||
        		word.equals("than") || word.equals("been") || word.equals("good") ||
        		word.equals("bad") || word.equals("her") || word.equals("after") ||
        		word.equals("before") || word.equals("its") || word.equals("when") ||
        		word.equals("it") || word.equals("most") || word.equals("use") ||
        		word.equals("only") || word.equals("first") || word.equals("second") ||
        		word.equals("third") || word.equals("one") || word.equals("three") ||
        		word.equals("between") || word.equals("either") || word.equals("non") ||
        		word.equals("thumb") || word.equals("them") || word.equals("you") ||
        		word.equals("our") || word.equals("year") ||
        		word.length() <= 1) {
        		continue;
        	}
        	
        	//remove too similar words to title
        	if (jaccardSimilarity.apply(wordToLowerCase(article.getTitle()), word) >= 0.75) {
        		continue;
        	}
        	
        	if (hasS && mp.containsKey(wordWithoutS)) {
        		 mp.put(wordWithoutS, mp.get(wordWithoutS)+1);
        	} else if (hasS && !mp.containsKey(wordWithoutS)) {
        		mp.put(word,1);
        	} else if (!hasS && mp.containsKey(word+'s')) {
        		mp.put(word, mp.get(word+'s')+1);
        	} else if (!hasS && mp.containsKey(word)) {
        		mp.put(word, mp.get(word)+1);
        	} else if (!hasS && !mp.containsKey(word)) {
        		mp.put(word,1);
        	}
        }
      
        article.setKeyWords(getKLargest(mp, 10));
	}
	
	public static String wordToLowerCase(String word) {
		char c[] = word.toCharArray();
		c[0] = Character.toLowerCase(c[0]);
		return new String(c);
	} 
	
	public static Map<String, Integer> getKLargest(Map<String,Integer> mp, int k) {
		LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<>();
		 
		mp.entrySet()
		    .stream()
		    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
		    .forEachOrdered(x -> sortedMap.put(x.getKey(), x.getValue()));
		
		mp = sortedMap;
		
		mp = mp.entrySet().stream()
			    .limit(k)
			    .collect(TreeMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), Map::putAll);
		
		return mp;
	}
}
