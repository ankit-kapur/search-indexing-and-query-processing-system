package edu.buffalo.cse.irf14.query;

import java.util.HashMap;
import java.util.Map;

import edu.buffalo.cse.irf14.analysis.Analyzer;
import edu.buffalo.cse.irf14.analysis.AnalyzerFactory;
import edu.buffalo.cse.irf14.analysis.Token;
import edu.buffalo.cse.irf14.analysis.TokenStream;
import edu.buffalo.cse.irf14.analysis.Tokenizer;
import edu.buffalo.cse.irf14.analysis.TokenizerException;
import edu.buffalo.cse.irf14.analysis.util.DictionaryMetadata;
import edu.buffalo.cse.irf14.analysis.util.TermMetadataForThisDoc;
import edu.buffalo.cse.irf14.document.FieldNames;
import edu.buffalo.cse.irf14.index.IndexReader;
import edu.buffalo.cse.irf14.index.IndexType;

public class Term extends Expression {

	private String queryTerm;
	boolean notFlag = false;
	boolean barFlag = false;
	StringBuffer sbBuffer = new StringBuffer();

	IndexType zone;
	String termText;

	public Term(String string) {
		queryTerm = string;
		// TODO Auto-generated constructor stub
	}

	public String getQueryTerm() {
		return queryTerm;
	}

	public void setQueryTerm(String queryTerm) {
		this.queryTerm = queryTerm;
	}

	@Override
	public String toString() {
		if (queryTerm.contains(":") || queryTerm.equals("AND") || queryTerm.equals("OR") || queryTerm.equals("NOT") && !notFlag) {
			if (queryTerm.contains("(") || queryTerm.contains("[")) {
				queryTerm = queryTerm.replace("(", "[");
				barFlag = true;
			}
			if (queryTerm.contains(")")) {
				queryTerm = queryTerm.replace(")", "]");
				barFlag = true;
			}
			if (notFlag && !barFlag) {
				queryTerm = "<Term:" + queryTerm + ">";
				notFlag = false;
			}
			if (notFlag && barFlag) {
				StringBuffer sbBuffer = new StringBuffer();
				queryTerm = sbBuffer.append("<").append(queryTerm).toString();
				queryTerm = sbBuffer.insert(queryTerm.indexOf("]"), ">").toString();
			}

			return queryTerm;
		} else {
			if (queryTerm.contains("(") || queryTerm.contains("[")) {
				queryTerm = queryTerm.replace("(", "[");
				barFlag = true;
			}
			if (queryTerm.contains(")")) {
				queryTerm = queryTerm.replace(")", "]");
				barFlag = true;
			}
			if (notFlag && !barFlag) {
				queryTerm = "<Term:" + queryTerm + ">";
			}
			if (notFlag && barFlag) {
				StringBuffer sbBuffer = new StringBuffer();
				queryTerm = sbBuffer.append("<Term:").append(queryTerm).toString();
				queryTerm = sbBuffer.insert(queryTerm.indexOf("]"), ">").toString();
			}
			if (barFlag && !notFlag) {
				sbBuffer.append(queryTerm);
				queryTerm = sbBuffer.insert(sbBuffer.lastIndexOf("[") + 1, "Term:").toString();

			}
			if (!notFlag && !barFlag) {
				queryTerm = "Term:" + queryTerm;
			}

		}
		return queryTerm;

	}

