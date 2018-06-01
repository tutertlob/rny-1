package com.github.tutertlob.mailboxnotifier.sensorsystemreceiver;

import java.net.UnknownHostException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.tutertlob.im920wireless.packet.NoticePacket;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
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
			host = AppProperties.getInstance().getProperty("database.host");
		} catch (IllegalArgumentException | NullPointerException e) {
			host = "localhost";
		}
		try {
			port = Integer.parseInt(AppProperties.getInstance().getProperty("database.port"));
		} catch (IllegalArgumentException | NullPointerException e) {
			port = 27017;
		}

		try {
			db = AppProperties.getInstance().getProperty("database.db");
		} catch (IllegalArgumentException | NullPointerException e) {
			db = "mailbox_notifier";
		}

		try {
			dbcollection = AppProperties.getInstance().getProperty("database.collection");
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
	public void insertImageRecord(NoticePacket notice, Path path) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		BasicDBObject doc = new BasicDBObject();
		doc.append("capturedDate", format.format(new Date())).append("sender", notice.getModuleId())
				.append("receiver", notice.getNodeId()).append("event", notice.getNotice())
				.append("rssi", notice.getRssi()).append("file", path.toString());
		try {
			collection.insert(doc);
		} catch (MongoException e) {
			logger.log(Level.WARNING, "Inserting a document to collection failed.", e);
		}
	}

	@Override
	public void insertEventRecord(NoticePacket notice, String message) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		BasicDBObject doc = new BasicDBObject();
		doc.append("receivedDate", format.format(new Date())).append("sender", notice.getModuleId())
				.append("receiver", notice.getNodeId()).append("event", notice.getNotice())
				.append("rssi", notice.getRssi()).append("message", message);
		try {
			collection.insert(doc);
		} catch (MongoException e) {
			logger.log(Level.WARNING, "Inserting a document to collection failed.", e);
		}
	}
}
