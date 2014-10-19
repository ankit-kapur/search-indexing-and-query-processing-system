package edu.buffalo.cse.irf14.index;

import java.util.HashMap;
import java.util.Map;

public class DocumentDictionary {

	public long totalNumberOfDocs;
	public Map<Long, String> documentDictionary = new HashMap<Long, String>();

	public DocumentDictionary(long totalNumberOfCount, Map<Long, String> documentDictionary) {
		super();
		this.totalNumberOfDocs = totalNumberOfCount;
		this.documentDictionary = documentDictionary;
	}

	public long getTotalNumberOfDocs() {
		return totalNumberOfDocs;
	}

	public void setTotalNumberOfDocs(long totalNumberOfCount) {
		this.totalNumberOfDocs = totalNumberOfCount;
	}

	public Map<Long, String> getDocumentDictionary() {
		return documentDictionary;
	}

	public void setDocumentDictionary(Map<Long, String> documentDictionary) {
		this.documentDictionary = documentDictionary;
	}
}
