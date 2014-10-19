package edu.buffalo.cse.irf14.scoring;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.buffalo.cse.irf14.analysis.util.DictionaryMetadata;
import edu.buffalo.cse.irf14.analysis.util.TermMetadataForThisDoc;
import edu.buffalo.cse.irf14.index.IndexReader;
import edu.buffalo.cse.irf14.index.IndexType;
import edu.buffalo.cse.irf14.query.DocMetaData;
import edu.buffalo.cse.irf14.query.IndexesAndDictionaries;
import edu.buffalo.cse.irf14.query.Query;
import edu.buffalo.cse.irf14.query.QueryResults;

public class TfIdfModel implements ScoreModel {

	@Override
	public Query calculateScore(Query query) {
		Map<Long, DocMetaData> docMap = query.getDocumentMap();

		/* Cycle through each doc found */
		for (long docId : docMap.keySet()) {
			DocMetaData docMetadata = docMap.get(docId);

			/* Map of all query-terms */
			Map<Long, IndexType> queryTermList = Query.getQueryTermList();

			double score = 0.0;
			for (Long termId : queryTermList.keySet()) {
				IndexType zone = queryTermList.get(termId);

				/* Taking tf as '1' for each query term */
				double tf_q = 1.0;
				/* Normalize tf */
				tf_q = calculateTfWeight(tf_q);

				/* Get information from the dictionary */
				IndexReader indexReader = IndexesAndDictionaries.getIndexByType(zone);
				Map<String, DictionaryMetadata> termDictionary = indexReader.getTermDictionary();
				String term = getTermById(termId, termDictionary);
				DictionaryMetadata dictionaryMetadata = termDictionary.get(term);

				/* Calculate idf */
				double df = dictionaryMetadata.getFrequency();
				double N = indexReader.getCorpusSize();
				double idf = Math.log10(N / df);

				/*--- Query term's score ---*/
				double w_tq = tf_q * idf;

				/* Calculating document's term weight */
				double wf_td = 0.0;
				/* Map of all terms found */
				Map<Long, TermMetadataForThisDoc> termMap = docMetadata.getTermMetaDataMap();
				/*- Only if the term was matched in this doc, do we calculate the weight.
				 * If it wasn't in the doc, the weight will be 0.0 */
				if (termMap.keySet().contains(termId)) {
					/* Now calculating the document term's score */
					double tf_d = indexReader.getIndex().get((term.toLowerCase()).charAt(0)).get(termId).get(docId).getTermFrequency();
					wf_td = calculateTfWeight(tf_d);
				}

				double productOfWeights = w_tq * wf_td;
				score += productOfWeights;
			}
			
			/* Length normalize with Euclidean weights */
			double euclideanWeight = IndexesAndDictionaries.getIndexByType(IndexType.TERM).getDocumentDictionary().get(docId).getEuclideanWeight();
			score = score/euclideanWeight;
			
			/* Insert the score calculated for this doc into the query object */
			QueryResults queryResult = new QueryResults();
			queryResult.setRelevancyScore(score);
			query.addResultToMap(docId, queryResult);
		}
		
		/* Assign ranks (by sorting scores) */
		assignRanks(query);

		/* View ranked map */
		for (long docId : query.getResultsMap().keySet()) {
			System.out.println(IndexesAndDictionaries.getIndexByType(IndexType.TERM).getDocumentDictionary().get(docId).getDocumentName() + " -> score: " + query.getResultsMap().get(docId).getRelevancyScore() + " -> rank: " + query.getResultsMap().get(docId).getRank());
		}
		
		return query;
	}

	private Query assignRanks(Query query) {
		int rank = 1;
		List<Long> done = new ArrayList<Long>();
		Map<Long, QueryResults> resultsMap = query.getResultsMap();

		while (rank <= resultsMap.size()) {
			double max = 0.0;
			long maxId = 0;
			for (long docId : resultsMap.keySet()) {
				if (!done.contains(docId)) {
					QueryResults result = resultsMap.get(docId);
					double score = result.getRelevancyScore();
					if (score > max) {
						max = score;
						maxId = docId;
					}
				}
			}

			/* Add to the 'done' list */
			done.add(maxId);
			resultsMap.get(maxId).setRank(rank++);
		}

		return query;
	}

	private double calculateTfWeight(double value) {
		double weight = 0.0;
		if (value > 0)
			weight = 1.0 + (double)Math.log10(value);
		return weight;
	}

	private String getTermById(Long termId, Map<String, DictionaryMetadata> termDictionary) {
		String term = null;
		for (String key : termDictionary.keySet()) {
			if (termDictionary.get(key).getTermId() == termId) {
				term = key;
			}
		}
		return term;
	}
}