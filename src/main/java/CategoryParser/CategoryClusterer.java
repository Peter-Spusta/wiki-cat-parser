package CategoryParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import Types.Article;
import Types.Category;
import Types.Cluster;

public class CategoryClusterer {
	
	static Map<String, Category> categories = new TreeMap<String, Category>();
	
	//probably isnt needed
	static Map<String, Integer> allKeyWords = new TreeMap<String, Integer>();
	static List<Cluster> clusters = new ArrayList<Cluster>();
	
	public static void doClusteringSpark(Article a) {
		a.getCategories().forEach(c -> {
			c.addTitle(a.getTitle());
			c.setKeyWords(a.getKeyWords());
			findClosestCluster(c.getName(), c);
		});
	}
	
	public static List<Article> doClustering(List<Article> articles, List<Article> articlesb) {
		
		if (articles != null && articlesb != null)
			articles.addAll(articlesb);
		if (articles == null && articlesb != null)
			articles = articlesb;
		if (articles == null && articlesb == null)
			return null;
		fillCategories(articles);
		
		setCentroids((int) Math.sqrt(categories.size()), articles);
		
		createClusters();
		
		for (int i = 0; i < 3; i++) {
			System.out.println(i+"0%");
			recalculateCluster();
		}
		
		return null;
	}
	
	public static List<Cluster> doClustering(List<Article> articles) {
		fillCategories(articles);
		
		setCentroids((int) Math.sqrt(categories.size()), articles);
		
		createClusters();
		
		for (int i = 0; i < 3; i++) {
			System.out.println(i+"0%");
			recalculateCluster();
		}
		
		return clusters;
	}
	
	public static List<Cluster> doClusteringPar(List<List<Article>> articles) {
		List<Article> art = new ArrayList<Article>();
		for(List<Article> a : articles) {
			art.addAll(a);
		}
		
		
		fillCategories(art);
		
		setCentroids((int) Math.sqrt(categories.size()), art);
		
		createClusters();
		
		for (int i = 0; i < 3; i++) {
			System.out.println(i+"0%");
			recalculateCluster();
		}
		
		return clusters;
	}
	
	public static void recalculateCluster() {
		
		List<Cluster> emptyClusters = new ArrayList<Cluster>();
		for(Cluster cluster : clusters) {
			
			if (cluster.getCategories().size() <= 1) {
				emptyClusters.add(cluster);
			}
				
			Integer bestDistance = 0;
			Integer distanceMean = 0;
			Category newCentroid = null;
				
			for(Map.Entry<String, Category> cat1 : cluster.getCategories().entrySet()) {
				
				for(Map.Entry<String, Category> cat2 : cluster.getCategories().entrySet()) {
					if (cat1.getKey() != cat2.getKey()) {
						distanceMean += CalculateCategoryDistance((Map<String, Integer>) cat1.getValue().getKeyWords(), (Map<String, Integer>) cat2.getValue().getKeyWords());
					} 
				};
				
				if (distanceMean >= bestDistance ) {
					distanceMean = distanceMean/cluster.getCategories().size();
					newCentroid = new Category(cat1.getValue());
				}
			}
			
			if (newCentroid == null) {
				cluster.setCentroid(cluster.getCentroid());
			} else {
				cluster.setCentroid(newCentroid);
			}
			
			cluster.getCategories().clear();
		}
		
		emptyClusters.forEach(cluster -> {
			clusters.remove(cluster);
		});
		createClusters();
	}
	
	//set k centroids
	public static void setCentroids(int k, List<Article> articles) {
		for (int i = 0; i < k; i++) {
			List<Category> catList = articles.get((int) (Math.random() * (articles.size()))).getCategories();
			
			Category cat = catList.get((int) (Math.random() * catList.size()));
			Cluster cluster = new Cluster();
			
			cluster.setCentroid(cat);
			clusters.add(cluster);
		}
	}
	
	public static void createClusters() {
		categories.forEach((catName, cat) -> {
			
			findClosestCluster(catName, cat);
		});
	}
	
	public static void findClosestCluster(String category, Category cat) {
		Cluster closest = null;
		int match = 0;
		
		for (Cluster cluster : clusters) {	
				
			int distance = CalculateCategoryDistance((Map<String, Integer>)cluster.getCentroid().getKeyWords(), cat.getKeyWords());
			
			if (distance > match) {
				 match = distance;
				 closest = cluster;
			}
		}
		
		//there isnt any similar centroid
		if (closest == null) {
			Cluster newCluster = new Cluster();
			newCluster.setCentroid(cat);
			clusters.add(newCluster);
		} else {
			closest.getCategories().put(cat.getName(), cat);
		}
	}
	
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
					for (Map.Entry<String, Integer> word : ((TreeMap<String, Integer>)categories.get(cat.getName()).getKeyWords()).entrySet()) {
						if (keyWords.containsKey(word.getKey())) {
							keyWords.put(word.getKey(), word.getValue()+keyWords.get(word.getKey()));
			        	} else {
			        		keyWords.put(word.getKey(), word.getValue());
			        	}
					}
					cat.setKeyWords(keyWords);
					ArrayList<String> titles = categories.get(cat.getName()).getTitles();
					titles.add(article.getTitle());
					cat.setTitles(titles);
					//keyWords.putAll(((TreeMap<String, Integer>)categories.get(cat.getName())));
				}
				categories.put(cat.getName(),cat);
			});
		}
	}
	
	public static int CalculateCategoryDistance(Map<String, Integer> c1Kw, Map<String, Integer> c2Kw) {
		int match = 0;
		
		for (Map.Entry<String, Integer> word : c1Kw.entrySet()) {
			if (c2Kw.containsKey(word.getKey( ))) {
				match += word.getValue();
			}
		}

		
		return match;
	}
	
	
	public static Cluster getClusterByCentroid(String centroid) {
		for(Cluster cluster : clusters) {
			if (cluster.getCentroid().getName().equals(centroid)) {
				return cluster;
			}
		}
		return null;
	}
	
	static void printCluster() {
		clusters.forEach(cluster -> {
			System.out.println(cluster.getCentroid().getName());
			System.out.println(cluster.getCentroid().getKeyWords());
			System.out.println();
		});
	}

	public static Map<String, Category> getCategories() {
		return categories;
	}

	public static void setCategories(Map<String, Category> categories) {
		CategoryClusterer.categories = categories;
	}

	public static Map<String, Integer> getAllKeyWords() {
		return allKeyWords;
	}

	public static void setAllKeyWords(Map<String, Integer> allKeyWords) {
		CategoryClusterer.allKeyWords = allKeyWords;
	}
	
	
}
