package edu.buffalo.cse.irf14.query.test;

import java.io.File;

import edu.buffalo.cse.irf14.SearchRunner;
import edu.buffalo.cse.irf14.SearchRunner.ScoringModel;

public class SearchTester {

	public static void main(String args[]) {

		//String userQuery = "author:mankwoski OR disney";
		String userQuery = "adobe";
		
		String indexDir = System.getProperty("user.dir") + File.separator + "indexdir";
		char mode = 'Q';
		
		SearchRunner searchRunner = new SearchRunner(indexDir, null, mode, null);
		searchRunner.query(userQuery, ScoringModel.TFIDF);
	}
}
