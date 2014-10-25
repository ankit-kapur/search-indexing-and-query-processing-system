package edu.buffalo.cse.irf14;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import edu.buffalo.cse.irf14.index.IndexType;
import edu.buffalo.cse.irf14.query.IndexesAndDictionaries;
import edu.buffalo.cse.irf14.query.Query;
import edu.buffalo.cse.irf14.query.QueryParser;
import edu.buffalo.cse.irf14.query.QueryParserException;
import edu.buffalo.cse.irf14.scoring.ModelFactory;
import edu.buffalo.cse.irf14.scoring.ScoreModel;
import edu.buffalo.cse.irf14.scoring.ScorerException;

/**
 * Main class to run the searcher. As before implement all TODO methods unless
 * marked for bonus
 * 
 * @author nikhillo
 *
 */
public class SearchRunner {
	public enum ScoringModel {
		TFIDF, OKAPI
	};

	char mode;
	private PrintStream stream;

	/**
	 * Default (and only public) constuctor
	 * 
	 * @param indexDir
	 *             : The directory where the index resides
	 * @param corpusDir
	 *             : Directory where the (flattened) corpus resides
	 * @param mode
	 *             : Mode, one of Q or E
	 * @param stream
	 *             : Stream to write output to
	 */
	public SearchRunner(String indexDir, String corpusDir, char mode, PrintStream stream) {
		IndexesAndDictionaries.readIndexes(indexDir);
		IndexesAndDictionaries.setCorpusDirec(corpusDir);
		this.mode = mode;
		this.stream = stream;
	}

	/**
	 * Method to execute given query in the Q mode
	 * 
	 * @param userQuery
	 *             : Query to be parsed and executed
	 * @param model
	 *             : Scoring Model to use for ranking results
	 */

	public void query(String userQuery, ScoringModel model) {
		try {
			ScoreModel scoreModel = null;
			ModelFactory modelFactory = ModelFactory.getInstance();
			
			/* Start time */
			long startTime = new Date().getTime();
			Query query = QueryParser.parse(userQuery, "OR");
			// The above query object will contain ParsedQuery and
			// PostingsList of the query
			if (model.equals(ScoringModel.TFIDF)) {
				scoreModel = modelFactory.getModelForQuery(ScoringModel.TFIDF);
			}
			if (model.equals(ScoringModel.OKAPI)) {
				scoreModel = modelFactory.getModelForQuery(ScoringModel.OKAPI);
			}
			
			/*- This query object will contain final relevant scores of the query */
			query = scoreModel.calculateScore(query);
			double queryTime = (new Date().getTime() - startTime) / 1000.0;

			query.setQueryTime(String.valueOf(queryTime));
			

			/* Print ranked map */
			for (long docId : query.getResultsMap().keySet()) {
				System.out.println(IndexesAndDictionaries.getIndexByType(IndexType.TERM).getDocumentDictionary().get(docId).getDocumentName() + " -> score: " + query.getResultsMap().get(docId).getRelevancyScore() + " -> rank: " + query.getResultsMap().get(docId).getRank() + "\nTitle: " + query.getResultsMap().get(docId).getTitle() + "\nSnippet: " + query.getResultsMap().get(docId).getSnippet() + "\n");
			}
			System.out.println("Query execution time ==> " + queryTime + " seconds");
			
			
			
		} catch (QueryParserException e) {
			e.printStackTrace();
		} catch (ScorerException e) {
			e.printStackTrace();
		}

	}

