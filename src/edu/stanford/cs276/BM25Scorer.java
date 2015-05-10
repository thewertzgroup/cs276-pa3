package edu.stanford.cs276;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Skeleton code for the implementation of a BM25 Scorer in Task 2.
 */
public class BM25Scorer extends AScorer {
	Map<Query,Map<String, Document>> queryDict; // query -> url -> document

	public BM25Scorer(Map<String,Double> idfs, Map<Query,Map<String, Document>> queryDict) {
		super(idfs);
		this.queryDict = queryDict;
		this.calcAverageLengths();
	}


	/////////////// Weights /////////////////
	double urlweight = -1;
	double titleweight  = -1;
	double bodyweight = -1;
	double headerweight = -1;
	double anchorweight = -1;

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
		 * 
		 * https://piazza.com/class/i7hsnt5af2d6pi?cid=727
		 */
		Map<String,Integer> counts = new HashMap<>();
		
		for (String tfType : this.TFTYPES) { counts.put(tfType, 0); }

		for (Query q : queryDict.keySet())
		{

			for (String url : queryDict.get(q).keySet())
			{
				Document d = queryDict.get(q).get(url);
				
				Map<String,Double> documentLengths = new HashMap<>();
				for (String tfType : this.TFTYPES) { documentLengths.put(tfType, 0.0); }

				documentLengths.put("title", (double)d.title.split("\\s+").length);
				counts.put("title", counts.get("title") + 1);
				
				documentLengths.put("body", (double)d.body_length);
				counts.put("body", counts.get("body") + 1);
				
				if (null != d.headers)
				{
					for (String header : d.headers)
					{
						if (null == documentLengths.get("header"))
						{
							documentLengths.put("header", (double)header.split("\\s+").length);
						}
						else
						{
							documentLengths.put("header", documentLengths.get("header") + (double)header.split("\\s+").length);
						}
						// TODO: Do we want average header length, or average header terms per document? Move outside the 'for' loop if the latter. cw
						counts.put("header", counts.get("header") + 1);
					}
				}
				
				if (null != d.anchors)
				{
					for (String anchor : d.anchors.keySet())
					{
						// TODO: Multiple number of anchor terms x number of anchors? cs
						double anchorLength = (double)anchor.split("\\s+").length * (double)d.anchors.get(anchor);
						
						if (null == documentLengths.get("anchor"))
						{
							documentLengths.put("anchor", anchorLength);
						}
						else
						{
							documentLengths.put("anchor", documentLengths.get("anchor") + anchorLength);
						}
						// TODO: Are we sure to only add 1 here? See question on headers as well. cw
						counts.put("anchor", counts.get("anchor") + 1);
					}
				}
				
				// "url","title","body","header","anchor"
				
				pagerankScores.put(d, (double)d.page_rank);

				lengths.put(d,  documentLengths);
			}
			
		}
		
		//normalize avgLengths
		for (String tfType : this.TFTYPES) {
			/*
			 * @//TODO : Your code here
			 */
			double tfTypeSum = 0.0;
			for (Document d : lengths.keySet())
			{
				tfTypeSum += lengths.get(d).get(tfType);
			}
			avgLengths.put(tfType, tfTypeSum / counts.get(tfType));
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
