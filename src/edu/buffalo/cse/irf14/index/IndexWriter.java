package edu.buffalo.cse.irf14.index;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.buffalo.cse.irf14.analysis.Analyzer;
import edu.buffalo.cse.irf14.analysis.AnalyzerFactory;
import edu.buffalo.cse.irf14.analysis.Token;
import edu.buffalo.cse.irf14.analysis.TokenStream;
import edu.buffalo.cse.irf14.analysis.Tokenizer;
import edu.buffalo.cse.irf14.analysis.TokenizerException;
import edu.buffalo.cse.irf14.analysis.util.DictionaryMetadata;
import edu.buffalo.cse.irf14.analysis.util.TermMetadataForThisDoc;
import edu.buffalo.cse.irf14.document.Document;
import edu.buffalo.cse.irf14.document.FieldNames;

/**
 * @author nikhillo Class responsible for writing indexes to disk
 */
public class IndexWriter {

	/* Data structures for dictionaries and indexes */
	Map<Long, DocumentDictionaryEntry> documentDictionary = new HashMap<Long, DocumentDictionaryEntry>();
	Map<String, DictionaryMetadata> dictionary = new HashMap<String, DictionaryMetadata>();

	Map<Character, Map<Long, Map<Long, TermMetadataForThisDoc>>> termIndex;
	Map<Character, Map<Long, Map<Long, TermMetadataForThisDoc>>> categoryIndex;
	Map<Character, Map<Long, Map<Long, TermMetadataForThisDoc>>> authorIndex;
	Map<Character, Map<Long, Map<Long, TermMetadataForThisDoc>>> placesIndex;

	/* File readers/writers */
	String indexDirectory;
	static String fileExtension = ".txt";
	public static String termIndexFileNamePrefix = File.separator + "term_index_";
	public static String categoryIndexFileNamePrefix = File.separator + "category_index_";
	public static String authorIndexFileNamePrefix = File.separator + "author_index_";
	public static String placeIndexFileNamePrefix = File.separator + "place_index_";

	public static String dictionaryFileName = File.separator + "dictionary" + fileExtension;
	public static String docuDictFileName = File.separator + "dictionaryOfDocs" + fileExtension;

	/* File writer for document dictionary */
	ObjectOutputStream docuDictionaryWriter = null;

	/* Booster scores and multipliers */
	final int BOOSTER_MULTIPLIER = 1;
	final int TITLE_BOOSTER = 3;
	final int AUTHOR_BOOSTER = 2;
	final int CONTENT_BOOSTER = 1;
	final int PLACES_BOOSTER = 2;
	final int CATEGORY_BOOSTER = 3;

	/* Miscellaneous declarations */
	long docIdCounter, termDictIdCounter;
	public static long lengthofCorpus;

	/* List to keep track of docs covered */
	public static List<String> docsCovered = new ArrayList<String>();

	/**
	 * Default constructor
	 * 
	 * @param indexDir
	 *             : The root directory to be sued for indexing
	 */
	public IndexWriter(String indexDir) {
		this.indexDirectory = indexDir;
		// listOfWriters = new HashMap<Character, ObjectOutputStream>();
		termIndex = new HashMap<Character, Map<Long, Map<Long, TermMetadataForThisDoc>>>();
		categoryIndex = new HashMap<Character, Map<Long, Map<Long, TermMetadataForThisDoc>>>();
		authorIndex = new HashMap<Character, Map<Long, Map<Long, TermMetadataForThisDoc>>>();
		placesIndex = new HashMap<Character, Map<Long, Map<Long, TermMetadataForThisDoc>>>();

		termDictIdCounter = 0;
		docIdCounter = 0;

		// writeTime = 0.0f;
	}

