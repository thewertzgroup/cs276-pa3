package edu.stanford.cs276;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class is used to store a query sequence.
 */
public class Query {

	List<String> queryWords;
	
	public Query(String query) {
		queryWords = new ArrayList<String>(Arrays.asList(query.split(" ")));
	}
	
	public String toString()
	{
		StringBuilder result = new StringBuilder();
		String NEW_LINE = System.getProperty("line.separator");
		if (queryWords != null) result.append("query: " + queryWords + NEW_LINE);
		return result.toString();
	}
	
}
