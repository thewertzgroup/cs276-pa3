package edu.stanford.cs276;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Skeleton code for the implementation of a BM25 Scorer in Task 2.
 */
public class BM25Scorer extends AScorer {
	
	private static boolean debug = true;
	
	Map<Query,Map<String, Document>> queryDict; // query -> url -> document

	public BM25Scorer(Map<String,Double> idfs, Map<Query,Map<String, Document>> queryDict) {
		super(idfs);
		this.queryDict = queryDict;
		this.calcAverageLengths();
	}


	/////////////// Weights /////////////////
	static public Map<String, Double> weights;
	static {
		weights = new HashMap<>();
		weights.put("url", 1.0);
		weights.put("title", 1.0);
		weights.put("body", 1.0);
		weights.put("header", 1.0);
		weights.put("anchor", 1.0);
	}
/*
	double urlweight = -1;
	double titleweight  = -1;
	double bodyweight = -1;
	double headerweight = -1;
	double anchorweight = -1;
*/
	/////// BM25 specific weights ///////////
	double burl=-1;
	double btitle=-1;
	double bheader=-1;
	double bbody=-1;
	double banchor=-1;

	double k1=-1;
	double pageRankLambda=-1;
	double pageRankLambdaPrime=-1;
	//////////////////////////////////////////

	/////// BM25 data structures - feel free to modify ///////

	Map<Document,Map<String,Double>> lengths; // Document -> field -> length
	Map<String,Double> avgLengths;  // field name -> average length
	Map<Document,Double> pagerankScores; // Document -> pagerank score

	//////////////////////////////////////////

	// Set up average lengths for bm25, also handles pagerank
	public void calcAverageLengths() {
		lengths = new HashMap<Document,Map<String,Double>>();
		avgLengths = new HashMap<String,Double>();
		pagerankScores = new HashMap<Document,Double>();
		
		/*
		 * @//TODO : Your code here
		 */
		
		for (String tftype : TFTYPES) { avgLengths.put(tftype, 0.0); }
		
		int docCount = 0;
		int length = 0;
		
		for (Query query : queryDict.keySet())
		{
			for (String url : queryDict.get(query).keySet())
			{
				HashMap<String, Double> docLengths = new HashMap<>();

				Document d = queryDict.get(query).get(url);
				docCount++;
				
				if (debug) System.out.println(d);
				
				// "url","title","body","header","anchor"
				length = getURLTerms(d).length;
				docLengths.put("url", (double)length);
				avgLengths.put("url", avgLengths.get("url") + length);
				
				length = getTitleTerms(d).length;
				docLengths.put("title", (double)length);
				avgLengths.put("title", avgLengths.get("title") + length);
				
				docLengths.put("body",  0.0);
				if (null != d.body_hits)
				{
					for (String term : d.body_hits.keySet())
					{
						length = d.body_hits.get(term).size();
						docLengths.put("body", docLengths.get("body") + length);
						avgLengths.put("body", avgLengths.get("body") + length);
					}
				}
				
				docLengths.put("header", 0.0);
				if (null != d.headers)
				{
					for (String header : d.headers)
					{
						length = getHeaderTerms(header).length;
						docLengths.put("header", docLengths.get("header") + length);
						avgLengths.put("header", avgLengths.get("header") + length);
					}
				}
				
				docLengths.put("anchor",  0.0);
				if (null != d.anchors)
				{
					for (String anchor : d.anchors.keySet())
					{
						length = getAnchorTerms(anchor).length * d.anchors.get(anchor);
						docLengths.put("anchor", docLengths.get("anchor") + length);
						avgLengths.put("anchor", avgLengths.get("anchor") + length);
					}
				}
				
				lengths.put(d, docLengths);
				pagerankScores.put(d, (double)d.page_rank);
			}
		}
		
		//normalize avgLengths
		for (String tftype : this.TFTYPES) {
			/*
			 * @//TODO : Your code here
			 */
			avgLengths.put(tftype, avgLengths.get(tftype) / (double)docCount);
		}

	}

	////////////////////////////////////


	public double getNetScore(Map<String,Map<String, Double>> tfs, Query q, Map<String,Double> tfQuery,Document d) {
		double score = 0.0;
		
		/*
		 * @//TODO : Your code here
		 */
		
		return score;
	}

	//do bm25 normalization
	public void normalizeTFs(Map<String,Map<String, Double>> tfs,Document d, Query q) {
		/*
		 * @//TODO : Your code here
		 */
	}


	@Override
	public double getSimScore(Document d, Query q) {
		
		Map<String,Map<String, Double>> tfs = this.getDocTermFreqs(d,q);
		
		this.normalizeTFs(tfs, d, q);
		
		Map<String,Double> tfQuery = getQueryFreqs(q);

	    return getNetScore(tfs,q,tfQuery,d);
	}
	
}