	/**
	 * Method to add the given Document to the index This method should take
	 * care of reading the filed values, passing them through corresponding
	 * analyzers and then indexing the results for each indexable field within
	 * the document.
	 * 
	 * @param doc
	 *             : The Document to be added
	 * @throws IndexerException
	 *              : In case any error occurs
	 */
	public void addDocument(Document doc) throws IndexerException {
		AnalyzerFactory analyzerFactory = new AnalyzerFactory();

		try {
			Map<Long, Integer> termStorage = new HashMap<Long, Integer>();
			FieldNames fieldName = null;
			boolean duplicateDoc = false;
			Tokenizer tokenizer = new Tokenizer();
			String fieldText = null;

			String documentName = doc.getField(FieldNames.FILEID)[0];
			/* Remove file extension, if present */
			if (documentName.indexOf(".") > 0) {
				documentName = documentName.substring(0, documentName.indexOf("."));
			}

			long documentDictId = docIdCounter;

			DocumentDictionaryEntry documentDictionaryEntry = new DocumentDictionaryEntry();

			if (docsCovered.contains(documentName)) {
				duplicateDoc = true;

				/* Find the original doc's ID */
				for (long key : documentDictionary.keySet()) {
					if (documentDictionary.get(key).getDocumentName().equals(documentName)) {
						documentDictId = key;
						break;
					}
				}
			}

			if (!duplicateDoc) {

				/* Building the TERM index */
				List<FieldNames> fieldNameList = new ArrayList<FieldNames>();
				fieldNameList.add(FieldNames.NEWSDATE);
				fieldNameList.add(FieldNames.TITLE);
				fieldNameList.add(FieldNames.CONTENT);
				int tokenCounter = 0;

				for (FieldNames fieldNameForTermIndex : fieldNameList) {
					fieldName = fieldNameForTermIndex;
					if (doc.getField(fieldName) != null) {

						/*- Find the OFFSET for this field, to be used for storing term positions */

						List<String> termTrackerForThisDoc = new ArrayList<String>();
						fieldText = doc.getField(fieldNameForTermIndex)[0];

						/*- If entire title is in caps, convert to lower case */
						if (fieldNameForTermIndex.equals(FieldNames.TITLE) && fieldText.toUpperCase().equals(fieldText)) {
							documentDictionaryEntry.setTitle(fieldText);
							fieldText = fieldText.toLowerCase();
						}

						TokenStream tokenstream = tokenizer.consume(fieldText);

						Analyzer analyzer = analyzerFactory.getAnalyzerForField(fieldNameForTermIndex, tokenstream);
						analyzer.processThroughFilters();
						tokenstream = analyzer.getStream();

						/* Transfer tokenstream into the dictionary */
						if (fieldNameForTermIndex.equals(FieldNames.TITLE) || fieldNameForTermIndex.equals(FieldNames.NEWSDATE) || fieldNameForTermIndex.equals(FieldNames.CONTENT)) {
							tokenstream.reset();

							while (tokenstream.hasNext()) {
								Token token = tokenstream.next();
								Long termId = termDictIdCounter;

								/* Add to doc's term-storage */
								if (termStorage.containsKey(termId)) {
									termStorage.put(termId, termStorage.get(termId) + 1);
								} else {
									termStorage.put(termId, 1);
								}

								/*- Increment the df counter only if this term 
								 * is occuring in the doc for the first time */
								if (termTrackerForThisDoc.contains(token.getTermText())) {
									termTrackerForThisDoc.add(token.getTermText());
								} else {
									/*- Check if term dictionary already contains the term. If yes, get the
									 * ID. If not, add the term to the dictionary */
									if (dictionary.containsKey(token.getTermText())) {
										termId = dictionary.get(token.getTermText()).getTermId();
										/*- Increase overall term frequency in term-dictionary */
										dictionary.get(token.getTermText()).setFrequency(dictionary.get(token.getTermText()).getFrequency() + 1);

									} else {
										DictionaryMetadata dictionaryMetadata = new DictionaryMetadata(termDictIdCounter++, 1);
										dictionary.put(token.getTermText(), dictionaryMetadata);
									}
								}

								/*- Set booster-score and frequency
								 * (relevant to this doc) */
								int boosterScore = BOOSTER_MULTIPLIER * (fieldNameForTermIndex.equals(FieldNames.TITLE) ? TITLE_BOOSTER : (fieldNameForTermIndex.equals(FieldNames.CONTENT) ? CONTENT_BOOSTER : 1));

								/*- Put in the corresponding alphabet-index */
								Map<Long, Map<Long, TermMetadataForThisDoc>> termIndexForThisAlphabet;
								char firstChar = token.getTermText().toLowerCase().charAt(0);
								if (termIndex.containsKey(firstChar)) {
									termIndexForThisAlphabet = termIndex.get(firstChar);
								} else {
									termIndexForThisAlphabet = new HashMap<Long, Map<Long, TermMetadataForThisDoc>>();

									if (firstChar >= 'a' && firstChar <= 'z') {
										termIndex.put(firstChar, termIndexForThisAlphabet);
									} else {
										termIndex.put('_', termIndexForThisAlphabet);
									}
								}

								/* Put in the term index */
								Map<Long, TermMetadataForThisDoc> termIndexForThisDoc;
								if (termIndexForThisAlphabet.containsKey(termId)) {
									termIndexForThisDoc = termIndexForThisAlphabet.get(termId);
								} else {
									termIndexForThisDoc = new HashMap<Long, TermMetadataForThisDoc>();
									termIndexForThisAlphabet.put(termId, termIndexForThisDoc);
								}

								/* For the doc */
								TermMetadataForThisDoc termMetadataForThisDoc = null;
								if (termIndexForThisDoc.containsKey(documentDictId)) {
									termMetadataForThisDoc = termIndexForThisDoc.get(documentDictId);
									termMetadataForThisDoc.setTermFrequency(termMetadataForThisDoc.getTermFrequency() + 1);
									termMetadataForThisDoc.setBoosterScore(termMetadataForThisDoc.getBoosterScore() + boosterScore);
									termMetadataForThisDoc.addPositionToList(tokenCounter);
								} else {
									termMetadataForThisDoc = new TermMetadataForThisDoc(1, boosterScore, token.getTermText().charAt(0), tokenCounter);
									termIndexForThisDoc.put(documentDictId, termMetadataForThisDoc);
								}
								tokenCounter++;
							}
						}
					}
				}

				/* Building the AUTHOR index */
				fieldNameList = new ArrayList<FieldNames>();
				fieldNameList.add(FieldNames.AUTHOR);
				fieldNameList.add(FieldNames.AUTHORORG);
				tokenCounter = 0;

				for (FieldNames fieldNameForTermIndex : fieldNameList) {
					fieldName = fieldNameForTermIndex;

					if (doc.getField(fieldName) != null) {

						
						List<String> termTrackerForThisDoc = new ArrayList<String>();
						fieldText = doc.getField(fieldName)[0];
						TokenStream tokenstream = tokenizer.consume(fieldText);

						Analyzer analyzer = analyzerFactory.getAnalyzerForField(fieldName, tokenstream);
						analyzer.processThroughFilters();
						tokenstream = analyzer.getStream();

						/* Transfer tokenstream into the dictionary */
						tokenstream.reset();
						while (tokenstream.hasNext()) {
							Token token = tokenstream.next();
							Long termId = termDictIdCounter;

							/* Add to doc's term-storage */
							if (termStorage.containsKey(termId)) {
								termStorage.put(termId, termStorage.get(termId) + 1);
							} else {
								termStorage.put(termId, 1);
							}

							/*- Increment the df counter only if this term 
							 * is occuring in the doc for the first time */
							if (termTrackerForThisDoc.contains(token.getTermText())) {
								termTrackerForThisDoc.add(token.getTermText());
							} else {

								/*- Check if the dictionary already contains
								 * the term. If yes, get the ID. If not, add
								 * the term to the dictionary */
								if (dictionary.containsKey(token.getTermText())) {
									termId = dictionary.get(token.getTermText()).getTermId();
									/*- Increase overall term frequency in
									 * term-dictionary */
									dictionary.get(token.getTermText()).setFrequency(dictionary.get(token.getTermText()).getFrequency() + 1);

								} else {
									DictionaryMetadata dictionaryMetadata = new DictionaryMetadata(termDictIdCounter++, 1);
									dictionary.put(token.getTermText(), dictionaryMetadata);
								}
							}

							/*- Set booster-score and frequency (relevant to
							 * this doc) */
							int boosterScore = BOOSTER_MULTIPLIER * AUTHOR_BOOSTER;

							/* Put in the corresponding alphabet-index */
							Map<Long, Map<Long, TermMetadataForThisDoc>> termIndexForThisAlphabet;
							char firstChar = token.getTermText().toLowerCase().charAt(0);
							if (authorIndex.containsKey(firstChar)) {
								termIndexForThisAlphabet = authorIndex.get(firstChar);
							} else {
								termIndexForThisAlphabet = new HashMap<Long, Map<Long, TermMetadataForThisDoc>>();

								if (firstChar >= 'a' && firstChar <= 'z') {
									authorIndex.put(firstChar, termIndexForThisAlphabet);
								} else {
									authorIndex.put('_', termIndexForThisAlphabet);
								}
							}

							/* Put in the term index */
							Map<Long, TermMetadataForThisDoc> termIndexForThisDoc;
							if (termIndexForThisAlphabet.containsKey(termId)) {
								termIndexForThisDoc = termIndexForThisAlphabet.get(termId);
							} else {
								termIndexForThisDoc = new HashMap<Long, TermMetadataForThisDoc>();
								termIndexForThisAlphabet.put(termId, termIndexForThisDoc);
							}

							/* For the doc */
							TermMetadataForThisDoc termMetadataForThisDoc = null;
							if (termIndexForThisDoc.containsKey(documentDictId)) {
								termMetadataForThisDoc = termIndexForThisDoc.get(documentDictId);
								termMetadataForThisDoc.setTermFrequency(termMetadataForThisDoc.getTermFrequency() + 1);
								termMetadataForThisDoc.setBoosterScore(termMetadataForThisDoc.getBoosterScore() + boosterScore);
								termMetadataForThisDoc.addPositionToList(tokenCounter);
							} else {
								termMetadataForThisDoc = new TermMetadataForThisDoc(1, boosterScore, token.getTermText().charAt(0), tokenCounter);
								termIndexForThisDoc.put(documentDictId, termMetadataForThisDoc);
							}
							tokenCounter++;
						}
					}

					/* Building the PLACES index */
					fieldName = FieldNames.PLACE;
					if (doc.getField(fieldName) != null) {

						List<String> termTrackerForThisDoc = new ArrayList<String>();
						fieldText = doc.getField(fieldName)[0];
						TokenStream tokenstream = tokenizer.consume(fieldText);

						Analyzer analyzer = analyzerFactory.getAnalyzerForField(fieldName, tokenstream);
						analyzer.processThroughFilters();
						tokenstream = analyzer.getStream();

						/* Transfer tokenstream into the dictionary */
						tokenstream.reset();
						tokenCounter = 0;
						while (tokenstream.hasNext()) {
							Token token = tokenstream.next();
							Long termId = termDictIdCounter;

							/* Add to doc's term-storage */
							if (termStorage.containsKey(termId)) {
								termStorage.put(termId, termStorage.get(termId) + 1);
							} else {
								termStorage.put(termId, 1);
							}

							/*- Increment the df counter only if this term 
							 * is occuring in the doc for the first time */
							if (termTrackerForThisDoc.contains(token.getTermText())) {
								termTrackerForThisDoc.add(token.getTermText());
							} else {

								/*- Check if the dictionary already contains the
								 * term. If yes, get the ID. If not, add the term
								 * to the dictionary */
								if (dictionary.containsKey(token.getTermText())) {
									termId = dictionary.get(token.getTermText()).getTermId();
									/*- Increase overall term frequency in
									 * term-dictionary */
									dictionary.get(token.getTermText()).setFrequency(dictionary.get(token.getTermText()).getFrequency() + 1);

								} else {
									DictionaryMetadata dictionaryMetadata = new DictionaryMetadata(termDictIdCounter++, 1);
									dictionary.put(token.getTermText(), dictionaryMetadata);
								}
							}

							/*- Set booster-score and frequency (relevant to
							 * this doc) */
							int boosterScore = BOOSTER_MULTIPLIER * PLACES_BOOSTER;

							/* Put in the corresponding alphabet-index */
							Map<Long, Map<Long, TermMetadataForThisDoc>> termIndexForThisAlphabet;
							char firstChar = token.getTermText().toLowerCase().charAt(0);
							if (placesIndex.containsKey(firstChar)) {
								termIndexForThisAlphabet = placesIndex.get(firstChar);
							} else {
								termIndexForThisAlphabet = new HashMap<Long, Map<Long, TermMetadataForThisDoc>>();

								if (firstChar >= 'a' && firstChar <= 'z') {
									placesIndex.put(firstChar, termIndexForThisAlphabet);
								} else {
									placesIndex.put('_', termIndexForThisAlphabet);
								}
							}

							/* Put in the term index */
							Map<Long, TermMetadataForThisDoc> termIndexForThisDoc;
							if (termIndexForThisAlphabet.containsKey(termId)) {
								termIndexForThisDoc = termIndexForThisAlphabet.get(termId);
							} else {
								termIndexForThisDoc = new HashMap<Long, TermMetadataForThisDoc>();
								termIndexForThisAlphabet.put(termId, termIndexForThisDoc);
							}

							/* For the doc */
							TermMetadataForThisDoc termMetadataForThisDoc = null;
							if (termIndexForThisDoc.containsKey(documentDictId)) {
								termMetadataForThisDoc = termIndexForThisDoc.get(documentDictId);
								termMetadataForThisDoc.setTermFrequency(termMetadataForThisDoc.getTermFrequency() + 1);
								termMetadataForThisDoc.setBoosterScore(termMetadataForThisDoc.getBoosterScore() + boosterScore);
								termMetadataForThisDoc.addPositionToList(tokenCounter);
							} else {
								termMetadataForThisDoc = new TermMetadataForThisDoc(1, boosterScore, token.getTermText().charAt(0), tokenCounter);
								termIndexForThisDoc.put(documentDictId, termMetadataForThisDoc);
							}
							tokenCounter++;
						}
					}

					/* Increment the counter for document IDs */
					docIdCounter++;

				}
			}

			/*- If this is a duplicate document, we only want to update the category dictionary and index */

			/* MODIFY the CATEGORY index */
			fieldName = FieldNames.CATEGORY;
			if (doc.getField(fieldName) != null) {

				List<String> termTrackerForThisDoc = new ArrayList<String>();
				fieldText = doc.getField(fieldName)[0];
				TokenStream tokenstream = tokenizer.consume(fieldText);

				Analyzer analyzer = analyzerFactory.getAnalyzerForField(fieldName, tokenstream);
				analyzer.processThroughFilters();
				tokenstream = analyzer.getStream();

				/* Transfer tokenstream into the dictionary */
				tokenstream.reset();
				int tokenCounter = 0;
				while (tokenstream.hasNext()) {
					Token token = tokenstream.next();
					Long termId = termDictIdCounter;

					/* Add to doc's term-storage */
					if (termStorage.containsKey(termId)) {
						termStorage.put(termId, termStorage.get(termId) + 1);
					} else {
						termStorage.put(termId, 1);
					}

					/*- Increment the df counter only if this term 
					 * is occuring in the doc for the first time */
					if (termTrackerForThisDoc.contains(token.getTermText())) {
						termTrackerForThisDoc.add(token.getTermText());
					} else {
						/*- Check if the dictionary already contains the
						 * term. If yes, get the ID. If not, add the term
						 * to the dictionary */
						if (dictionary.containsKey(token.getTermText())) {
							termId = dictionary.get(token.getTermText()).getTermId();
							/*- Increase overall term frequency in
							 * term-dictionary */
							dictionary.get(token.getTermText()).setFrequency(dictionary.get(token.getTermText()).getFrequency() + 1);

						} else {
							DictionaryMetadata dictionaryMetadata = new DictionaryMetadata(termDictIdCounter++, 1);
							dictionary.put(token.getTermText(), dictionaryMetadata);
						}
					}

					/*- Set booster-score and frequency (relevant to this doc) */
					int boosterScore = BOOSTER_MULTIPLIER * CATEGORY_BOOSTER;

					/* Put in the corresponding alphabet-index */
					Map<Long, Map<Long, TermMetadataForThisDoc>> termIndexForThisAlphabet;
					char firstChar = token.getTermText().toLowerCase().charAt(0);
					if (categoryIndex.containsKey(firstChar)) {
						termIndexForThisAlphabet = categoryIndex.get(firstChar);
					} else {
						termIndexForThisAlphabet = new HashMap<Long, Map<Long, TermMetadataForThisDoc>>();

						if (firstChar >= 'a' && firstChar <= 'z') {
							categoryIndex.put(firstChar, termIndexForThisAlphabet);
						} else {
							categoryIndex.put('_', termIndexForThisAlphabet);
						}
					}

					/* Put in the term index */
					Map<Long, TermMetadataForThisDoc> termIndexForThisDoc;
					if (termIndexForThisAlphabet.containsKey(termId)) {
						termIndexForThisDoc = termIndexForThisAlphabet.get(termId);
					} else {
						termIndexForThisDoc = new HashMap<Long, TermMetadataForThisDoc>();
						termIndexForThisAlphabet.put(termId, termIndexForThisDoc);
					}

					/* For the doc */
					TermMetadataForThisDoc termMetadataForThisDoc = null;
					if (termIndexForThisDoc.containsKey(documentDictId)) {
						termMetadataForThisDoc = termIndexForThisDoc.get(documentDictId);
						termMetadataForThisDoc.setTermFrequency(termMetadataForThisDoc.getTermFrequency() + 1);
						termMetadataForThisDoc.setBoosterScore(termMetadataForThisDoc.getBoosterScore() + boosterScore);
						termMetadataForThisDoc.addPositionToList(tokenCounter);
					} else {
						termMetadataForThisDoc = new TermMetadataForThisDoc(1, boosterScore, token.getTermText().charAt(0), tokenCounter);
						termIndexForThisDoc.put(documentDictId, termMetadataForThisDoc);
					}
					tokenCounter++;
				}
			}

			if (!duplicateDoc) {
				/* Calculate the Euclidean score */
				double euclideanWeight = 0.0;
				int numOfTokensInDocument = 0;
				for (long termId : termStorage.keySet()) {
					int freq = termStorage.get(termId);
					double tf = calculateTfWeight(freq);
					euclideanWeight += tf * tf;
					numOfTokensInDocument += freq;
				}
				lengthofCorpus += numOfTokensInDocument;
				euclideanWeight = Math.sqrt(euclideanWeight);
				documentDictionaryEntry.setEuclideanWeight(euclideanWeight);
				documentDictionaryEntry.setDocumentName(documentName);
				documentDictionaryEntry.setNumOfTokensInDocument(numOfTokensInDocument);
				documentDictionary.put(documentDictId, documentDictionaryEntry);

				/* Add doc to list of covered docs */
				docsCovered.add(documentName);
			}
		} catch (TokenizerException e) {
			System.out.println("Exception caught");
			e.printStackTrace();
		}
	}

