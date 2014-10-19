package edu.buffalo.cse.irf14.query;

import edu.buffalo.cse.irf14.index.IndexReader;
import edu.buffalo.cse.irf14.index.IndexType;

public class IndexesAndDictionaries {

	static IndexReader placeIndex;
	static IndexReader authorIndex;
	static IndexReader categoryIndex;
	static IndexReader termIndex;
	
	public static void readIndexes(String indexDir) {
		placeIndex = new IndexReader(indexDir, IndexType.PLACE);
		authorIndex = new IndexReader(indexDir, IndexType.AUTHOR);
		categoryIndex = new IndexReader(indexDir, IndexType.CATEGORY);
		termIndex = new IndexReader(indexDir, IndexType.TERM);
	}

	public static IndexReader getIndexByType(IndexType indexType) {
		if (indexType.equals(IndexType.CATEGORY))
			return categoryIndex;
		else if (indexType.equals(IndexType.AUTHOR))
			return authorIndex;
		else if (indexType.equals(IndexType.PLACE))
			return placeIndex;
		else if (indexType.equals(IndexType.TERM))
			return termIndex;
		else return null;
	}
}
