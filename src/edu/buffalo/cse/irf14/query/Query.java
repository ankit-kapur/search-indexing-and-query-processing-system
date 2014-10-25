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
	private String parsedQuery;
	private Map<Long, DocMetaData> documentMap;
	private Map<Long, QueryResults> resultsMap;
	private static Map<Long, IndexType> queryTermList;
	private String queryTime;

	Query() {
		queryTermList = new HashMap<Long, IndexType>();
		resultsMap = new HashMap<Long, QueryResults>();
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
	public Map<Long, QueryResults> getResultsMap() {
		return resultsMap;
	}
	public void setResultsMap(Map<Long, QueryResults> resultsMap) {
		this.resultsMap = resultsMap;
	}
	public void addResultToMap(Long docId, QueryResults queryResult) {
		if (this.resultsMap == null) {
			resultsMap = new HashMap<Long, QueryResults>();
		}
		resultsMap.put(docId, queryResult);
	}
	
	public Map<Long, DocMetaData> getDocumentMap() {
		return documentMap;
	}
	public void setDocumentMap(Map<Long, DocMetaData> documentMap) {
		this.documentMap = documentMap;
	}
	public String getQueryTime() {
		return queryTime;
	}
	public void setQueryTime(String queryTime) {
		this.queryTime = queryTime;
	}
}