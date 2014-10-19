package edu.buffalo.cse.irf14.query;

import java.util.HashMap;
import java.util.Map;



public class OR implements Expression {
	
	public OR()
	{
		
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
	public String toString()
	{
		return leftExpression.toString()+" "+"OR"+" "+rightExpression.toString();
		
	}
	@Override
	public Map<Long, DocMetaData> getPostings() {
		Map<Long, DocMetaData> leftTermPostingMap=leftExpression.getPostings();
		Map<Long, DocMetaData> rightTermPostingMap=leftExpression.getPostings();
		Map<Long, DocMetaData> UnionMap = new HashMap<Long, DocMetaData>();
		UnionMap.putAll(leftTermPostingMap);
		UnionMap.putAll(rightTermPostingMap);
		return UnionMap;
	}
	
}
