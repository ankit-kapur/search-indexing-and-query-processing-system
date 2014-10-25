package edu.buffalo.cse.irf14.scoring;

import java.util.HashMap;
import java.util.Map;

import edu.buffalo.cse.irf14.analysis.util.DictionaryMetadata;
import edu.buffalo.cse.irf14.analysis.util.TermMetadataForThisDoc;
import edu.buffalo.cse.irf14.index.DocumentDictionaryEntry;
import edu.buffalo.cse.irf14.index.IndexReader;
import edu.buffalo.cse.irf14.index.IndexType;
import edu.buffalo.cse.irf14.query.DocMetaData;
import edu.buffalo.cse.irf14.query.IndexesAndDictionaries;
import edu.buffalo.cse.irf14.query.Query;
import edu.buffalo.cse.irf14.query.QueryResults;

public class OkapiModel implements ScoreModel {
	public final double tunningParameterK = 1.5;
	public final double tunningParameterB = 0.75;
	public Map<Long, Double> OkapiScoreMap = new HashMap<Long, Double>();

	@Override
	public Query calculateScore(Query query) {
		double lengthofDocument = 0.0;
		double avgLenOfDocInCorpus = 0.0;
		Map<Long, DocMetaData> docMap = query.getDocumentMap();
		/* Cycle through each doc found */
		for (long docId : docMap.keySet()) {
			DocMetaData docMetadata = docMap.get(docId);
			/* Map of all query-terms */
			Map<Long, IndexType> queryTermList = Query.getQueryTermList();

			double finalOkapiScore = 0.0;
			for (Long termId : queryTermList.keySet()) {

				IndexType zone = queryTermList.get(termId);
				/* Get information from the dictionary */
				IndexReader indexReader = IndexesAndDictionaries.getIndexByType(zone);
				Map<String, DictionaryMetadata> termDictionary = indexReader.getTermDictionary();
				String term = ScorerUtils.getTermById(termId, termDictionary);
				DictionaryMetadata dictionaryMetadata = termDictionary.get(term);

				/* Calculate idf */
				double df = dictionaryMetadata.getFrequency();
				double N = indexReader.getCorpusSize();
				double idf = Math.log10(N / df);

				Map<Long, TermMetadataForThisDoc> termMap = docMetadata.getTermMetaDataMap();
				/*- Only if the term was matched in this doc, do we calculate the weight.
				 * If it wasn't in the doc, the weight will be 0.0 */
				double tf_d = 0.0;
				if (termMap.keySet().contains(termId)) {
					// Retrieving the term frequency
					tf_d = indexReader.getIndex().get((term.toLowerCase()).charAt(0)).get(termId).get(docId).getTermFrequency();
				}
				/*
				 * Retrieving Length of the document and average document
				 * length
				 */
				Map<Long, DocumentDictionaryEntry> documentDictionary = indexReader.getDocumentDictionary();
				if (documentDictionary.containsKey(docId)) {
					DocumentDictionaryEntry documentDictionaryEntry = documentDictionary.get(docId);
					lengthofDocument = documentDictionaryEntry.getNumOfTokensInDocument();
				}
				avgLenOfDocInCorpus = indexReader.getAvgLenOfDocuments();
				finalOkapiScore = idf * (((tunningParameterK + 1) * tf_d) / (tunningParameterK * ((1 - tunningParameterB) + tunningParameterB * (lengthofDocument / avgLenOfDocInCorpus)) + tf_d));
				/*
				 * Insert the score calculated for this doc into the query
				 * object
				 */
				OkapiScoreMap.put(docId, finalOkapiScore);
			}
		}
		double max = 0.0;
		for (Long documentId : OkapiScoreMap.keySet()) {
			double score = OkapiScoreMap.get(documentId);
			if (score > max) {
				max = score;
			}
		}
		for (Long documentId : OkapiScoreMap.keySet()) {
			QueryResults queryResult = new QueryResults();
			queryResult.setRelevancyScore(OkapiScoreMap.get(documentId) / max);
			query.addResultToMap(documentId, queryResult);
		}

		/* Assign ranks (by sorting scores) */
		ScorerUtils.assignRanks(query);

		/* Sort according to rank */
		ScorerUtils.sortAccordingtoRank(query);

		/* Clip no. of results to 10 */
		ScorerUtils.clipResults(query);
		
		return query;
	}

}