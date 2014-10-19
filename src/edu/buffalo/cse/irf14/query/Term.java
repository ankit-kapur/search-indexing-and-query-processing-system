package edu.buffalo.cse.irf14.query;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import edu.buffalo.cse.irf14.analysis.Analyzer;
import edu.buffalo.cse.irf14.analysis.AnalyzerFactory;
import edu.buffalo.cse.irf14.analysis.Token;
import edu.buffalo.cse.irf14.analysis.TokenStream;
import edu.buffalo.cse.irf14.analysis.util.DictionaryMetadata;
import edu.buffalo.cse.irf14.analysis.util.TermMetadataForThisDoc;
import edu.buffalo.cse.irf14.document.FieldNames;
import edu.buffalo.cse.irf14.index.IndexReader;
import edu.buffalo.cse.irf14.index.IndexType;

public class Term implements Expression {

	static Map<Long, DocMetaData> finalDocMap = new HashMap<Long, DocMetaData>();
	private String queryTerm;
	boolean notFlag = false;
	boolean barFlag = false;
	StringBuffer sbBuffer = new StringBuffer();

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
		if (queryTerm.contains(":") || queryTerm.equals("AND")
				|| queryTerm.equals("OR") || queryTerm.equals("NOT")
				&& !notFlag) {
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
				queryTerm = sbBuffer.insert(queryTerm.indexOf("]"), ">")
						.toString();
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
				queryTerm = sbBuffer.append("<Term:").append(queryTerm)
						.toString();
				queryTerm = sbBuffer.insert(queryTerm.indexOf("]"), ">")
						.toString();
			}
			if (barFlag && !notFlag) {
				sbBuffer.append(queryTerm);
				queryTerm = sbBuffer.insert(sbBuffer.lastIndexOf("[") + 1,
						"Term:").toString();

			}
			if (!notFlag && !barFlag) {
				queryTerm = "Term:" + queryTerm;
			}

		}
		return queryTerm;

	}

	@Override
	public Map<Long, DocMetaData> getPostings() {
		// TODO Auto-generated method stub
		String indexType = null;
		IndexType IndexTypeObject = null;
		String filteredQueryTerm = null;
		Long termId;
		Character firstChar;
		Token token = new Token();
		token.setTermText(queryTerm);
		TokenStream tokenStream = new TokenStream();
		tokenStream.addTokenToStream(token);
		AnalyzerFactory analyzerFactory = AnalyzerFactory.getInstance();
		Analyzer analyzer = null;
		if (queryTerm.contains(":")) {
			if (queryTerm.contains("(")) {
				queryTerm = queryTerm.replace("(", "");
			}
			if (queryTerm.contains(")")) {
				queryTerm = queryTerm.replace(")", "");
			}
			indexType = queryTerm.substring(0, queryTerm.indexOf(":"));

			if (indexType.equals("Category")) {
				IndexTypeObject = IndexType.CATEGORY;
				analyzer = analyzerFactory.getAnalyzerForField(
						FieldNames.CATEGORY, tokenStream);

			} else if (indexType.equals("Author")) {
				IndexTypeObject = IndexType.AUTHOR;
				analyzer = analyzerFactory.getAnalyzerForField(
						FieldNames.AUTHOR, tokenStream);
			} else if (indexType.equals("Place")) {
				IndexTypeObject = IndexType.PLACE;
				analyzer = analyzerFactory.getAnalyzerForField(
						FieldNames.PLACE, tokenStream);
			} else {
				IndexTypeObject = IndexType.TERM;
				analyzer = analyzerFactory.getAnalyzerForField(
						FieldNames.CONTENT, tokenStream);
			}
			analyzer.processThroughFilters();
			tokenStream = analyzer.getStream();
			token = tokenStream.getCurrent();
			filteredQueryTerm = token.getTermText();
		} else {
			IndexTypeObject = IndexType.TERM;
			if (queryTerm.contains("(")) {
				queryTerm = queryTerm.replace("(", "");
			}
			if (queryTerm.contains(")")) {
				queryTerm = queryTerm.replace(")", "");
			}
			analyzer = analyzerFactory.getAnalyzerForField(FieldNames.CONTENT,
					tokenStream);
			analyzer.processThroughFilters();
			tokenStream = analyzer.getStream();
			token = tokenStream.getCurrent();
			filteredQueryTerm = token.getTermText();
		}
		IndexReader indexReader = new IndexReader(
				System.getProperty("user.dir") + File.separator + "indexdir",
				IndexTypeObject);
		Map<String, DictionaryMetadata> termDictionary = indexReader.termDictionary;
		Map<Long, String> documentDictionary = indexReader.documentDictionary;
		DictionaryMetadata dictionaryMetadata = termDictionary
				.get(filteredQueryTerm);
		firstChar = filteredQueryTerm.toLowerCase().charAt(0);
		termId = dictionaryMetadata.getTermId();
		Map<Character, Map<Long, Map<Long, TermMetadataForThisDoc>>> index = indexReader.index;
		Map<Long, TermMetadataForThisDoc> postingsMap = index.get(firstChar)
				.get(termId);

		for (Long docId : postingsMap.keySet()) {
			DocMetaData docMetaData = null;
			if (finalDocMap.containsKey(docId)) {
				docMetaData = finalDocMap.get(docId);
			} else {
				docMetaData = new DocMetaData();
			}

			Map<Long, TermMetadataForThisDoc> termMetaDataMap = docMetaData
					.getTermMetaDataMap();
			termMetaDataMap.put(termId, postingsMap.get(docId));
			docMetaData.setTermMetaDataMap(termMetaDataMap);
			
			finalDocMap.put(docId, docMetaData);
		}
		return finalDocMap;
	}

}
