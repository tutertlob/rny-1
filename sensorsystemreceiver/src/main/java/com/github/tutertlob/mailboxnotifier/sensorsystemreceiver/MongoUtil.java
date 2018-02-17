package com.github.tutertlob.mailboxnotifier.sensorsystemreceiver;

import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.BulkWriteResult;
import com.mongodb.Cursor;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.ParallelScanOptions;
import com.mongodb.ServerAddress;

import java.nio.file.Path;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.text.SimpleDateFormat;

import java.lang.NullPointerException;
import java.lang.IllegalArgumentException;
import java.lang.NumberFormatException;
import java.net.UnknownHostException;
import com.mongodb.MongoException;

public class MongoUtil implements DatabaseUtil {
	private static final Logger logger = Logger.getLogger(MongoUtil.class.getName());
	
	private static final MongoUtil INSTANCE = new MongoUtil();
	
	private static MongoClient mongoClient;
	
	private static DB database;
	
	private static DBCollection collection;

	private MongoUtil() {
		String host;
		int port;
		String db;
		String dbcollection;
		
		try {
			host = AppProperties.getProperty("database.host");
		} catch (IllegalArgumentException | NullPointerException e) {
			host = "localhost";
		}
		try {
			port = Integer.parseInt(AppProperties.getProperty("database.port"));
		} catch (IllegalArgumentException | NullPointerException e) {
			port = 27017;
		}
		
		try {
			db = AppProperties.getProperty("database.db");
		} catch (IllegalArgumentException | NullPointerException e) {
			db = "mailbox_notifier";
		}
		
		try {
			dbcollection = AppProperties.getProperty("database.collection");
		} catch (IllegalArgumentException | NullPointerException e) {
			dbcollection = "posting_records";
		}

		try {
			mongoClient = new MongoClient(host, port);
		} catch (UnknownHostException e) {
			logger.log(Level.WARNING, String.format("Unknown host '%s:%d'.", host, port), e);
		}
		database = mongoClient.getDB(db);
		collection = database.getCollection(dbcollection);
	}
	
	public static MongoUtil getInstance() {
		return INSTANCE;
	}
	
	@Override
	public void insertImageRecord(Date capturedDate, Path file) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		BasicDBObject doc = new BasicDBObject("capturedDate", format.format(capturedDate))
                .append("file", file.toString())
                .append("event", "In");
		try {
			collection.insert(doc);
		} catch (MongoException e) {
			logger.log(Level.WARNING, "Inserting a document to collection failed.", e);
		}
	}

	@Override
	public void insertEventRecord(Date occurredDate, String event, String message) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		BasicDBObject doc = new BasicDBObject("receivedDate", format.format(occurredDate))
				.append("event", event)
				.append("message", message);
		try {
			collection.insert(doc);
		} catch (MongoException e) {
			logger.log(Level.WARNING, "Inserting a document to collection failed.", e);
		}
	}
}
