package edu.buffalo.cse.irf14.query;

import java.util.HashMap;
import java.util.Map;

import edu.buffalo.cse.irf14.analysis.util.TermMetadataForThisDoc;

public class AND extends Expression {

	private Expression leftExpression;
	private Expression rightExpression;
	boolean notFlag = false;

	public AND() {

	}

	public AND(Expression leftExpression, Expression rightExpression) {
		this.leftExpression = rightExpression;
		this.rightExpression = leftExpression;
	}

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
		return leftExpression.toString() + " " + "AND" + " " + rightExpression.toString();

	}

	@Override
	public Map<Long, DocMetaData> getPostings() {
		/*- Recursively calling the left and right postings and intersecting them. */
		Map<Long, DocMetaData> leftTermPostingMap = leftExpression.getPostings();
		Map<Long, DocMetaData> rightTermPostingMap = rightExpression.getPostings();
		Map<Long, DocMetaData> intersectionMap = new HashMap<Long, DocMetaData>();

		for (Long docId : leftTermPostingMap.keySet()) {

			if (rightTermPostingMap.containsKey(docId)) {

				DocMetaData docMetadataLeft = leftTermPostingMap.get(docId);
				DocMetaData docMetadataRight = rightTermPostingMap.get(docId);

				/* Intersected node */
				Map<Long, TermMetadataForThisDoc> termMetaDataMap = new HashMap<Long, TermMetadataForThisDoc>();
				termMetaDataMap.putAll(docMetadataLeft.getTermMetaDataMap());
				termMetaDataMap.putAll(docMetadataRight.getTermMetaDataMap());

				DocMetaData intersectDocMetadata = new DocMetaData();
				intersectDocMetadata.setTermMetaDataMap(termMetaDataMap);
				intersectionMap.put(docId, intersectDocMetadata);
			}
		}
		return intersectionMap;
	}
}