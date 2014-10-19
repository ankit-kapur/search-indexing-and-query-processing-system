package edu.buffalo.cse.irf14.query;
import java.util.HashMap;
import java.util.Map;

import edu.buffalo.cse.irf14.analysis.util.TermMetadataForThisDoc;

public class DocMetaData {
	private Map<Long,TermMetadataForThisDoc> TermMetaDataMap ;
	private Float score;
	public DocMetaData()
	{
		TermMetaDataMap=new HashMap<Long, TermMetadataForThisDoc>();
	}
	
	public Map<Long, TermMetadataForThisDoc> getTermMetaDataMap() {
		return this.TermMetaDataMap;
	}
	public void setTermMetaDataMap(
			Map<Long, TermMetadataForThisDoc> termMetaDataMap) {
		TermMetaDataMap = termMetaDataMap;
	}
	public Float getScore() {
		return score;
	}
	public void setScore(Float score) {
		this.score = score;
	}

}
