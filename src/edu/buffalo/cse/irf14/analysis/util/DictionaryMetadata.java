package edu.buffalo.cse.irf14.analysis.util;

import java.io.Serializable;

public class DictionaryMetadata implements Serializable {
	private static final long serialVersionUID = 1L;
	long termId;
	long frequency;

	public DictionaryMetadata() {

	}

	public DictionaryMetadata(long termId, long frequency) {
		super();
		this.termId = termId;
		this.frequency = frequency;
	}

	public long getTermId() {
		return termId;
	}

	public void setTermId(long termId) {
		this.termId = termId;
	}

	public long getFrequency() {
		return frequency;
	}

	public void setFrequency(long frequency) {
		this.frequency = frequency;
	}
}