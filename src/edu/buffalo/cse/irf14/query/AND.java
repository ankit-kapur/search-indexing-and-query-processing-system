package edu.buffalo.cse.irf14.query;
import java.util.HashMap;
import java.util.Map;



public class AND implements Expression {

	private Expression leftExpression;
	private Expression rightExpression;
	boolean notFlag=false;

	public AND()
	{

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
	public String toString()
	{
		return leftExpression.toString()+" "+"AND"+" "+rightExpression.toString();

	}

	@Override
	public Map<Long, DocMetaData> getPostings() {
		// TODO Auto-generated method stub
		//Recursively calling the left and right postings and intersecting them.
		int leftMaplength;
		int rightMaplength;
		Map<Long,DocMetaData> leftTermPostingMap=leftExpression.getPostings();
		leftMaplength=leftTermPostingMap.size();
		Map<Long, DocMetaData> rightTermPostingMap=leftExpression.getPostings();
		rightMaplength=rightTermPostingMap.size();
		Map<Long, DocMetaData> intersectionMap = new HashMap<Long, DocMetaData>();
		if(leftMaplength>rightMaplength)
		{
			intersectionMap.putAll(leftTermPostingMap);
			for(Long DocId:rightTermPostingMap.keySet())
			{
				if(!intersectionMap.containsKey(DocId))
				{
					intersectionMap.remove(DocId);
				}
				else
				{
					intersectionMap.put(DocId,rightTermPostingMap.get(DocId));
				}
			}
		}
		else
		{
			intersectionMap.putAll(rightTermPostingMap);
			for(Long DocId:leftTermPostingMap.keySet())
			{
				if(!intersectionMap.containsKey(DocId))
				{
					intersectionMap.remove(DocId);
				}
				else
				{
					intersectionMap.put(DocId,leftTermPostingMap.get(DocId));
				}
			}
		}
		return intersectionMap;
	}
}
