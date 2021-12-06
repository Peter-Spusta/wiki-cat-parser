package CategoryParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import org.apache.arrow.vector.util.Text;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.shaded.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.hadoop.shaded.org.eclipse.jetty.util.Fields;
import org.apache.log4j.BasicConfigurator;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.input.StreamInputFormat;
import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.launcher.SparkLauncher;
import org.apache.spark.sql.SparkSession;

import FileManager.ClusterPersistance;
import FileManager.FileParser;
import FileManager.FileReader;
import FileManager.TextParser;
import Types.Article;
import Types.Cluster;
import scala.Tuple2;

public class CategoryParser {

	public static void main(String[] args) throws Exception {
		BasicConfigurator.configure();

		String filePath = "C:\\Users\\petos\\OneDrive\\Documents\\FIIT\\vinf\\project\\wikiDumpFiles\\enwiki-latest-pages-articles1.xml-p1p41242";
		
		if (args.length > 0)
			filePath = args[0];
		
		SparkConf sparkConf = new SparkConf().setAppName("Category Parser").setMaster("local");
		 // start a spark context
		JavaSparkContext sc = new JavaSparkContext(sparkConf);
		
		//JavaRDD<List<String>> file = rdd.map(s -> Arrays.asList(s));
		JavaRDD<List<Article>> articles = sc.textFile(filePath).map(s -> Arrays.asList(s))
											.glom().map(f -> getArticles(5, f));
		articles.reduce((a,b) -> CategoryClusterer.doClustering(a,b));
		//List<Cluster> clusters = CategoryClusterer.doClusteringPar(articles.collect());									
		
		//clusters.saveAsTextFile("SavedSparkClusters");

		//ClusterPersistance.saveClusterToFile(CategoryClusterer.clusters);
		StandardAnalyzer analyzer = new StandardAnalyzer();
		//Directory index = new RAMDirectory();
		Directory index = FSDirectory.open(Files.createTempDirectory("index-dir"));
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		IndexWriter writer = new IndexWriter(index, config);
		
		createIndexes(writer, CategoryClusterer.clusters);
		
		while(true) {
			System.out.print("Query: ");
			Scanner scan= new Scanner(System.in); 
			String querystr= scan.nextLine(); 
			
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
		
		//start(null);
	}
	
	public static void start(List<List<String>> wikiFile) throws Exception {
		//check if categories was already clustered
		File file = new File("clusterPersistance.txt");
		
        //if (file.length() == 0) {
        	List<Article> articles = new ArrayList<Article>();
        	if(wikiFile != null)
        		articles = getArticles(5, wikiFile);
        	else 
        		articles = getArticles(5);
			CategoryClusterer.doClustering(articles);
			
		//	ClusterPersistance.saveClusterToFile(CategoryClusterer.clusters);
        //} else {
        //	CategoryClusterer.clusters = ClusterPersistance.getClusterFromFile();
        //}
		
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
			//sc.close();
		}
	}
	
	
	public static List<Article> getArticles(Integer keyWordsCnt) throws Exception {
		FileReader fileReader = new FileReader();
		FileParser fileParser = new FileParser();
		List<Article> articles = new ArrayList<Article>();
		Article articleFound = null;
		
		
		fileReader.readFile("C:\\Users\\petos\\OneDrive\\Documents\\FIIT\\vinf\\project\\wikiDumpFiles\\enwiki-latest-pages-articles1.xml-p1p41242");
		
		int cnt = 0;
		
		while(fileReader.getBr() != null) {
			
			FileReader.readNextPage();
			
			articleFound = fileParser.parsePage(FileReader.getPage());
			
			if (articleFound == null) {
				continue;
			}
			
			articles.add(articleFound);
			
			TextParser.getWordFrequency(articleFound, keyWordsCnt);		
			
			if (cnt == 2700) break;
			cnt ++;
		}
		
		fileReader.closeFile();
		
		addKeywordsToCategories(articles);
		
		return articles;
	}
	
	public static List<Article> getArticles(Integer keyWordsCnt, List<List<String>> file) throws Exception {
		FileReader fileReader = new FileReader();
		FileParser fileParser = new FileParser();
		List<Article> articles = new ArrayList<Article>();
		Article articleFound = null;
		//System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
		//System.out.println(file);
		//fileReader.setTextAsString(file);
		//fileReader.setIt(file.lines().iterator());
		
		FileReader.index = 0;
		
		int cnt = 0;
		
		while(FileReader.index < file.size()) {
			
			FileReader.readNextPage(file);
			
			articleFound = fileParser.parsePage(FileReader.getPage());
			
			if (articleFound == null) {
				continue;
			}
			
			articles.add(articleFound);
	
			TextParser.getWordFrequency(articleFound, keyWordsCnt);		
			
			//if (cnt == 100) break;
			//cnt ++;
		}
		
		//System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
		//articles.get(0).printArticle();
		
		addKeywordsToCategories(articles);
		return articles;
	}
	
