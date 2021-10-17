package Types;

import java.util.Map;
import java.util.TreeMap;

public class Cluster {
	
	 Category centroid;
	 Map<String, Object> categories = new TreeMap<String, Object>();
	
	public Category getCentroid() {
		return centroid;
	}
	public void setCentroid(String name, Map<String, Integer> keyWords) {
		this.centroid = new Category(name, keyWords);
	}
	public void setCentroid(Category centroid) {
		this.centroid = centroid;
	}
	public Map<String, Object> getCategories() {
		return categories;
	}
	public void setCategories(Map<String, Object> categories) {
		this.categories = categories;
	}
	
}
