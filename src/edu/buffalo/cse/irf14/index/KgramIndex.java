package edu.buffalo.cse.irf14.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KgramIndex {
	int k;
	Map<String, List<Long>> index;

	public KgramIndex(int kValue) {
		k = kValue;
		index = new HashMap<String, List<Long>>();
	}

	public Map<String, List<Long>> getIndex() {
		return index;
	}

	public void setIndex(Map<String, List<Long>> index) {
		this.index = index;
	}
	
	public void addToIndex(String key, long termId) {
		if (index.containsKey(key)) {
			/* Already exists. Add termId to the list */
			List<Long> list = index.get(key);
			list.add(termId);
			index.put(key, list);
		} else {
			/* New entry */
			List<Long> list = new ArrayList<Long>();
			list.add(termId);
			index.put(key, list);
		}
	}

	public int getK() {
		return k;
	}
}