	public static void addKeywordsToCategories(List<Article> articles) {
		articles.forEach(article -> {
			article.getCategories().forEach(category -> {
				category.addTitle(article.getTitle());
				category.setKeyWords(article.getKeyWords());
			});
		});
	};
	

	private static void addDoc(IndexWriter w, String category, String cluster, TreeMap<String, Integer> keywords, ArrayList<String> titles) throws IOException {
		Document doc = new Document();
		keywords.forEach((keyword, count) -> {
			doc.add(new StringField("keyword", keyword, Field.Store.YES));
		});
		titles.forEach(title -> {
			doc.add(new StringField("title", title, Field.Store.YES));
		});
		doc.add(new TextField("category", category, Field.Store.YES));
		doc.add(new TextField("cluster", cluster, Field.Store.YES));
		w.addDocument(doc);
	}
	
	public static void createIndexes(IndexWriter writer) throws IOException {
		List<Cluster>clust = CategoryClusterer.clusters;
		CategoryClusterer.clusters.forEach(cluster -> {
			 cluster.getCategories().forEach((categoryName, cat) -> {
				 try {
					addDoc(writer, categoryName, cluster.getCentroid().getName(), (TreeMap<String, Integer>)cat.getKeyWords(), cat.getTitles());
				} catch (IOException e) {
					e.printStackTrace();
				}
			 });
		 });
		
		 writer.commit();
		writer.close();
	}
	
	public static void createIndexes(IndexWriter writer, List<Cluster> clusters) throws IOException {
		 clusters.forEach(c -> {

				 c.getCategories().forEach((categoryName, cat) -> {
					 try {
						addDoc(writer, categoryName, c.getCentroid().getName(), (TreeMap<String, Integer>)cat.getKeyWords(), cat.getTitles());
					} catch (IOException e) {
						e.printStackTrace();
					}
				 });
	
		 });
		writer.close();
	}
	
	public static void createIndexes2(IndexWriter writer, List<List<Cluster>> clusters) throws IOException {
		 clusters.forEach(c -> {
			 c.forEach(cluster -> {
				 cluster.getCategories().forEach((categoryName, cat) -> {
					 try {
						addDoc(writer, categoryName, cluster.getCentroid().getName(), (TreeMap<String, Integer>)cat.getKeyWords(), cat.getTitles());
					} catch (IOException e) {
						e.printStackTrace();
					}
				 });
			 });
		 });
		writer.close();
	}
	
	public static void printFoundedResults(ScoreDoc[] hits, IndexSearcher searcher) throws IOException {
		System.out.println("Found " + hits.length + " hits.");
		for (int i = 0; i < hits.length; ++i) {
			int docId = hits[i].doc;
			boolean firstTitle = true;
			boolean firstKeyword = true;
			Document d = searcher.doc(docId);
			System.out.println((i + 1) + ". " + d.get("category") + "\t in cluster: " + d.get("cluster"));
			Iterator<IndexableField> it = d.getFields().iterator();
			
			 while ( it.hasNext() )
			  {
			    IndexableField field = it.next();
			    String fieldName = field.name();
			    if ( fieldName.equals("keyword"))
			    {
			    	if (firstKeyword) {
			    		System.out.print("\tKeywords: ");
			    		firstKeyword = false;
			    	}
			    	System.out.print(field.stringValue() + ", ");
			    	
			    } else if (fieldName.equals("title")) {
			    	if (firstTitle) {
			    		System.out.print("\n\tTitles: ");
			    		firstTitle = false;
			    	}
			    	System.out.print(field.stringValue() + ", ");
			    	
			    }
			  }
			
			Cluster foundCluster = CategoryClusterer.getClusterByCentroid(d.get("cluster"));
			//printSimilarCategries(foundCluster);
			System.out.println("\n");
		}
	}
	
	public static void printSimilarCategries(Cluster cluster) {
		if (cluster != null) {
			System.out.println("Similar categories");
			cluster.getCategories().forEach((cat, keyWords) -> {
				System.out.println("\t" + cat);
			});
		}
	}
}