	private double calculateTfWeight(double value) {
		double weight = 0.0;
		if (value > 0)
			weight = 1.0 + (double) Math.log10(value);
		return weight;
	}

	/* Write the document-dictionary to disk */
	private void writeDocumentDictionary() throws IOException {
		if (documentDictionary != null) {
			File docuDictFile = new File(indexDirectory + docuDictFileName);
			if (docuDictFile.exists())
				docuDictFile.delete();
			docuDictFile.createNewFile();

			/* Store N (total no. of docs) */
			DocumentDictionary docDictionary = new DocumentDictionary(docIdCounter, documentDictionary);
			docDictionary.setAvgLenOfDocInCorpus(lengthofCorpus / docIdCounter);

			docuDictionaryWriter = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(docuDictFile, true)));
			docuDictionaryWriter.writeObject(docDictionary);
		}
	}

	/* Write the term-dictionary to disk */
	private void writeDictionaryToFile(String dictFileName, Map<String, DictionaryMetadata> dictionary) throws IOException {
		if (dictionary != null) {
			File termDictFile = new File(indexDirectory + dictFileName);
			if (termDictFile.exists())
				termDictFile.delete();
			termDictFile.createNewFile();

			ObjectOutputStream termDictionaryWriter = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(termDictFile, true)));
			if (termDictionaryWriter != null) {
				/* Write the dictionary */
				termDictionaryWriter.writeObject(dictionary);

				/* Close the dictionary writer */
				termDictionaryWriter.close();
			}
		}
	}

	private void writeIndexToFile(String indexFileNamePrefix, Map<Character, Map<Long, Map<Long, TermMetadataForThisDoc>>> index) throws IOException {
		/* For indexes: Create the files */
		File termIndexFile;
		for (char i = 'a'; i <= 'z'; i++) {
			termIndexFile = new File(indexDirectory + indexFileNamePrefix + i + fileExtension);
			if (termIndexFile.exists()) {
				termIndexFile.delete();
			}
			termIndexFile.createNewFile();
			ObjectOutputStream writer = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(termIndexFile, true)));
			if (writer != null) {
				writer.writeObject(index.get(i));
				writer.close();
			}
		}

		/* Create the last file for miscellaneous characters */
		termIndexFile = new File(indexDirectory + indexFileNamePrefix + "_" + fileExtension);
		if (termIndexFile.exists()) {
			termIndexFile.delete();
		}
		termIndexFile.createNewFile();
		ObjectOutputStream writer = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(termIndexFile, true)));
		if (writer != null) {
			writer.writeObject(index.get('_'));
			writer.close();
		}
	}

	/**
	 * Method that indicates that all open resources must be closed and cleaned
	 * and that the entire indexing operation has been completed.
	 * 
	 * @throws IndexerException
	 *              : In case any error occurs
	 */
	public void close() throws IndexerException {

		try {
			// startTime = new Date().getTime();

			/* Write the document dictionaries to files */
			writeDocumentDictionary();

			/* Write the term dictionaries */
			writeDictionaryToFile(dictionaryFileName, dictionary);

			/* Write the indexes to files */
			writeIndexToFile(termIndexFileNamePrefix, termIndex);
			writeIndexToFile(categoryIndexFileNamePrefix, categoryIndex);
			writeIndexToFile(authorIndexFileNamePrefix, authorIndex);
			writeIndexToFile(placeIndexFileNamePrefix, placesIndex);

			// writeTime += (new Date().getTime() - startTime) / 1000.0;
		} catch (IOException e) {
			e.printStackTrace();
			throw new IndexerException("IndexerException occured while writing to indexer files");
		}

		// System.out.println("\nTime for filtering ==> " + analyzerTime);
		// System.out.println("Time for writing ==> " + writeTime);

		try {
			if (docuDictionaryWriter != null) {
				docuDictionaryWriter.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new IndexerException("IndexerException occured while writing to indexer files");
		}
	}
}