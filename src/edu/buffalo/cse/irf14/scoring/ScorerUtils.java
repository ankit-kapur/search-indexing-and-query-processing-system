package edu.buffalo.cse.irf14.scoring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.buffalo.cse.irf14.analysis.util.DictionaryMetadata;
import edu.buffalo.cse.irf14.index.DocumentDictionaryEntry;
import edu.buffalo.cse.irf14.index.IndexType;
import edu.buffalo.cse.irf14.query.IndexesAndDictionaries;
import edu.buffalo.cse.irf14.query.Query;
import edu.buffalo.cse.irf14.query.QueryResults;

public class ScorerUtils {
	public static void generateSnippets(Query query) throws ScorerException {

		Map<Long, QueryResults> resultsMap = query.getResultsMap();
		Map<Long, DocumentDictionaryEntry> docDictionary = IndexesAndDictionaries.getIndexByType(IndexType.TERM).getDocumentDictionary();

		for (Long docId : resultsMap.keySet()) {
			int snippetLength = 100;
			String snippet = "";
			QueryResults queryResults = resultsMap.get(docId);
			String docName = docDictionary.get(docId).getDocumentName();
			String title = docDictionary.get(docId).getTitle();
			queryResults.setTitle(title);

			/* Read document contents */
			String docContents = IndexesAndDictionaries.readDocFromFlattenedDir(docName);
			docContents = docContents.replaceAll("[\\n\\r]", " ");

			List<Integer> matchesInDoc = new ArrayList<Integer>();
			for (long termId : Query.getQueryTermList().keySet()) {
				IndexType type = Query.getQueryTermList().get(termId);
				if (type != IndexType.CATEGORY) {
					String term = getTermById(termId, IndexesAndDictionaries.getIndexByType(IndexType.CATEGORY).getTermDictionary());

					Pattern pattern = Pattern.compile("(?i)" + term);
					Matcher matcher = pattern.matcher(docContents);
					while (matcher.find()) {
						matchesInDoc.add(matcher.start());
					}
				}
			}

			if (matchesInDoc.size() > 0) {
				/* Decide snippet length */
				if (matchesInDoc.size() > 4) {
					snippetLength = 30;
				} else {
					snippetLength = snippetLength / matchesInDoc.size();
				}

				for (int i = 0; i < matchesInDoc.size(); i++) {
					int matchPosition = matchesInDoc.get(i);
					String modifiedDocContents = docContents.substring(0, matchPosition) + "<b>";

					/* Find end of the word */
					int wordEndPosition = 0;
					Pattern pattern = Pattern.compile("[.,\\s]");
					Matcher matcher = pattern.matcher(docContents.substring(matchPosition));
					if (matcher.find()) {
						wordEndPosition = matchPosition + matcher.start();
					} else {
						/* Last word in the file */
						wordEndPosition = docContents.length() - 1;
					}

					String wordMatched = docContents.substring(matchPosition, wordEndPosition);
					modifiedDocContents += wordMatched + "</b>" + docContents.substring(wordEndPosition);

					/* Make snippet */
					int newMatchStartPosition = matchPosition - "<b>".length();
					if (newMatchStartPosition < 0)
						newMatchStartPosition = 0;
					int startPos, endPos;
					if (i == 0) {
						snippet = "... ";
					}
					startPos = newMatchStartPosition - snippetLength / 2;
					endPos = newMatchStartPosition + wordMatched.length() + snippetLength / 2;
					if (startPos < 0)
						startPos = newMatchStartPosition;
					if (endPos > modifiedDocContents.length() - 1)
						endPos = modifiedDocContents.length() - 1;

					/* Crawl to word boundaries */
					if (startPos > 0 && modifiedDocContents.substring(0, startPos).lastIndexOf(' ') > 0) {
						startPos = modifiedDocContents.substring(0, startPos).lastIndexOf(' ') + 1;
					}
					if (startPos == 363)
						System.out.println();
					if (endPos < modifiedDocContents.length() && modifiedDocContents.substring(endPos, modifiedDocContents.length()).indexOf(' ') > 0) {

						endPos = modifiedDocContents.substring(0, endPos).length() + modifiedDocContents.substring(endPos, modifiedDocContents.length()).indexOf(' ');
					}

					if (startPos >= endPos || snippetLength <= 0 || modifiedDocContents.length() <= 0) {
						System.out.println();
					}
					try {
						snippet += modifiedDocContents.substring(startPos, endPos) + "... ";
					} catch (StringIndexOutOfBoundsException e) {
						e.printStackTrace();
					}
				}

			} else {
				/* By default, take the snippet as the first few lines */
				if (docContents.indexOf(title) > 0) {
					String contentAfterTitle = docContents.substring(docContents.indexOf(title) + title.length());
					snippet = contentAfterTitle.substring(0, snippetLength);
				}
			}

			queryResults.setTitle(title);
			queryResults.setSnippet(snippet.replaceAll(" [\\s]+", " "));
		}
	}

	public static Query assignRanks(Query query) {
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

	public static String getTermById(Long termId, Map<String, DictionaryMetadata> termDictionary) {
		String term = null;
		for (String key : termDictionary.keySet()) {
			if (termDictionary.get(key).getTermId() == termId) {
				term = key;
			}
		}
		return term;
	}

	public static Query sortAccordingtoRank(Query query) {
		Map<Long, QueryResults> resultsMap = query.getResultsMap();
		List<Map.Entry<Long, QueryResults>> results = new ArrayList<Map.Entry<Long, QueryResults>>(resultsMap.entrySet());
		Collections.sort(results, new Comparator<Map.Entry<Long, QueryResults>>() {
			public int compare(Map.Entry<Long, QueryResults> o1, Map.Entry<Long, QueryResults> o2) {
				return o2.getValue().getRelevancyScore() > o1.getValue().getRelevancyScore() ? 1 : (o2.getValue().getRelevancyScore() < o1.getValue().getRelevancyScore() ? -1 : 0);
			}
		});
		Map<Long, QueryResults> sortedMap = new LinkedHashMap<Long, QueryResults>();
		java.util.Iterator<Entry<Long, QueryResults>> iterator = results.iterator();

		while (iterator.hasNext()) {
			Entry<Long, QueryResults> entry = iterator.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		query.setResultsMap(sortedMap);
		return query;
	}

	public static void clipResults(Query query) {
		Map<Long, QueryResults> resultsMap = query.getResultsMap();
		if (resultsMap.size() > 10) {
			for (long i = 10; i < resultsMap.size(); i++) {
				resultsMap.remove(i);
			}
		}
	}
}
