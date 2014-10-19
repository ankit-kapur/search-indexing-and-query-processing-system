package edu.buffalo.cse.irf14.scoring;

import java.util.Map;

import edu.buffalo.cse.irf14.analysis.util.TermMetadataForThisDoc;
import edu.buffalo.cse.irf14.index.IndexReader;
import edu.buffalo.cse.irf14.index.IndexType;
import edu.buffalo.cse.irf14.query.DocMetaData;
import edu.buffalo.cse.irf14.query.IndexesAndDictionaries;
import edu.buffalo.cse.irf14.query.Query;

public class TfIdfModel implements ScoreModel {

	@Override
	public Query calculateScore(Query query) {
		Map<Long, DocMetaData> docMap = query.getDocumentMap();
		
		/* Cycle through each doc found */
		for (long docId: docMap.keySet()) {
			DocMetaData docMetadata = docMap.get(docId);
			
			/* Map of all query-terms */
			Map<Long, IndexType> queryTermList = Query.getQueryTermList();
			
			for (Long termId: queryTermList.keySet()) {
				IndexType zone = queryTermList.get(termId);
				int tf = 1;
				IndexReader df = IndexesAndDictionaries.getIndexByType(zone);
			}
			
			/* Map of all terms found */
			Map<Long, TermMetadataForThisDoc> termMap = docMetadata.getTermMetaDataMap();
			
			
		}
		
		return query;
	}
}