	@Override
	public Map<Long, DocMetaData> getPostings() {
		Map<Long, DocMetaData> docMap = new HashMap<Long, DocMetaData>();
		String indexType = null;
		IndexType indexTypeObject = null;
		String filteredQueryTerm = null;
		Long termId = null;
		Character firstChar;
		AnalyzerFactory analyzerFactory = AnalyzerFactory.getInstance();
		Analyzer analyzer = null;
		if (queryTerm.contains(":")) {
			if (queryTerm.contains("(")) {
				queryTerm = queryTerm.replace("(", "");
			}
			if (queryTerm.contains(")")) {
				queryTerm = queryTerm.replace(")", "");
			}

			indexType = queryTerm.substring(0, queryTerm.indexOf(":")).toLowerCase();
			String termText = queryTerm.substring(queryTerm.indexOf(":") + 1, queryTerm.length());

			// Token token = new Token();
			// token.setTermText(termText);
			// TokenStream tokenStream = new TokenStream();
			// tokenStream.addTokenToStream(token);

			Tokenizer tokenizer = new Tokenizer();
			TokenStream tokenStream = null;
			try {
				tokenStream = tokenizer.consume(termText);
			} catch (TokenizerException e) {
				e.printStackTrace();
			}

			if (indexType.equals("category")) {
				indexTypeObject = IndexType.CATEGORY;
				analyzer = analyzerFactory.getAnalyzerForField(FieldNames.CATEGORY, tokenStream);

			} else if (indexType.equals("author")) {
				indexTypeObject = IndexType.AUTHOR;
				analyzer = analyzerFactory.getAnalyzerForField(FieldNames.AUTHOR, tokenStream);
			} else if (indexType.equals("place")) {
				indexTypeObject = IndexType.PLACE;
				analyzer = analyzerFactory.getAnalyzerForField(FieldNames.PLACE, tokenStream);
			} else {
				indexTypeObject = IndexType.TERM;
				analyzer = analyzerFactory.getAnalyzerForField(FieldNames.CONTENT, tokenStream);
			}

			analyzer.processThroughFilters();
			tokenStream = analyzer.getStream();
			Token token = null;
			if (tokenStream.hasNext()) {
				token = tokenStream.next();
			}
			filteredQueryTerm = token.getTermText();
		} else {
			indexType = "term";
			indexTypeObject = IndexType.TERM;
			if (queryTerm.contains("(")) {
				queryTerm = queryTerm.replace("(", "");
			}
			if (queryTerm.contains(")")) {
				queryTerm = queryTerm.replace(")", "");
			}

			Token token = new Token();
			token.setTermText(queryTerm);
			TokenStream tokenStream = new TokenStream();
			tokenStream.addTokenToStream(token);

			analyzer = analyzerFactory.getAnalyzerForField(FieldNames.CONTENT, tokenStream);
			analyzer.processThroughFilters();

			tokenStream = analyzer.getStream();
			token = tokenStream.getCurrent();
			filteredQueryTerm = token.getTermText();
		}

		termText = filteredQueryTerm;
		zone = QueryUtils.getZoneTypeByZoneName(indexType);

		IndexReader indexReader = IndexesAndDictionaries.getIndexByType(indexTypeObject);
		if (indexReader != null) {
			Map<String, DictionaryMetadata> termDictionary = indexReader.termDictionary;
			DictionaryMetadata dictionaryMetadata = termDictionary.get(termText);
			if (dictionaryMetadata != null) {
				termId = dictionaryMetadata.getTermId();
				// /////// TODO NULL CHECK

				firstChar = termText.toLowerCase().charAt(0);
				Map<Character, Map<Long, Map<Long, TermMetadataForThisDoc>>> index = indexReader.index;
				Map<Long, TermMetadataForThisDoc> postingsMap = index.get(firstChar).get(termId);
				if (postingsMap != null) {
					for (Long docId : postingsMap.keySet()) {
						DocMetaData docMetaData = null;
						if (docMap.containsKey(docId)) {
							docMetaData = docMap.get(docId);
						} else {
							docMetaData = new DocMetaData();
						}

						/* Set the zone in the term-metadata map */
						Map<Long, TermMetadataForThisDoc> termMetaDataMap = docMetaData.getTermMetaDataMap();
						TermMetadataForThisDoc termMetadata = postingsMap.get(docId);
						termMetadata.setZone(zone);
						termMetaDataMap.put(termId, termMetadata);
						docMetaData.setTermMetaDataMap(termMetaDataMap);

						/*- Add query-term to the list that holds every
						 * term in the query
						 */
						if (!Query.getQueryTermList().keySet().contains(termId))
							Query.addQueryTermToList(termId, zone);

						docMap.put(docId, docMetaData);
					}
				}
			}
		}
		/*
		 * Map<Long, DocumentDictionaryEntry>
		 * map2=indexReader.getDocumentDictionary(); DocumentDictionaryEntry
		 * documentDictionaryEntry; for(Long docId:docMap.keySet()) {
		 * documentDictionaryEntry=map2.get(docId);
		 * System.out.println("Id-->"+
		 * docId+"Name-->"+documentDictionaryEntry.getDocumentName()); }
		 */
		return docMap;
	}
}