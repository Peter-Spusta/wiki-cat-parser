package Types;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class Cluster implements Serializable {
	
	 /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	Category centroid;
	 Map<String, Category> categories = new TreeMap<String, Category>();
	
	public Category getCentroid() {
		return centroid;
	}
	public void setCentroid(String name, Map<String, Integer> keyWords) {
		this.centroid = new Category(name, keyWords);
	}
	public void setCentroid(Category centroid) {
		this.centroid = centroid;
	}
	public Map<String, Category> getCategories() {
		return categories;
	}
	public void setCategories(Map<String, Category> categories) {
		this.categories = categories;
	}
	
}
