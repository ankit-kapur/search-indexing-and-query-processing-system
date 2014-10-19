package edu.buffalo.cse.irf14.query;

import java.util.HashMap;
import java.util.Map;



public class NOT implements Expression
{
	private Expression leftExpression;
	private Expression rightExpression;
	
	public NOT()
	{
		
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
	public String toString()
	{
		return leftExpression.toString()+" "+"AND"+" "+rightExpression.toString();
		
	}
	@Override
	public Map<Long, DocMetaData> getPostings() {
		// TODO Auto-generated method stub
		Map<Long, DocMetaData> leftTermPostingMap=leftExpression.getPostings();
		Map<Long, DocMetaData> rightTermPostingMap=rightExpression.getPostings();
		Map<Long, DocMetaData> NotMap = new HashMap<Long, DocMetaData>();
		NotMap.putAll(rightTermPostingMap);
		for(Long DocId:NotMap.keySet())
		{
			if(leftTermPostingMap.containsKey(DocId))
				NotMap.remove(DocId);
			if(NotMap.entrySet()==null)
				break;
		}
		return NotMap;
	}
	

}
