package edu.buffalo.cse.irf14.query;

import java.util.HashMap;
import java.util.Map;

public class NOT extends Expression {
	private Expression leftExpression;
	private Expression rightExpression;

	public NOT() {

	}

	public NOT(Expression leftExpression, Expression rightExpression) {
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
		Map<Long, DocMetaData> combinedMap = new HashMap<Long, DocMetaData>();

		for (Long docId : leftTermPostingMap.keySet()) {

			/* The right map should not contain this doc */
			if (!rightTermPostingMap.containsKey(docId)) {

				/* Combined node */
				DocMetaData docMetadataLeft = leftTermPostingMap.get(docId);
				combinedMap.put(docId, docMetadataLeft);
			}
		}
		return combinedMap;
	}
}