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
	double urlweight = 15;
	double titleweight = 20;
	double bodyweight = 5;
	double headerweight = 15;
	double anchorweight = 20;

	/////// BM25 specific weights ///////////
	double burl=0.75;
	double btitle=0.75;
	double bheader=0.75;
	double bbody=0.75;
	double banchor=0.75;

	double k1=1000;
	double pageRankLambda=4;
	double pageRankLambdaPrime=0;
	//////////////////////////////////////////

	/////// BM25 data structures - feel free to modify ///////

	Map<Document,Map<String,Double>> lengths; // Document -> field -> length
	Map<String,Double> avgLengths;  // field name -> average length
	Map<Document,Double> pagerankScores; // Document -> pagerank score
	Map<String,Double> b;  // field name -> constant b
	Map<String,Double> weight;  // field name -> weight
	

	//////////////////////////////////////////

	// Set up average lengths for bm25, also handles pagerank
	public void calcAverageLengths() {
		lengths = new HashMap<Document,Map<String,Double>>();
		avgLengths = new HashMap<String,Double>();		
		pagerankScores = new HashMap<Document,Double>();
		b = new HashMap<String,Double>();
		weight = new HashMap<String,Double>();
		
		/*
		 * @//TODO : Your code here
		 */
		// for every field, compute the avlen over all the collection
		double avlenURL, avLenTitle, avLenBody, avLenHeader, avLenAnchor; 
		avlenURL = avLenTitle = avLenBody = avLenHeader = avLenAnchor = 0.0; 
		int numURL, numTitle, numBody, numHeader, numAnchor; 
		numURL = numTitle = numBody = numHeader = numAnchor = 0;
		Document doc; 		  
		Map<String,Double> lendthsPerdoc = new HashMap<String,Double>();
		double len; 
		for(Query q: queryDict.keySet())
		{ 
			
			/*In case of duplicate documents, you can use the field length (e.g. header length) from any of them. 
			 * For this assignment this will not influence the result too much. For a real-world ranking system, 
			 * I agree that we should take the complete field information into account.
			 * Update: I am sorry that I made a mistake here by constraining you with a single choice. 
			 * Actually since we only provide you with paritial information about each document, you should 
			 * feel free to use any strategy. Specifically, you can count every document in the dataset to calculate
			 *  the average length (no matter they are duplicates or not). Or you can select arbitrary one from the duplicates
			 *   documents. Or you can experiment with both and see which one gives you better performance. 
			 *   You have full freedom here and you can mention this in your report. Sorry if my prior answer confused you.*/
			
			// currently not taking care of duplicates: like "you can count every document in the dataset to calculate
			// the average length (no matter they are duplicates or not)", should try other strategy too. 
			for(String url: queryDict.get(q).keySet())
			{ 
				doc = queryDict.get(q).get(url); 
				
				lendthsPerdoc.put("url", (double) doc.url.length()); 				
				avlenURL += lendthsPerdoc.get("url"); 
				numURL++; 
				
				if(doc.title == null)
					lendthsPerdoc.put("title", 0.0); 
				else
				{ 
					lendthsPerdoc.put("title", (double) doc.title.length());
					avLenTitle += lendthsPerdoc.get("title"); 
					numTitle++;
				} 
				
				lendthsPerdoc.put("body" , (double) doc.body_length);
				avLenBody += lendthsPerdoc.get("body"); 
				numBody++;
				
				len = 0.0; 				
				if(doc.headers == null)				
					lendthsPerdoc.put("header" , len);
				else 
				{ 
					// assume that there is one big document that contains all of the headers
					for(int headerInd = 0; headerInd<doc.headers.size(); headerInd++ )
						len += (double) doc.headers.get(headerInd).length();
					lendthsPerdoc.put("header" , len);
					avLenHeader += len;  
					numHeader++;
				} 
				
				len = 0.0; 
				if(doc.anchors == null)				
					lendthsPerdoc.put("anchor" , len);
				else 
				{ 
					// assume that there is one big document that	contains all of the anchors 
	    			// with the anchor text multiplied by the anchor count.
					for(String anchor:doc.anchors.keySet())
						len  += (double) anchor.length()*doc.anchors.get(anchor);
					lendthsPerdoc.put("anchor" , len);
					avLenAnchor += len; 
					numAnchor++; 			
				} 
				lengths.put(doc, lendthsPerdoc); 
				
			}			
			
		}
		
		if(numURL!=0)
			avgLengths.put("url", avlenURL/numURL);
		else 
			avgLengths.put("url", 0.0);
		
		if(numTitle!=0)
			avgLengths.put("title", avLenTitle/numTitle);
		else 
			avgLengths.put("title", 0.0);
		
		if(numBody!=0)	
			avgLengths.put("body", avLenBody/numBody);
		else
			avgLengths.put("body", 0.0);
		
		if(numHeader!=0)
			avgLengths.put("header", avLenHeader/numHeader);
		else 
			avgLengths.put("header", 0.0);
		
		if(numAnchor!=0)
			avgLengths.put("anchor", avLenHeader/numAnchor);
		else 
			avgLengths.put("anchor", 0.0);
			
		double mappedPageRank; 
		for(Document d: lengths.keySet())
		{ 	
			//normalize lengths
			for (String tfType : this.TFTYPES) {
			/*
			 * @//TODO : Your code here
			 */
				len = lengths.get(d).get(tfType);
				if(avgLengths.get(tfType) !=0.0) // should always be true
					lengths.get(d).put(tfType, len/avgLengths.get(tfType));							
			}
			
			// pageRank scores
			mappedPageRank = pageRankLambda* Math.log(pageRankLambdaPrime + (double)d.page_rank);
			pagerankScores.put(d, mappedPageRank);						
		} 
		
		b.put("url", burl);
		b.put("title", btitle);
		b.put("body", bbody);
		b.put("header", bheader); 
		b.put("anchor", banchor); 
		
		weight.put("url", urlweight);
		weight.put("title", titleweight);
		weight.put("body", bodyweight);
		weight.put("header", headerweight); 
		weight.put("anchor", anchorweight);
		
		

	}

	////////////////////////////////////


	public double getNetScore(Map<String,Map<String, Double>> tfs, Query q, Map<String,Double> tfQuery,Document d) {
		double score = 0.0;
		
		/*
		 * @//TODO : Your code here
		 */
		double ftf_dft; 
		double wdt;
		double idfsTerm; 
		for(String term: tfs.get("url").keySet())
		{
			wdt = 0.0; 
			// aggregating normalized term frequencies from all fields 
			for (String tfType : this.TFTYPES) {						 				
				ftf_dft = tfs.get(tfType).get(term);
				wdt+=weight.get(tfType)*ftf_dft; 
			}

			// the BM25 RSV
			if(idfs.containsKey(term))
				idfsTerm = idfs.get(term);
			else
				idfsTerm = idfs.get(LoadHandler.specialTerm);
			score += wdt*idfsTerm/(k1+wdt);  
		} 
		// page rank incorporation
		score += pagerankScores.get(d); // already weighted by lambda and filtered by log with lambda'
		return score;
	}

	//do bm25 normalization
	public void normalizeTFs(Map<String,Map<String, Double>> tfs,Document d, Query q) {
		/*
		 * @//TODO : Your code here
		 */
		// tfs has the raw query term frequencies for every type of fields.
		double freq = 0.0; 		
		for (String tfType : this.TFTYPES) {
			for(String term: tfs.get("url").keySet())
			{ 				
				freq = tfs.get(tfType).get(term);
				if(avgLengths.get(tfType)!=0) // should always be true
					freq = freq/(1.0 + b.get(tfType)*( lengths.get(d).get(tfType)/avgLengths.get(tfType) - 1.0) ); 
				else 
					freq = 0.0; 
				tfs.get(tfType).put(term, freq); 
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
