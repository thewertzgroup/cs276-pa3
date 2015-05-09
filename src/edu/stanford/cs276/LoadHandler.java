package edu.stanford.cs276;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import edu.stanford.cs276.util.Pair;

/**
 * This class is used to 1) load training data from files; 2) build idf from data collections in PA1.
 */
public class LoadHandler {
	public static String specialTerm = "%&%#"; // special term will be needed later in AScorer for laplace smoothing for terms not in this trainihg corpus
	
	public static Map<Query,Map<String, Document>> loadTrainData(String feature_file_name) throws Exception {
		File feature_file = new File(feature_file_name);
		if (!feature_file.exists() ) {
			System.err.println("Invalid feature file name: " + feature_file_name);
			return null;
		}
		
		BufferedReader reader = new BufferedReader(new FileReader(feature_file));
		String line = null, url= null, anchor_text = null;
		Query query = null;
		
		/* Feature dictionary: Query -> (url -> Document)  */
		Map<Query,Map<String, Document>> queryDict =  new HashMap<Query,Map<String, Document>>();
		
		while ((line = reader.readLine()) != null) {
			String[] tokens = line.split(":", 2);
			String key = tokens[0].trim();
			String value = tokens[1].trim();

			if (key.equals("query")) {
				query = new Query(value);
				queryDict.put(query, new HashMap<String, Document>());
			} 
			else if (key.equals("url")) {
				url = value;
				queryDict.get(query).put(url, new Document(url));
			} 
			else if (key.equals("title")) {
				queryDict.get(query).get(url).title = new String(value);
			}
			else if (key.equals("header")) {
				if (queryDict.get(query).get(url).headers == null)
					queryDict.get(query).get(url).headers =  new ArrayList<String>();
				queryDict.get(query).get(url).headers.add(value);
			}
			else if (key.equals("body_hits")) {
				if (queryDict.get(query).get(url).body_hits == null)
					queryDict.get(query).get(url).body_hits = new HashMap<String, List<Integer>>();
				String[] temp = value.split(" ", 2);
				String term = temp[0].trim();
				List<Integer> positions_int;
				
				if (!queryDict.get(query).get(url).body_hits.containsKey(term)) {
					positions_int = new ArrayList<Integer>();
					queryDict.get(query).get(url).body_hits.put(term, positions_int);
				} else
					positions_int = queryDict.get(query).get(url).body_hits.get(term);
				
				String[] positions = temp[1].trim().split(" ");
				for (String position : positions)
					positions_int.add(Integer.parseInt(position));
				
			} 
			else if (key.equals("body_length"))
				queryDict.get(query).get(url).body_length = Integer.parseInt(value);
			else if (key.equals("pagerank"))
				queryDict.get(query).get(url).page_rank = Integer.parseInt(value);
			else if (key.equals("anchor_text")) {
				anchor_text = value;
				if (queryDict.get(query).get(url).anchors == null)
					queryDict.get(query).get(url).anchors = new HashMap<String, Integer>();
			}
			else if (key.equals("stanford_anchor_count"))
				queryDict.get(query).get(url).anchors.put(anchor_text, Integer.parseInt(value));      
		}

		reader.close();
		
		return queryDict;
	}
	
	// Unserializes from file
	public static Map<String,Double> loadDFs(String idfFile) {
		Map<String,Double> termDocCount = null;
		try {
			FileInputStream fis = new FileInputStream(idfFile);
			ObjectInputStream ois = new ObjectInputStream(fis);
			termDocCount = (HashMap<String,Double>) ois.readObject();
			ois.close();
			fis.close();
		}
		catch(IOException | ClassNotFoundException ioe) {
			ioe.printStackTrace();
			return null;
		}
		return termDocCount;
	}
	
	// Build document frequencies and then serializes to file
	public static Map<String,Double> buildDFs(String dataDir, String idfFile) throws IOException
	{
		
		// Get root directory
		String root = dataDir;
		File rootdir = new File(root);
		if (!rootdir.exists() || !rootdir.isDirectory()) {
			System.err.println("Invalid data directory: " + root);
			return null;
		}
		
		File[] dirlist = rootdir.listFiles();

		int totalDocCount = 0;
		
		// Count number of documents in which each term appears
		Map<String,Double> termDocCount = new HashMap<String,Double>();
		
		/*
		 * @//TODO : Your code here -- consult PA1 (will be a simplified version)
		 */
		// added by Maha		
		Map<String,Set<Integer>> termDocSet = new HashMap<String,Set<Integer>>();
		/* For each block */
		for (File block : dirlist) {			
			File blockDir = new File(root, block.getName());
			File[] filelist = blockDir.listFiles();						
			/* For each file */			
			for (File file : filelist) {				
				String fileName = block.getName() + "/" + file.getName();
				totalDocCount++;
															
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line;
				while ((line = reader.readLine()) != null) {
					String[] tokens = line.trim().split("\\s+");
					for (String token : tokens) {
						
						if(!termDocSet.containsKey(token)) // the term is new
						{ 				
							Set<Integer> docSet = new HashSet<Integer>();		
							docSet.add(totalDocCount-1); 
							termDocSet.put(token, docSet); 							
						}
						else //the term appeared before as the term can appear many times						
							termDocSet.get(token).add(totalDocCount-1); 	// will not add duplicate values																		 
						 						
					}
				}
				reader.close();
			}
		}
		
		
		if(!termDocSet.containsKey(specialTerm)) // the term is new
		{			 
			Set<Integer> docSet = new HashSet<Integer>();				
			termDocSet.put(specialTerm, docSet); 
			System.out.println("success");
		}
		System.out.println(totalDocCount);
		
		//NplusOne = totalDocCount + 1; // will be needed later in AScorer for laplace smoothing for terms not in this trainihg corpus  
		
		// from termDocSet to termDocCount
		for (String term : termDocSet.keySet()) {
			double df = (double)termDocSet.get(term).size(); 					 
			termDocCount.put(term, df); // just temporaily put the document frequency			
		}
		// Make idf using df
		for (String term : termDocCount.keySet()) {
			/*
			 * @//TODO : Your code here
			 */
			double idf = Math.log((double)(totalDocCount+1)/(termDocCount.get(term)+1));// add 1 smoothing: laplace 		 
			termDocCount.put(term, idf);
			
		}
		
		
		
		
		// Save to file
		try {
			FileOutputStream fos = new FileOutputStream(idfFile);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(termDocCount);
			oos.close();
			fos.close();
		}

		catch(IOException ioe) {
			ioe.printStackTrace();
		}

		return termDocCount;
	}

}
