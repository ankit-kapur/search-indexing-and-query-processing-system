package edu.buffalo.cse.irf14.query;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import edu.buffalo.cse.irf14.index.IndexReader;
import edu.buffalo.cse.irf14.index.IndexType;
import edu.buffalo.cse.irf14.scoring.ScorerException;

public class IndexesAndDictionaries {

	static IndexReader placeIndex;
	static IndexReader authorIndex;
	static IndexReader categoryIndex;
	static IndexReader termIndex;

	static String corpusDir;

	public static void readIndexes(String indexDir) {
		placeIndex = new IndexReader(indexDir, IndexType.PLACE);
		authorIndex = new IndexReader(indexDir, IndexType.AUTHOR);
		categoryIndex = new IndexReader(indexDir, IndexType.CATEGORY);
		termIndex = new IndexReader(indexDir, IndexType.TERM);
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
		try {
			if (corpusDir != null && docName != null) {
				File file = new File(corpusDir + File.separator + docName);
				if (file.exists()) {
					fileContent = new Scanner(file).useDelimiter("\\A").next();
				} else {
					throw new ScorerException("File does not exist: " + corpusDir + File.separator + docName);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new ScorerException(e.getMessage());
		}
		return fileContent;
	}
}