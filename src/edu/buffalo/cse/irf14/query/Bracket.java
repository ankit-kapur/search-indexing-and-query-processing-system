package edu.buffalo.cse.irf14.query;

import java.util.Map;

public class Bracket extends Expression {

	
	private String bracketValue;

	public Bracket(String s) {
		this.bracketValue=s;
		// TODO Auto-generated constructor stub
	}

	public String getS() {
		return bracketValue;
	}

	public void setS(String s) {
		this.bracketValue = s;
	}

	@Override
	public Map<Long, DocMetaData> getPostings() {
		return null;
	}

	@Override
	public String toString() {
		return null;
	}
}