	/*
	 * Method to execute queries in E mode
	 * 
	 * @param queryFile : The file from which queries are to be read and
	 * executed
	 */
	public void query(File queryFile) {
		String fileBody = null;
		int numResults = 0;
		Map<String, String> queryMap = new HashMap<String, String>();
		try {
			StringBuffer sbBuffer = new StringBuffer();
			fileBody = new Scanner(queryFile).useDelimiter("\\A").next();
			String[] lines = fileBody.split("[\\r]");
			int nOfQ = Integer.parseInt(lines[0].substring(lines[0].indexOf('=') + 1));
			System.out.println(nOfQ);
			for (int i = 1; i <= nOfQ; i++) {
				String queryId = lines[i].substring(0, lines[i].indexOf(':'));
				String query = lines[i].substring(lines[i].indexOf('{') + 1, lines[i].indexOf('}'));
				System.out.println(queryId + " " + "Query-->" + query);
				queryMap.put(queryId, query);
			}

			for (String queryId : queryMap.keySet()) {
				int size = 0;
				try {
					numResults++;

					/* Start time */
					long startTime = new Date().getTime();
					
					Query query = QueryParser.parse(queryMap.get(queryId), "OR");
					ScoreModel scoreModel = null;
					ModelFactory modelFactory = ModelFactory.getInstance();
					scoreModel = modelFactory.getModelForQuery(ScoringModel.OKAPI);
					query = scoreModel.calculateScore(query);

					/* Set time taken by query */
					double queryTime = (new Date().getTime() - startTime) / 1000.0;
					query.setQueryTime(String.valueOf(queryTime));
					
					sbBuffer.append(queryId).append(":").append("{");
					for (long docId : query.getResultsMap().keySet()) {
						size++;
						if (size == query.getResultsMap().size())
							sbBuffer.append(IndexesAndDictionaries.getIndexByType(IndexType.TERM).getDocumentDictionary().get(docId).getDocumentName()).append("#").append(query.getResultsMap().get(docId).getRelevancyScore());
						else
							sbBuffer.append(IndexesAndDictionaries.getIndexByType(IndexType.TERM).getDocumentDictionary().get(docId).getDocumentName()).append("#").append(query.getResultsMap().get(docId).getRelevancyScore()).append(",").append(" ");
					}
					sbBuffer.append("}\n\r");


					/* Print ranked map */
					for (long docId : query.getResultsMap().keySet()) {
						System.out.println(IndexesAndDictionaries.getIndexByType(IndexType.TERM).getDocumentDictionary().get(docId).getDocumentName() + " -> score: " + query.getResultsMap().get(docId).getRelevancyScore() + " -> rank: " + query.getResultsMap().get(docId).getRank() + "\nTitle: " + query.getResultsMap().get(docId).getTitle() + "\nSnippet: " + query.getResultsMap().get(docId).getSnippet() + "\n");
					}
					System.out.println("Query execution time ==> " + queryTime + " seconds");
					
				} catch (QueryParserException e) {
					e.printStackTrace();
				} catch (ScorerException e) {
					e.printStackTrace();
				}

			}
			sbBuffer.insert(0, "numResults=" + numResults + "\n\r");
			stream.println(sbBuffer.toString());

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public PrintStream getsPrintStream() {
		return stream;
	}

	/**
	 * General cleanup method
	 */
	public void close() {
		// TODO : IMPLEMENT THIS METHOD
	}

	/**
	 * Method to indicate if wildcard queries are supported
	 * 
	 * @return true if supported, false otherwise
	 */
	public static boolean wildcardSupported() {
		return false;
	}

	/**
	 * Method to get substituted query terms for a given term with wildcards
	 * 
	 * @return A Map containing the original query term as key and list of
	 *         possible expansions as values if exist, null otherwise
	 */
	public Map<String, List<String>> getQueryTerms() {
		return null;
	}

	/**
	 * Method to indicate if speel correct queries are supported
	 * 
	 * @return true if supported, false otherwise
	 */
	public static boolean spellCorrectSupported() {
		return false;
	}

	/**
	 * Method to get ordered "full query" substitutions for a given misspelt
	 * query
	 * 
	 * @return : Ordered list of full corrections (null if none present) for
	 *         the given query
	 */
	public List<String> getCorrections() {
		return null;
	}
}