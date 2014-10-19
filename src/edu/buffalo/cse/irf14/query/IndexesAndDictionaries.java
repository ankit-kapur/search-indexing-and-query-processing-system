package edu.buffalo.cse.irf14.query;

import java.io.File;

import edu.buffalo.cse.irf14.index.IndexReader;
import edu.buffalo.cse.irf14.index.IndexType;

public class IndexesAndDictionaries {

	static IndexReader placeIndex = new IndexReader(System.getProperty("user.dir") + File.separator + "indexdir", IndexType.PLACE);
	static IndexReader authorIndex = new IndexReader(System.getProperty("user.dir") + File.separator + "indexdir", IndexType.AUTHOR);
	static IndexReader categoryIndex = new IndexReader(System.getProperty("user.dir") + File.separator + "indexdir", IndexType.CATEGORY);
	static IndexReader termIndex = new IndexReader(System.getProperty("user.dir") + File.separator + "indexdir", IndexType.TERM);

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
