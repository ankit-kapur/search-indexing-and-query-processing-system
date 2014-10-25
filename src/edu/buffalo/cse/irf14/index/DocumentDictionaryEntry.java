package edu.buffalo.cse.irf14.index;

import java.io.Serializable;

public class DocumentDictionaryEntry implements Serializable {
	private static final long serialVersionUID = -1628104538427743456L;
	String documentName;
	double euclideanWeight;
	double numOfTokensInDocument;
	String title;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDocumentName() {
		return documentName;
	}

	public void setDocumentName(String documentName) {
		this.documentName = documentName;
	}

	public double getEuclideanWeight() {
		return euclideanWeight;
	}

	public void setEuclideanWeight(double euclideanWeight) {
		this.euclideanWeight = euclideanWeight;
	}

	public double getNumOfTokensInDocument() {
		return numOfTokensInDocument;
	}

	public void setNumOfTokensInDocument(double numOfTokensInDocument) {
		this.numOfTokensInDocument = numOfTokensInDocument;
	}
}