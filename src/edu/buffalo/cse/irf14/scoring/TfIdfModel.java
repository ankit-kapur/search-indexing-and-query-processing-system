package edu.buffalo.cse.irf14.scoring;

import java.util.HashMap;
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
	public Query calculateScore(Query query) throws ScorerException {

		Map<Long, DocMetaData> docMap = query.getDocumentMap();
		Map<Long, Double> scoreMap = new HashMap<Long, Double>();

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
				String term = ScorerUtils.getTermById(termId, termDictionary);
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
			score = score / euclideanWeight;

			/* Insert the score calculated for this doc into the query object */
			scoreMap.put(docId, score);
		}
		
		double max = 0.0;
		for (Long documentId : scoreMap.keySet()) {
			double score = scoreMap.get(documentId);
			if (score > max) {
				max = score;
			}
		}
		for (Long documentId : scoreMap.keySet()) {
			QueryResults queryResult = new QueryResults();
			queryResult.setRelevancyScore(scoreMap.get(documentId) / max);
			query.addResultToMap(documentId, queryResult);
		}

		/* Assign ranks (by sorting scores) */
		ScorerUtils.assignRanks(query);

		/* Sort according to rank */
		ScorerUtils.sortAccordingtoRank(query);
		
		/* Generate snippets */
		ScorerUtils.generateSnippets(query);

		/* Clip no. of results to 10 */
		ScorerUtils.clipResults(query);
		
		return query;
	}

	
	private double calculateTfWeight(double value) {
		double weight = 0.0;
		if (value > 0)
			weight = 1.0 + (double) Math.log10(value);
		return weight;
	}
}