package edu.buffalo.cse.irf14.query;

import java.util.HashMap;
import java.util.Map;

import edu.buffalo.cse.irf14.analysis.util.TermMetadataForThisDoc;

public class OR extends Expression {

	public OR() {
	}

	public OR(Expression leftExpression, Expression rightExpression) {

		this.leftExpression = rightExpression;
		this.rightExpression = leftExpression;
	}

	private Expression leftExpression;
	private Expression rightExpression;

	public Expression getLeftExpression() {
		return leftExpression;
	}

	public void setLeftExpression(Expression leftExpression) {
		this.leftExpression = leftExpression;
	}

	public Expression getRightExpression() {
		return rightExpression;
	}

	public void setRightExpression(Expression rightExpression) {
		this.rightExpression = rightExpression;
	}

	@Override
	public String toString() {
		return leftExpression.toString() + " " + "OR" + " " + rightExpression.toString();
	}

	@Override
	public Map<Long, DocMetaData> getPostings() {
		Map<Long, DocMetaData> leftTermPostingMap = leftExpression.getPostings();
		Map<Long, DocMetaData> rightTermPostingMap = leftExpression.getPostings();
		Map<Long, DocMetaData> unionMap = new HashMap<Long, DocMetaData>();

		unionMap.putAll(leftTermPostingMap);

		for (Long docId : rightTermPostingMap.keySet()) {

			if (unionMap.containsKey(docId)) {

				DocMetaData rightDocMetadata = rightTermPostingMap.get(docId);
				DocMetaData unionMetadata = unionMap.get(docId);
				
				/* Combine the maps */
				Map<Long, TermMetadataForThisDoc> unionDocMetadataMap = unionMetadata.getTermMetaDataMap();
				unionDocMetadataMap.putAll(rightDocMetadata.getTermMetaDataMap());
				unionMetadata.setTermMetaDataMap(unionDocMetadataMap);
				unionMap.put(docId, unionMetadata);
			} else {
				unionMap.put(docId, rightTermPostingMap.get(docId));
			}
		}

		return unionMap;
	}
}