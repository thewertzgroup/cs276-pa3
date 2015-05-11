package edu.stanford.cs276;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.stanford.cs276.util.Pair;

/**
 * A skeleton for implementing the Smallest Window scorer in Task 3.
 * Note: The class provided in the skeleton code extends BM25Scorer in Task 2. However, you don't necessarily
 * have to use Task 2. (You could also use Task 1, in which case, you'd probably like to extend CosineSimilarityScorer instead.)
 */
//public class SmallestWindowScorer extends BM25Scorer {
public class SmallestWindowScorer extends CosineSimilarityScorer {

	/////// Smallest window specific hyper-parameters ////////
	double B = 1.3;
	// bm25
	// 1.0 --> 0.8172678063690284
	// 1.1 --> 0.8186279498855178
	// 1.2 --> 
	// 1.3 --> 0.8210079834752704 *
	// 1.4 --> 0.821231077485018
	// 1.5 --> 0.8211871279652254
	// 2.0 --> 0.819763972299397
	// 2.1 --> 0.819033141772585
	// 2.5 --> 0.8191307979581912
	// 3.0 --> 0.8196449983459656
	// 3.5 --> 0.8187255438741172

	// cosine similarity
	// 1.0 --> 0.8362797213208841
	// 1.5 --> 0.8382538956227988
	// 1.6 --> 0.8381542314536609
	// 1.7 --> 0.8387713698845667
	// 1.8 --> 0.8391990972186835 *
	// 1.9 --> 0.8388521774609936
	// 2.0 --> 0.8384153586767213  
	// 2.1 --> 0.8384114042843523
	// 2.5 --> 0.8378363859107839
	double boostmod = -1;

	//////////////////////////////
	
	Map<Pair<Document, Query>,Double> docQuerySmallestWindow; 	// store the smallest window for a query and a document
	
	// for CosineSimilarityScorer extension
	Map<Query,Map<String, Document>> queryDict; // query -> url -> document
	public SmallestWindowScorer(Map<String, Double> idfs, Map<Query,Map<String, Document>> queryDict) {
		super(idfs);
		this.queryDict = queryDict;
		handleSmallestWindow();
	}

	// for BM25Scorer extension
/*	public SmallestWindowScorer(Map<String, Double> idfs,Map<Query,Map<String, Document>> queryDict) {
		super(idfs, queryDict);
		handleSmallestWindow();
	}
	*/

	
	public void handleSmallestWindow() {
		/*
		 * @//TODO : Your code here
		 */
		Document doc; 
		double curSmallestWindow = -1;
		List<String> docStrList; 
		boolean isBodyField = false; 
		docQuerySmallestWindow = new HashMap<Pair<Document, Query>,Double>(); 
		for(Query q: queryDict.keySet())
		{ 			
			for(String url: queryDict.get(q).keySet())
			{
				doc = queryDict.get(q).get(url); 
				
				curSmallestWindow= -1;
				isBodyField = false;
				
				docStrList = parseURL(doc.url); 
				curSmallestWindow = checkWindow(q,docStrList, curSmallestWindow, isBodyField); 
				
				if(doc.title!=null)
				{
					docStrList = parseTitle(doc.title); 
					curSmallestWindow = checkWindow(q,docStrList, curSmallestWindow, isBodyField);
				} 
								
				if(doc.headers!=null)
				{ 				 
					for(int headerInd = 0; headerInd < doc.headers.size(); headerInd++)
					{ 
						docStrList = Arrays.asList(doc.headers.get(headerInd).trim().toLowerCase().split("\\+"));  
						curSmallestWindow = checkWindow(q,docStrList, curSmallestWindow, isBodyField);
					} 
				}
				
				if(doc.anchors!=null)
				{ 
					Map<List<String>, Integer> anchorTermsMul = parseAnchors(doc.anchors); 
					for(List<String> anchorList:anchorTermsMul.keySet())
						curSmallestWindow = checkWindow(q,anchorList, curSmallestWindow, isBodyField);
				} 
				
				if(doc.body_hits!=null)
				{ 
					isBodyField = true; 
					docStrList = new ArrayList<String>(); 
					docStrList.add(doc.url); 
					curSmallestWindow = checkWindow(q,docStrList, curSmallestWindow, isBodyField);
				} 
		//		if(curSmallestWindow!=-1)
		//			System.out.println(curSmallestWindow); 
				docQuerySmallestWindow.put(new Pair<Document, Query>(doc, q), curSmallestWindow); 
				
			}
		}
		
	}

