package edu.stanford.cs276;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Skeleton code for the implementation of a Cosine Similarity Scorer in Task 1.
 */
public class CosineSimilarityScorer extends AScorer {

	public CosineSimilarityScorer(Map<String,Double> idfs) {
		super(idfs);
	}

	/////////////// Weights //////////////////
	double urlweight = 1;
	double titleweight = 1;
	double bodyweight = 1;
	double headerweight = 1;
	double anchorweight = 1;

	double smoothingBodyLength = 500.0; // Smoothing factor when the body length is 0.
	//////////////////////////////////////////

	public double getNetScore(Map<String, Map<String, Double>> tfs, Query q, Map<String,Double> tfQuery, Document d) {
		double score = 0.0;
		
		/*
		 * @//TODO : Your code here
		 */
		for(String term: tfQuery.keySet())	
		{	
			score += tfQuery.get(term)*(urlweight*tfs.get("url").get(term)
									  + titleweight*tfs.get("title").get(term)
									  + bodyweight*tfs.get("body").get(term)
									  + headerweight*tfs.get("header").get(term)
									  + anchorweight*tfs.get("anchor").get(term)); 
			
		}
		
		
		return score;
	}

	// Normalize the term frequencies. Note that we should give uniform normalization to all fields as discussed
	// in the assignment handout.
	public void normalizeTFs(Map<String,Map<String, Double>> tfs,Document d, Query q) {
		/*
		 * @//TODO : Your code here
		 */
		double bodyLength_inv = 1.0/((double)d.body_length + smoothingBodyLength);
		double freq; 
		for(String term: tfs.get("url").keySet())	
		{		
			freq = tfs.get("url").get(term); 
			if(freq!=0)
			{ 			 
				freq *= bodyLength_inv;
				tfs.get("url").put(term, freq);
			} 
			
			freq = tfs.get("title").get(term); 
			if(freq!=0)
			{ 			 
				freq *= bodyLength_inv;
				tfs.get("title").put(term, freq);
			}
			
			freq = tfs.get("body").get(term); 
			if(freq!=0)
			{ 			 
				freq *= bodyLength_inv;
				tfs.get("body").put(term, freq);
			}
			
			freq = tfs.get("header").get(term); 
			if(freq!=0)
			{ 			 
				freq *= bodyLength_inv;
				tfs.get("header").put(term, freq);
			}
			
			freq = tfs.get("anchor").get(term); 
			if(freq!=0)
			{ 			 
				freq *= bodyLength_inv;
				tfs.get("anchor").put(term, freq);
			}
		}
	}


	@Override
	public double getSimScore(Document d, Query q) {
		
		Map<String,Map<String, Double>> tfs = this.getDocTermFreqs(d,q);
		
		this.normalizeTFs(tfs, d, q);
		
		Map<String,Double> tfQuery = getQueryFreqs(q);

	    return getNetScore(tfs,q,tfQuery,d);
	}

}
