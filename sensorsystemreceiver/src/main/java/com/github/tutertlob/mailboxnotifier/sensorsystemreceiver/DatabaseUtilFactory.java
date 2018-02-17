package com.github.tutertlob.mailboxnotifier.sensorsystemreceiver;

public class DatabaseUtilFactory {
	public static final DatabaseUtil getDatabaseUtil() {
		String db = AppProperties.getProperty("database");
		
		if ("mongodb".equals(db)) {
			return MongoUtil.getInstance();
		} else {
			throw new IllegalArgumentException(db + " is not a supported database.");
		}
	}
}
