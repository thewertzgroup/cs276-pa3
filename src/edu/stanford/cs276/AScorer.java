package edu.stanford.cs276;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An abstract class for a scorer. Need to be extended by each specific implementation of scorers.
 */
public abstract class AScorer {
	
	public static String IDF_MAX = "###IDF_MAX###";

	Map<String,Double> idfs; // Map: term -> idf
	
    // Various types of term frequencies that you will need
	String[] TFTYPES = {"url","title","body","header","anchor"};
	
	public AScorer(Map<String,Double> idfs) {
		this.idfs = idfs;
	}
	
	// Score each document for each query.
	public abstract double getSimScore(Document d, Query q);
	
	// Handle the query vector
	public Map<String,Double> getQueryFreqs(Query q) {
		Map<String,Double> tfQuery = newVector(q); // queryWord -> term frequency
		
		/*
		 * @//TODO : Your code here
		 */
		for (String term : q.queryWords)
		{
			tfQuery.put(term,  tfQuery.get(term) + 1.0);
		}
		
		return tfQuery;
	}
	
	
	////////////// Initialization/Parsing Methods ///////////////
	
	/*
	 * @//TODO : Your code here
	 */

    /////////////////////////////////////////////////////////////
	
	
	/*/
	 * Creates the various kinds of term frequencies (url, title, body, header, and anchor)
	 * You can override this if you'd like, but it's likely that your concrete classes will share this implementation.
	 */
	public Map<String,Map<String, Double>> getDocTermFreqs(Document d, Query q) {
		// Map from tf type -> queryWord -> score
		Map<String,Map<String, Double>> tfs = new HashMap<String,Map<String, Double>>();
		
		////////////////////Initialization/////////////////////
		
		/*
		 * @//TODO : Your code here
		 */
		// "url","title","body","header","anchor"
		
		tfs.put("url", getTFTypeVector(q, d.url.split("[^a-zA-Z0-9']")));
		tfs.put("title", getTFTypeVector(q, d.title.split("\\s+")));
		
		Map<String, Double> termFreq = newVector(q);
		for (String term : d.body_hits.keySet())
		{
			if (q.queryWords.contains(term))
			{
				termFreq.put(term, (double)d.body_hits.get(term).size());
			}
		}
		tfs.put("body", termFreq);
		
		ArrayList<String> headers = new ArrayList<>();
		if (null != d.headers)
		{
			for (String header : d.headers)
			{
				headers.addAll(new ArrayList<String>(Arrays.asList(header.split("\\s+"))));
			}
		}
		String[] headerArray = new String[headers.size()];
		tfs.put("header", getTFTypeVector(q, headers.toArray(headerArray)));
		
		termFreq = newVector(q);
		if (null != d.anchors)
		{
			for (String anchor : d.anchors.keySet())
			{
				termFreq = mergeVectors(q, termFreq, getTFTypeVector(q, anchor.split("\\s+"), (double)d.anchors.get(anchor)));
			}
		}
		tfs.put("anchor", termFreq);
		
	    ////////////////////////////////////////////////////////
		
		// Loop through query terms and increase relevant tfs. Note: you should do this to each type of term frequencies.
		for (String queryWord : q.queryWords) {
			/*
			 * @//TODO : Your code here
			 */
			
		}
		return tfs;
	}
	
	
	private Map<String, Double> newVector(Query q) 
	{
		Map<String, Double> vector = new HashMap<>();
		
		for (String term : q.queryWords) vector.put(term, 0.0);
		
		return vector;

	}
	
	
	private Map<String, Double> mergeVectors(Query q, Map<String,Double> vector1, Map<String,Double> vector2)
	{
		Map<String, Double> termFreq = new HashMap<>();
		
		for (String term : q.queryWords)
		{
			termFreq.put(term, vector1.get(term) + vector2.get(term));
		}
		
		return termFreq;
	}
	
	
	private Map<String, Double> getTFTypeVector(Query q, String[] terms) 
	{
		return getTFTypeVector(q, terms, 1.0);
	}

	
	private Map<String, Double> getTFTypeVector(Query q, String[] terms, Double factor) 
	{
		Map<String, Double> termFreq = newVector(q);
				
		for (String term : terms)
		{
			if (q.queryWords.contains(term))
			{
				termFreq.put(term, termFreq.get(term)+1.0);
			}
		}
		
		if (factor != 1.0)
		{
			for (String term : q.queryWords)
			{
				termFreq.put(term, termFreq.get(term) * factor);
			}
		}

		return termFreq;
	}

}
