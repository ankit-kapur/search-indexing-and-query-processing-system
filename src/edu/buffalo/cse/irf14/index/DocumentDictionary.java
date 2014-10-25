package edu.buffalo.cse.irf14.index;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DocumentDictionary implements Serializable {
	private static final long serialVersionUID = 3511238558669593458L;
	public long totalNumberOfDocs;
	public long avgLenOfDocInCorpus;
	public Map<Long, DocumentDictionaryEntry> documentDictionary = new HashMap<Long, DocumentDictionaryEntry>();

	public DocumentDictionary(long totalNumberOfCount, Map<Long, DocumentDictionaryEntry> documentDictionary) {
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

	public Map<Long, DocumentDictionaryEntry> getDocumentDictionary() {
		return documentDictionary;
	}

	public void setDocumentDictionary(Map<Long, DocumentDictionaryEntry> documentDictionary) {
		this.documentDictionary = documentDictionary;
	}

	public long getAvgLenOfDocInCorpus() {
		return avgLenOfDocInCorpus;
	}

	public void setAvgLenOfDocInCorpus(long avgLenOfDocInCorpus) {
		this.avgLenOfDocInCorpus = avgLenOfDocInCorpus;
	}
}