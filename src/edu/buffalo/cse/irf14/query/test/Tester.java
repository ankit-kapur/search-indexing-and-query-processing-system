package edu.buffalo.cse.irf14.query.test;

import java.io.File;

import edu.buffalo.cse.irf14.index.IndexReader;
import edu.buffalo.cse.irf14.index.IndexType;

public class Tester {

	public static void main(String[] args) {

		String indexDir = System.getProperty("user.dir") + File.separator + "training/harsh";
		
		IndexReader categoryIndex = new IndexReader(indexDir, IndexType.CATEGORY);
		
		System.out.println("Category index length: " + categoryIndex.getCorpusSize());
		
		/* Display all info about the term
		 * such as postings list, positional indexes, term frequency */
		categoryIndex.getTermInformation();
	}

}
