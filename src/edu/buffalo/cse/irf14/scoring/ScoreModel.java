package edu.buffalo.cse.irf14.scoring;

import edu.buffalo.cse.irf14.query.Query;

public interface ScoreModel {
	
	public Query calculateScore(Query query) throws ScorerException; 

}
