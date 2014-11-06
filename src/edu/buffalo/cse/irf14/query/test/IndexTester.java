package edu.buffalo.cse.irf14.query.test;

import java.io.File;
import java.util.Map;

import edu.buffalo.cse.irf14.analysis.util.DictionaryMetadata;
import edu.buffalo.cse.irf14.analysis.util.TermMetadataForThisDoc;
import edu.buffalo.cse.irf14.index.DocumentDictionaryEntry;
import edu.buffalo.cse.irf14.index.IndexReader;
import edu.buffalo.cse.irf14.index.IndexType;

public class IndexTester {

	public static void main(String[] args) {

		String indexDir = System.getProperty("user.dir") + File.separator + "indexdir";

//		IndexReader categoryIndex = new IndexReader(indexDir, IndexType.CATEGORY);
//		IndexReader placeIndex = new IndexReader(indexDir, IndexType.PLACE);
//		IndexReader authorIndex = new IndexReader(indexDir, IndexType.AUTHOR);
		IndexReader termIndex = new IndexReader(indexDir, IndexType.TERM);

		System.out.println("Total number of docs: " + termIndex.getCorpusSize());

		System.out.println("\nDoc dictionary...");
		Map<Long, DocumentDictionaryEntry> docDict = termIndex.getDocumentDictionary();
		for (long docId : docDict.keySet()) {
			System.out.println("ID: " + docId + " -> Docname: " + docDict.get(docId).getDocumentName());
		}

		System.out.println("\nTerm dictionary...");
		Map<String, DictionaryMetadata> termDict = termIndex.getTermDictionary();
		for (String term : termDict.keySet()) {
//			if (term.toLowerCase().startsWith("adob"))
				System.out.println(term + " -> ID: " + termDict.get(term).getTermId() + " -> freq: " + termDict.get(term).getFrequency());
		}

//		KgramIndex kgramIndex = categoryIndex.getKgramIndex();
//		Map<String, List<Long>> map = kgramIndex.getIndex();
//		Set<String> set = map.keySet();
//		Iterator<String> it = set.iterator();
//		while (it.hasNext()) {
//			String key = it.next();
//			System.out.print(key + " -> ");
//			List<Long> termIdList = kgramIndex.getIndex().get(key);
//			for (Long termId: termIdList) {
//				/* GET TERM STRING FROM TERM ID */
//				System.out.print(getTermById(termId, termDict) + "(" + termId +  "), ");
//			}
//			System.out.println();
//		}
		
//		System.out.println("\nCategory index...");
//		printIndex(categoryIndex, termDict, docDict);
//		System.out.println("\nAuthor index...");
//		printIndex(authorIndex, termDict, docDict);
//		System.out.println("\nPlace index...");
//		printIndex(placeIndex, termDict, docDict);
		System.out.println("\nTerm index...");
		printIndex(termIndex, termDict, docDict);

		/*- Display all info about the term such as postings list, positional
		 * indexes, term frequency */
		termIndex.getTermInformation();
	}

	private static void printIndex(IndexReader index, Map<String, DictionaryMetadata> termDict, Map<Long, DocumentDictionaryEntry> docDict) {
		Map<Character, Map<Long, Map<Long, TermMetadataForThisDoc>>> categIndex = index.getIndex();
		for (char c : categIndex.keySet()) {
			Map<Long, Map<Long, TermMetadataForThisDoc>> termIdMap = categIndex.get(c);
			if (termIdMap != null) {
				for (long termId : termIdMap.keySet()) {
					Map<Long, TermMetadataForThisDoc> docIdMap = termIdMap.get(termId);
					if (docIdMap != null) {
						for (long docId : docIdMap.keySet()) {
							TermMetadataForThisDoc termMetadata = docIdMap.get(docId);
							String termText = getTermById(termId, termDict);
//							if (termId == 6032 || termText.toLowerCase().startsWith("ado")) {
								String docName = null;
								DocumentDictionaryEntry meta = docDict.get(docId);
								if (meta != null) 
									docName = meta.getDocumentName();
								System.out.println(termText + " -> ID: " + termId + " -> doc: " + docName + " -> freq: " + (termMetadata != null ? termMetadata.getTermFrequency() : "null") + " -> pos: " + (termMetadata != null ? termMetadata.getPositions() : " "));
//							}
						}
					}
				}
			}
		}

	}

	private static String getTermById(long termId, Map<String, DictionaryMetadata> termDict) {
		String termText = null;
		for (String term : termDict.keySet()) {
			if (termDict.get(term).getTermId() == termId)
				termText = term;
		}
		return termText;
	}
}