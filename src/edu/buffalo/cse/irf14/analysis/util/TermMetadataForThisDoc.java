package edu.buffalo.cse.irf14.analysis.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.buffalo.cse.irf14.index.IndexType;

public class TermMetadataForThisDoc implements Serializable {
	private static final long serialVersionUID = 1L;
	int termFrequency;
	int boosterScore;
	char firstLetter;
	List<Integer> positions;
	IndexType zone;

	public TermMetadataForThisDoc(int termFrequency, int boosterScore, char firstLetter, int position) {
		super();
		this.termFrequency = termFrequency;
		this.boosterScore = boosterScore;
		this.firstLetter = firstLetter;

		addPositionToList(position);
	}

	public IndexType getZone() {
		return zone;
	}

	public void setZone(IndexType zone) {
		this.zone = zone;
	}

	public void addPositionToList(int position) {
		if (positions == null) {
			positions = new ArrayList<Integer>();
		}
		positions.add(position);
	}

	public List<Integer> getPositions() {
		return positions;
	}

	public void setPositions(List<Integer> positions) {
		this.positions = positions;
	}

	public TermMetadataForThisDoc() {
	}

	public int getTermFrequency() {
		return termFrequency;
	}

	public void setTermFrequency(int termFrequency) {
		this.termFrequency = termFrequency;
	}

	public int getBoosterScore() {
		return boosterScore;
	}

	public void setBoosterScore(int boosterScore) {
		this.boosterScore = boosterScore;
	}

	public char getFirstLetter() {
		return firstLetter;
	}

	public void setFirstLetter(char firstLetter) {
		this.firstLetter = firstLetter;
	}
}
