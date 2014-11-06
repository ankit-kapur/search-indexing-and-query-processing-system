package edu.buffalo.cse.irf14.query;

import java.io.File;
import java.util.Scanner;

import edu.buffalo.cse.irf14.index.IndexReader;
import edu.buffalo.cse.irf14.index.IndexType;
import edu.buffalo.cse.irf14.index.KgramIndex;
import edu.buffalo.cse.irf14.scoring.ScorerException;

public class IndexesAndDictionaries {

	static IndexReader placeIndex;
	static IndexReader authorIndex;
	static IndexReader categoryIndex;
	static IndexReader termIndex;

	static KgramIndex kgramIndex;

	static String corpusDir;

	public static void readIndexes(String indexDir) {
		placeIndex = new IndexReader(indexDir, IndexType.PLACE);
		authorIndex = new IndexReader(indexDir, IndexType.AUTHOR);
		categoryIndex = new IndexReader(indexDir, IndexType.CATEGORY);
		termIndex = new IndexReader(indexDir, IndexType.TERM);

		if (termIndex != null) {
			kgramIndex = termIndex.getKgramIndex();
		}
	}

	public static KgramIndex getKgramIndex() {
		return kgramIndex;
	}

	public static IndexReader getIndexByType(IndexType indexType) {
		if (indexType == null) {
			System.out.println();
		}
		if (indexType.equals(IndexType.CATEGORY))
			return categoryIndex;
		else if (indexType.equals(IndexType.AUTHOR))
			return authorIndex;
		else if (indexType.equals(IndexType.PLACE))
			return placeIndex;
		else if (indexType.equals(IndexType.TERM))
			return termIndex;
		else
			return null;
	}

	public static void setCorpusDirec(String corpusDirec) {
		corpusDir = corpusDirec;
	}

	public static String readDocFromFlattenedDir(String docName) throws ScorerException {
		String fileContent = "";
		String fileName = null;
		try {
			if (corpusDir != null && docName != null) {
				fileName = corpusDir + File.separator + docName;
				File file = new File(fileName);
				if (file.exists()) {
					Scanner scanner = new Scanner(file);
					if (scanner != null) {
						scanner.useDelimiter("\\A");
						if (scanner.hasNext())
							fileContent = scanner.next();
					}
				} else {
					throw new ScorerException("File does not exist: " + corpusDir + File.separator + docName);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new ScorerException("Problem reading from file: " + fileName + " - " + e.getMessage());
		}
		return fileContent;
	}
}