	public Map<String, List<Integer>> generateQueryHits(Set<String> uniqueQueryWords, List<String> docStrList)
	{ 
		Map<String, List<Integer>> hits = new HashMap<String, List<Integer>>();
			
		if(uniqueQueryWords == null)
			return hits; 
				
		if(uniqueQueryWords.size()<=docStrList.size())
		{ 
			List<Integer> positions; 
			for(String term: uniqueQueryWords)
			{ 
				for(int ind = 0; ind < docStrList.size(); ind++)
					if(docStrList.get(ind).equals(term)) // for every term that is in the docStrList, the list 
														 // of positions should be sorted in ascending order
					{ 
						if(hits.containsKey(term))
						{ 
							hits.get(term).add(ind);  								
						} 
						else 
						{ 
							positions = new ArrayList<Integer>(); 
							positions.add(ind); 
							hits.put(term, positions);
						}
					} 
							
			}
		} 
		
		 
		return hits; 
		
	}
	
	public double computeSmallestWindow(Map<String, List<Integer>> hits)
	{ 
		double smallestWindow = -1;	
		double currWindow = -1; 
		Map.Entry<String, Integer> minPosTerm, maxPosTerm; 
		String termToMove; 
		int indToMove; 
		Map<String, Integer> currentParsedPositions = new HashMap<String, Integer> ();
		for(String term: hits.keySet())
			currentParsedPositions.put(term,hits.get(term).get(0));
		
		while(true)
		{ 
			List<Map.Entry<String, Integer>> listForSort = 
					new LinkedList<Map.Entry<String, Integer>>(currentParsedPositions.entrySet());
			minPosTerm = (Map.Entry<String, Integer>) Collections.min(listForSort, new Comparator<Map.Entry<String, Integer>>() {
				@Override
				public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
					 return o1.getValue().compareTo(o2.getValue());  
					
				}	
			});
			
			maxPosTerm = (Map.Entry<String, Integer>) Collections.max(listForSort, new Comparator<Map.Entry<String, Integer>>() {
				@Override
				public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
					 return o1.getValue().compareTo(o2.getValue());  
					
				}	
			});
			
			currWindow =  (double) ( maxPosTerm.getValue() -  minPosTerm.getValue() + 1 );
			if(smallestWindow == -1 || currWindow < smallestWindow)
				smallestWindow = currWindow; 
			
			// progressing to the next position in the document, for the term that is earliest in the window.  
			termToMove = minPosTerm.getKey();
			indToMove  =  minPosTerm.getValue();
			indToMove++; 
			if(indToMove < hits.get(termToMove).size())
				currentParsedPositions.put(termToMove,hits.get(termToMove).get(indToMove)); // overwrite older position for this term 
																							// advance it to next position
			else // no more occurences of the leftmost term
				break; 
		} 
		
		return smallestWindow; 
	}
	
	public double checkWindow(Query q,List<String> docStrList,double curSmallestWindow,boolean isBodyField) {
		/*
		 * @//TODO : Your code here
		 */
		Set<String> uniqueQueryWords = new HashSet<String>(q.queryWords);
		int numUniqueQueryWords = uniqueQueryWords.size(); 
		
		double newSmallestWindow = -1; 
		if(!isBodyField)
		{ 
			//..
			Map<String, List<Integer>> hits = generateQueryHits(uniqueQueryWords, docStrList); // term -> [list of positions]
			if(numUniqueQueryWords <= hits.size())
			{ 
			//	System.out.println("looking..."); 
				newSmallestWindow = computeSmallestWindow(hits);
			//	System.out.println("Smallest: " + newSmallestWindow);
			} 
		} 
		else 
		{ 
			String url = docStrList.get(0);
			if(numUniqueQueryWords <= queryDict.get(q).get(url).body_hits.size() )
			{ 
			//	System.out.println("looking in body..."); 
				newSmallestWindow = computeSmallestWindow(queryDict.get(q).get(url).body_hits);
			//	System.out.println("In body, Smallest: " + newSmallestWindow);
			} 
		} 
		
		if(curSmallestWindow == -1)
			return newSmallestWindow; 
		else if(newSmallestWindow == -1 || newSmallestWindow >= curSmallestWindow)
			return curSmallestWindow;
		else 
			return newSmallestWindow; 
	}
	
	public double computeBoost(Query q, double smallestWindow)
	{ 
		double boost= 1;
		if(smallestWindow>0 )
		{ 
			Set<String> uniqueQueryWords = new HashSet<String>(q.queryWords);
			double exponent = (double) uniqueQueryWords.size() * Math.log(B)/smallestWindow;
			boost = Math.exp(exponent);
		} 
		return boost; 
	}
	
	@Override
	public double getSimScore(Document d, Query q) {
		double score = 0; 
		Map<String,Map<String, Double>> tfs = this.getDocTermFreqs(d,q);
		
		this.normalizeTFs(tfs, d, q);
		
		Map<String,Double> tfQuery = getQueryFreqs(q);
		
		
		score  = getNetScore(tfs,q,tfQuery,d);
	    
		// applying the boosting based on the smallest window, 			
		double smallestWindow = docQuerySmallestWindow.get(new Pair<Document, Query>(d, q));
		double boost = computeBoost(q, smallestWindow);
	//	if(boost!=1)
	//		System.out.println(numBoosts++); 
		return score*boost; 
		
	}

}
