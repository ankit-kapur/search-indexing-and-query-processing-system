package edu.buffalo.cse.irf14.query;

import edu.buffalo.cse.irf14.index.IndexType;

public class QueryUtils {

	public static IndexType getZoneTypeByZoneName(String zoneString) {
		IndexType zone = null;
		if (zoneString != null) {
			zoneString = zoneString.toLowerCase();
			if (zoneString.equals("category")) {
				zone = IndexType.CATEGORY;
			} else if (zoneString.equals("place")) {
				zone = IndexType.PLACE;
			} else if (zoneString.equals("author")) {
				zone = IndexType.AUTHOR;
			} else if (zoneString.equals("term")) {
				zone = IndexType.TERM;
			} else {
				zone = IndexType.TERM;
			}
		}
		return zone;
	}

}
