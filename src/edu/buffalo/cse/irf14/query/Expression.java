package edu.buffalo.cse.irf14.query;


import java.util.Map;

public interface Expression {

	@Override
	public String toString();
	public Map<Long,DocMetaData> getPostings();

}
