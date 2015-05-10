package edu.stanford.cs276;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An abstract class for a scorer. Need to be extended by each specific implementation of scorers.
 */
public abstract class AScorer {
	
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
		Map<String,Double> tfQuery = new HashMap<String, Double>(); // queryWord -> term frequency
		
		/*
		 * @//TODO : Your code here
		 */
	  // raw frequencies
		// should be 1 for most queries but not necessarily true
	  for(String term: q.queryWords)
	  { 
		  double value = 1.0; 
		  if(!tfQuery.containsKey(term))
			  tfQuery.put(term, 1.0);
		  else
		  { 
			  value = tfQuery.get(term); 
			  value += 1.0; 
			  tfQuery.put(term, value); // replaces old value with new
		  } 
			  
	  }
	  
	  // should we apply sublinear scaling? 
	  // I don't believe it is necessary for the query, 
	  // since mostly the term frequencies would be 1 and there is no huge discrepancy
	  
	  
	  // document frequency
	  double idfsTerm;
	  double wqt; 
	  for(String term: tfQuery.keySet())
	  { 
		  if(idfs.containsKey(term))
			  idfsTerm = idfs.get(term);
		  else
			  idfsTerm = idfs.get(LoadHandler.specialTerm);
			  
		  wqt = tfQuery.get(term) * idfsTerm; 
		  tfQuery.put(term, wqt); 
	  }
	  
	  // No normalization is needed for query length because any query length
	  // normalization applies to all docs and so is not relevant to ranking.
	  return tfQuery;
	}
	

	////////////// Initialization/Parsing Methods ///////////////
	
	/*
	 * @//TODO : Your code here
	 */

    /////////////////////////////////////////////////////////////
	List<String> parseURL(String url)
	{ 
		String[] urlTerms = url.split("[^a-zA-Z0-9]");// splitting on non alphanumeric characters
		// turn to lower case
		for(int ind = 0; ind<urlTerms.length; ind++)
			urlTerms[ind] = urlTerms[ind].toLowerCase(); 		
		return Arrays.asList(urlTerms); 
	}
	
	List<String> parseTitle(String title)
	{ 
		 
		if(title!=null)
		{ 
			String[] titleTerms;
			titleTerms = title.toLowerCase().split("\\+");// splitting on space		
			return Arrays.asList(titleTerms); 			
		} 
		
		List<String> emptyList = new ArrayList<String>();
		emptyList.add(""); 
		return emptyList;
		
	}
	
	List<String> parseHeaders(List<String> headers)
	{
		// all headers in one string 
		String allHeaders = ""; 
		if(headers!=null)
		{ 
		//	System.out.println("num of headers " + headers.size()); 
			for(int headerInd = 0; headerInd < headers.size(); headerInd++)		
				allHeaders = allHeaders + " " + headers.get(headerInd).trim().toLowerCase();  
			} 
		String[] headersTerms = allHeaders.split("\\+");// splitting on space

		return Arrays.asList(headersTerms); 
	}
	
	Map<List<String>, Integer> parseAnchors(Map<String, Integer> anchors)
	{
		
		Map<List<String>, Integer> anchorTermsMul = new HashMap<List<String>, Integer>();		
		List<String> anchorTerms;		
		int anchor_freq; 
		if(anchors!=null)
		{ 
			for(String anchor: anchors.keySet())	
			{ 
				anchor_freq  = anchors.get(anchor); 
				anchorTerms = Arrays.asList(anchor.toLowerCase().split("\\+"));// splitting on space
				
				anchorTermsMul.put(anchorTerms, anchor_freq); 												
			}			
			
		} 
		else 			
			{ 			
				String dumAnchor = "";		
				anchorTerms = Arrays.asList(dumAnchor.split("\\+"));// splitting on space
				anchorTermsMul.put(anchorTerms, 0); 	
			} 
			
		return anchorTermsMul; 
	}
	
	Map<String, Double> getAnchorsTFS(Map<String, Integer> anchors, Query q)
	{
		Map<String, Double> tf_anchor = new HashMap<String, Double>();
		String key; 
		if(anchors!=null)
		{ 
			List<String> anchorTerms;
			double freq; 			
			int anchor_freq; 
				
			for(String anchor: anchors.keySet())	
			{ 
				anchor_freq  = anchors.get(anchor); 
				anchorTerms = Arrays.asList(anchor.toLowerCase().split("\\+"));// splitting on space			 
			
				for(int queryTInd = 0; queryTInd < q.queryWords.size(); queryTInd++)
				{ 			
					key = q.queryWords.get(queryTInd); 
					freq = (double) Collections.frequency(anchorTerms, key)*anchor_freq;
					if(tf_anchor.containsKey(key))
						freq += tf_anchor.get(key); 
					tf_anchor.put(key, freq); 					
				}
					
				}			
			
		} 
		else 
			for(int queryTInd = 0; queryTInd < q.queryWords.size(); queryTInd++)
			{ 			
				key = q.queryWords.get(queryTInd); 				 
				tf_anchor.put(key, 0.0); 		
			} 
			
		return tf_anchor; 
	}
