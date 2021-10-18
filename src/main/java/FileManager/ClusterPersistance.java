package FileManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import Types.Cluster;

public class ClusterPersistance {

	public static void saveClusterToFile(List<Cluster> clusters) throws IOException {
		FileOutputStream f = new FileOutputStream(new File("clusterPersistance.txt"));
		ObjectOutputStream o = new ObjectOutputStream(f);

		// Write objects to file
		clusters.forEach(cluster -> {
			try {
				o.writeObject(cluster);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		

		o.close();
		f.close();
	}
	
	public static List<Cluster> getClusterFromFile() throws IOException, ClassNotFoundException {
		FileInputStream f = new FileInputStream(new File("clusterPersistance.txt"));
		ObjectInputStream o = new ObjectInputStream(f);

		List<Cluster> clusters = new ArrayList<Cluster>();
		// Read objects
		while(o.equals(o)) {
			try {
				clusters.add((Cluster) o.readObject());
			} catch (Exception e) {
				break;
			}
		}

		o.close();
		f.close();
		
		return clusters;
	}
}
