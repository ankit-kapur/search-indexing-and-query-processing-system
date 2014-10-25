package edu.buffalo.cse.irf14.query.test;

import java.io.File;

import edu.buffalo.cse.irf14.SearchRunner;
import edu.buffalo.cse.irf14.SearchRunner.ScoringModel;

public class SearchTester {

	public static void main(String args[]) {

//		String userQuery = "author:mankwoski OR disney";
//		String userQuery = "DISNEY";
//		String userQuery = "author:(brian OR richard) AND place:(paris OR washington)";
		String userQuery = "hostile bids mergers takeovers acquisitions";
		
		String indexDir = System.getProperty("user.dir") + File.separator + "indexdir";
		String corpusDir = System.getProperty("user.dir") + File.separator + "flattened";
		
		char mode = 'Q';
		
		SearchRunner searchRunner = new SearchRunner(indexDir, corpusDir, mode, null);
		searchRunner.query(userQuery, ScoringModel.TFIDF);
	}
}