/*	
	Map<String, Double> parseBodyHits(Map<String, List<Integer>> body_hits, Query q)
	{
		Map<String, Double> tf_bodyHits = initializeTF(q);
		 
		for(String term: body_hits.keySet())		
			tf_bodyHits.put(term, (double)body_hits.get(term).size()) ;			
		
		return tf_bodyHits; 
	}
	*/
	Map<String, Double> initializeTF(Query q)
	{ 
		Map<String, Double> tf = new HashMap<String, Double>();
		for(int queryTInd = 0; queryTInd < q.queryWords.size(); queryTInd++)		
			tf.put(q.queryWords.get(queryTInd), 0.0); 					
		return tf; 
	} 	
	
	
	/*/
	 * Creates the various kinds of term frequencies (url, title, body, header, and anchor)
	 * You can override this if you'd like, but it's likely that your concrete classes will share this implementation.
	 */
	// returns the raw term frequencies of the fields. 
	public Map<String,Map<String, Double>> getDocTermFreqs(Document d, Query q) {
		// Map from tf type -> queryWord -> score
		Map<String,Map<String, Double>> tfs = new HashMap<String,Map<String, Double>>();
		
		////////////////////Initialization/////////////////////
		
		/*
		 * @//TODO : Your code here
		 */
		
	    ////////////////////////////////////////////////////////		
		// initializing 
		tfs.put("url", new HashMap<String, Double>());
		tfs.put("title", new HashMap<String, Double>());
		tfs.put("body", new HashMap<String, Double>());
		tfs.put("header", new HashMap<String, Double>());
		// anchor will be taken care of later
		// parsing 
		List<String> urlTerms = parseURL(d.url); 
		List<String> titleTerms = parseTitle(d.title); 
		List<String> headersTerms = parseHeaders(d.headers); 

		// Loop through query terms and increase relevant tfs. Note: you should do this to each type of term frequencies.
		//TFTYPES = {"url","title","body","header","anchor"};
		double freq = 0; 
		for (String queryWord : q.queryWords) {
			/*
			 * @//TODO : Your code here
			 */
			
			freq = (double) Collections.frequency(urlTerms, queryWord);
			tfs.get("url").put(queryWord, freq);
			
			freq = (double) Collections.frequency(titleTerms, queryWord);
			tfs.get("title").put(queryWord, freq);
			
			if(d.body_hits!= null && d.body_hits.containsKey(queryWord))
			{ 				
				freq = (double)d.body_hits.get(queryWord).size(); 
				tfs.get("body").put(queryWord, freq) ;
			} 
			else 
				tfs.get("body").put(queryWord, 0.0) ;
			
			freq = (double) Collections.frequency(headersTerms, queryWord);
			tfs.get("header").put(queryWord, freq);									
		}
		
		// finally 
		// compute and add the tfs for the anchors		
		Map<String, Double> tfs_anchors = getAnchorsTFS(d.anchors, q);
		tfs.put("anchor", tfs_anchors);

		return tfs;
	}

}
