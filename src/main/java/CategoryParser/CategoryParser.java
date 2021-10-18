package CategoryParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import FileManager.ClusterPersistance;
import FileManager.FileParser;
import FileManager.FileReader;
import FileManager.TextParser;
import Types.Article;
import Types.Cluster;

public class CategoryParser {

	public static void main(String[] args) throws Exception {
		//check if categories was already clustered
		File file = new File("clusterPersistance.txt");
        if (file.length() == 0) {
        	List<Article> articles = new ArrayList<Article>();
			articles = getArticles(5);
			
			CategoryClusterer.doClustering(articles);
			
			ClusterPersistance.saveClusterToFile(CategoryClusterer.clusters);
        } else {
        	CategoryClusterer.clusters = ClusterPersistance.getClusterFromFile();
        }
		
		StandardAnalyzer analyzer = new StandardAnalyzer();
		Directory index = new RAMDirectory();
		//Directory index = FSDirectory.open(new File("index-dir"));
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		IndexWriter writer = new IndexWriter(index, config);
		
		createIndexes(writer);
		
		while(true) {
			System.out.print("Query: ");
			Scanner sc= new Scanner(System.in); 
			String querystr= sc.nextLine(); 
			
			Query query = new QueryParser("category", analyzer).parse(querystr);
	
			int hitsPerPage = 10;
			IndexReader reader = DirectoryReader.open(index);
			IndexSearcher searcher = new IndexSearcher(reader);
			TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
			searcher.search(query, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
	
			printFoundedResults(hits, searcher);
		
			reader.close();
		}
	}
	
	public static List<Article> getArticles(Integer keyWordsCnt) throws Exception {
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
			
			TextParser.getWordFrequency(articleFound, keyWordsCnt);		
			
			/*if (cnt == 100) break;
			cnt ++;*/
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
	
	private static void addDoc(IndexWriter w, String category, String cluster) throws IOException {
		Document doc = new Document();
		doc.add(new TextField("category", category, Field.Store.YES));
		doc.add(new TextField("cluster", cluster, Field.Store.YES));
		w.addDocument(doc);
	}
	
	public static void createIndexes(IndexWriter writer) throws IOException {
		 CategoryClusterer.clusters.forEach(cluster -> {
			 cluster.getCategories().forEach((category, keyWords) -> {
				 try {
					addDoc(writer, category, cluster.getCentroid().getName());
				} catch (IOException e) {
					e.printStackTrace();
				}
			 });
		 });
		
		writer.close();
	}
	
	public static void printFoundedResults(ScoreDoc[] hits, IndexSearcher searcher) throws IOException {
		System.out.println("Found " + hits.length + " hits.");
		for (int i = 0; i < hits.length; ++i) {
			int docId = hits[i].doc;
			Document d = searcher.doc(docId);
			System.out.println((i + 1) + ". " + d.get("category") + "\t in cluster: " + d.get("cluster"));
			Cluster foundCluster = CategoryClusterer.getClusterByCentroid(d.get("cluster"));
			if (foundCluster != null) {
				System.out.println("Similar categories");
				foundCluster.getCategories().forEach((cat, keyWords) -> {
					System.out.println("\t" + cat);
				});
			}
		}
	}
}
