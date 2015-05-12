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
	
	private static boolean debug = false;

	public CosineSimilarityScorer(Map<String,Double> idfs) {
		super(idfs);
	}

	/////////////// Weights //////////////////
	static public Map<String, Double> weights;
	static {
		weights = new HashMap<>();
		weights.put("url", 1.0);
		weights.put("title", 0.8);
		weights.put("body", 0.4);
		weights.put("header", 0.6);
		weights.put("anchor", 0.2);
	}
/*
	double urlweight = 1.0;
	double titleweight = 1.0;
	double bodyweight = 1.0;
	double headerweight = 1.0;
	double anchorweight = 1.0;
*/
	double smoothingBodyLength = 500; // Smoothing factor when the body length is 0.
	//////////////////////////////////////////

	public double getNetScore(Map<String, Map<String, Double>> tfs, Query q, Map<String,Double> tfQuery, Document d) {
		double score = 0.0;
		
		/*
		 * @//TODO : Your code here
		 */ 
		// "url","title","body","header","anchor"

		Map<String, Double> tfDoc = newVector(q);
		for (String tftype : TFTYPES)
		{
			tfDoc = mergeVectors(q, tfDoc, multVector(weights.get(tftype), tfs.get(tftype)));
		}
		
		score = dotVectors(tfDoc, tfQuery);
		
		return score;
	}

	// Normalize the term frequencies. Note that we should give uniform normalization to all fields as discussed
	// in the assignment handout.
	public void normalizeTFs(Map<String,Map<String, Double>> tfs,Document d, Query q) {
		/*
		 * @//TODO : Your code here
		 */
		Double factor = d.body_length + smoothingBodyLength;
		
		for (String tftype : TFTYPES)
		{
			for (String term : q.queryWords)
			{
				tfs.get(tftype).put(term, tfs.get(tftype).get(term) / factor);
			}
		}
	}


	@Override
	public double getSimScore(Document d, Query q) {
		
		Map<String,Map<String, Double>> tfs = this.getDocTermFreqs(d,q);
		
		this.normalizeTFs(tfs, d, q);
		
		Map<String,Double> tfQuery = getQueryFreqs(q);
		
		if (debug) debugTFS(d, q, tfs, tfQuery);

	    return getNetScore(tfs,q,tfQuery,d);
	}

}
