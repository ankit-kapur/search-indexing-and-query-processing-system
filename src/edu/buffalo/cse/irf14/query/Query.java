package edu.buffalo.cse.irf14.query;

import java.util.HashMap;
import java.util.Map;

import edu.buffalo.cse.irf14.index.IndexType;

/**
 * Class that represents a parsed query
 * @author nikhillo
 *
 */
public class Query {
	/*- Method to convert given parsed query into string */
	private String parsedQuery;
	private Map<Long, DocMetaData> documentMap;
	private HashMap<Integer, QueryResults> resultsMap;
	private static Map<Long, IndexType> queryTermList;

	Query() {
		queryTermList = new HashMap<Long, IndexType>();
	}
	public static Map<Long, IndexType> getQueryTermList() {
		return queryTermList;
	}
	public static void setQueryTermList(HashMap<Long, IndexType> list) {
		queryTermList = list;
	}
	public static void addQueryTermToList(long queryId, IndexType zone) {
		queryTermList.put(queryId, zone);
	}
	
	
	public String toString() {
		return parsedQuery;
	}
	public String getParsedQuery() {
		return parsedQuery;
	}
	public void setParsedQuery(String parsedQuery) {
		this.parsedQuery = parsedQuery;
	}
	public HashMap<Integer, QueryResults> getResultsMap() {
		return resultsMap;
	}
	public void setResultsMap(HashMap<Integer, QueryResults> resultsMap) {
		this.resultsMap = resultsMap;
	}
	public Map<Long, DocMetaData> getDocumentMap() {
		return documentMap;
	}
	public void setDocumentMap(Map<Long, DocMetaData> documentMap) {
		this.documentMap = documentMap;
	}
}