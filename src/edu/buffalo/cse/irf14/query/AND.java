package edu.buffalo.cse.irf14.query;

import java.util.HashMap;
import java.util.Map;

public class AND implements Expression {

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
		Map<Long, DocMetaData> rightTermPostingMap = leftExpression.getPostings();

		Map<Long, DocMetaData> intersectionMap = new HashMap<Long, DocMetaData>();
		intersectionMap.putAll(leftTermPostingMap);
		for (Long DocId : rightTermPostingMap.keySet()) {
			if (intersectionMap.containsKey(DocId)) {
				rightTermPostingMap.get(DocId);
			} else {
				intersectionMap.put(DocId, rightTermPostingMap.get(DocId));
			}
		}
		return intersectionMap;
	}
}