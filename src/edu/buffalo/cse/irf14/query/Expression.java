package edu.buffalo.cse.irf14.query;

import java.util.Map;

public abstract class Expression {

	/* Abstract methods */
	@Override
	public abstract String toString();
	public abstract Map<Long, DocMetaData> getPostings();
}
