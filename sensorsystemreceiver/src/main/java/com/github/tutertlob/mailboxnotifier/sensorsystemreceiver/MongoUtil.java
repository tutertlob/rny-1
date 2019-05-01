package com.github.tutertlob.mailboxnotifier.sensorsystemreceiver;

import java.net.UnknownHostException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.tutertlob.subghz.SubGHzFrame;
import com.github.tutertlob.subghz.PacketImplementation;
import com.github.tutertlob.subghz.NoticePacketInterface;
import com.mongodb.util.JSON;
import com.mongodb.DBObject;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import org.json.JSONArray;

public class MongoUtil implements DatabaseUtil {
	private static final Logger logger = Logger.getLogger(MongoUtil.class.getName());

	private static final MongoUtil INSTANCE = new MongoUtil();

	private final MongoClient mongoClient;

	private final DB database;

	private final DBCollection collection;

	private final DBCollection sensorCollection;

	private final String SensorCollection = "sensor_list";

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

		MongoClient temp = null;
		try {
			temp = new MongoClient(host, port);
		} catch (UnknownHostException e) {
			logger.log(Level.SEVERE, String.format("Unknown host '%s:%d'.", host, port), e);
			System.exit(-1);
		}
		mongoClient = temp;
		database = mongoClient.getDB(db);
		collection = database.getCollection(dbcollection);
		sensorCollection = database.getCollection(SensorCollection);
	}

	public static MongoUtil getInstance() {
		return INSTANCE;
	}

	@Override
	public void insertImageRecord(NoticePacketInterface notice, Path path) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		BasicDBObject doc = new BasicDBObject();
		doc.append("capturedDate", format.format(new Date()))
				.append("event", notice.getNotice())
				.append("file", path.toString());
		logger.log(Level.INFO, "Recorded image: " + path.toString());
		try {
			collection.insert(doc);
		} catch (MongoException e) {
			logger.log(Level.WARNING, "Inserting a document to collection failed.", e);
		}
	}

	@Override
	public void insertEventRecord(SubGHzFrame frame, String message) {
		PacketImplementation packet = frame.getPacket();
		if (!(packet instanceof NoticePacketInterface)) {
			return;
		}
		NoticePacketInterface notice = (NoticePacketInterface) packet;

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		BasicDBObject doc = new BasicDBObject();
		doc.append("receivedDate", format.format(new Date()))
			.append("sender", frame.getSender())
			.append("event", notice.getNotice())
			.append("rssi", frame.getRssi())
			.append("message", message);
		try {
			collection.insert(doc);
		} catch (MongoException e) {
			logger.log(Level.WARNING, "Inserting a document to collection failed.", e);
		}
	}

	@Override
	public String getSensorList() {	
		DBObject query = (DBObject)JSON.parse("{}");
		DBObject projection = (DBObject)JSON.parse("{'_id': 0}");
		List<DBObject> list = sensorCollection.find(query, projection).toArray();
		JSONArray json = new JSONArray(list);
		logger.log(Level.INFO, "Sensor list: " + json.toString());
		return json.toString();
	}

	@Override
	public void insertSensor(String sensor) {
		try {
			DBObject query = BasicDBObjectBuilder.start().add("id", sensor).get();
			if (sensorCollection.count(query) > 0) {
				return;
			}

			String rfInterface = AppProperties.getInstance().getProperty("rf.interface");
			DBObject doc = BasicDBObjectBuilder.start().add("id", sensor).add("interface", rfInterface).get();
			sensorCollection.insert(doc);
			logger.log(Level.INFO, String.format("Sensor id=%s, interface=%s was registered in DB.", sensor, rfInterface));
		} catch (MongoException e) {
			logger.log(Level.WARNING, e.toString(), e);
		}
	}
}
