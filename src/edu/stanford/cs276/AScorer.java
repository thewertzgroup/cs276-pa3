package edu.stanford.cs276;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An abstract class for a scorer. Need to be extended by each specific implementation of scorers.
 */
public abstract class AScorer {
	
	private static boolean debug = false;
	
	public static String IDF_MAX = "###IDF_MAX###";

	Map<String,Double> idfs; // Map: term -> idf
	
    // Various types of term frequencies that you will need
	String[] TFTYPES = {"url","title","body","header","anchor"};
	
	public AScorer(Map<String,Double> idfs) {
		this.idfs = idfs;
	}
	
	public Set<String> tfTypePermutations()
	{
		Set<String> perms = new HashSet<>();
		
		for (String t1 : TFTYPES)
			for (String t2 : TFTYPES)
				for (String t3 : TFTYPES)
					for (String t4 : TFTYPES)
						for (String t5 : TFTYPES)
							perms.add(t1 + "," + t2 + "," + t3 + "," + t4 + "," + t5);
		
		return perms;
	}
	
	public Map<String, Double> getPermWeights(String perm)
	{
		Map<String, Double> weights = new HashMap<>();
		
		double weight = 1.0;
		for (String term : perm.split(","))
		{
			weights.put(term, weight);
			weight -= 0.2;
		}

		return weights;
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
		if (null != d.body_hits)
		{
			for (String term : d.body_hits.keySet())
			{
				if (q.queryWords.contains(term))
				{
					termFreq.put(term, (double)d.body_hits.get(term).size());
				}
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
	
	
	public void debugTFS(Document d, Query q, Map<String, Map<String, Double>> tfs, Map<String, Double> tfQuery) 
	{
		System.out.println(q);
		System.out.println(d);
		System.out.println("query: " + tfQuery);
		for (String tftype : TFTYPES) System.out.println(tftype + ": " + tfs.get(tftype));
		System.out.println();
	}

	public Map<String, Double> newVector(Query q) 
	{
		Map<String, Double> vector = new HashMap<>();
		
		for (String term : q.queryWords) vector.put(term, 0.0);
		
		return vector;

	}
	
	
	public Map<String, Double> mergeVectors(final Query q, final Map<String,Double> vector1, final Map<String,Double> vector2)
	{
		Map<String, Double> termFreq = new HashMap<>();
		
		for (String term : q.queryWords)
		{
			termFreq.put(term, vector1.get(term) + vector2.get(term));
		}
		
		return termFreq;
	}
	
	public Map<String, Double> multVector(final double d, final Map<String,Double> vector)
	{
		Map<String, Double> termFreq = new HashMap<>();
		
		for (String term : vector.keySet())
		{
			termFreq.put(term, d * vector.get(term));
		}
		
		return termFreq;
	}
	
	public double dotVectors(Map<String, Double> tfDoc, Map<String, Double> tfQuery) 
	{
		double result = 0.0;
		
		for (String term : tfQuery.keySet()) result += tfDoc.get(term) * tfQuery.get(term);

		return result;
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
