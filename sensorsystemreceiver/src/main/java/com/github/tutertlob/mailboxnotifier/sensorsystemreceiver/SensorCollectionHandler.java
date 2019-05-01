package com.github.tutertlob.mailboxnotifier.sensorsystemreceiver;

import java.util.logging.Logger;

import com.github.tutertlob.subghz.NoticePacketInterface;
import com.github.tutertlob.subghz.PacketImplementation;
import com.github.tutertlob.subghz.SubGHzFrame;

import java.util.List;
import java.util.ArrayList;

import org.json.JSONObject;
import org.json.JSONArray;

class SensorCollectionHandler implements Transceiver.PacketHandler {

	private static final Logger logger = Logger.getLogger(SensorNoticeHandler.class.getName());

	private final List<String> sensors = new ArrayList<>();

	private final String SensorField = "id";

	public SensorCollectionHandler() {
		String json = MongoUtil.getInstance().getSensorList();
		JSONArray jsonArray = new JSONArray(json);
		for (Object obj: jsonArray) {
			JSONObject jsonObj = (JSONObject)obj;
			String value = (String)jsonObj.get(SensorField);
			sensors.add(value);
		}
	}

	@Override
	public void handle(SubGHzFrame frame) {
		PacketImplementation packet = frame.getPacket();
		if (!(packet instanceof NoticePacketInterface)) {
			return;
		}

		NoticePacketInterface notice = (NoticePacketInterface) packet;
		DatabaseUtil db = DatabaseUtilFactory.getDatabaseUtil();
		String event = notice.getNotice();

		if (!event.startsWith("Posted") && !event.startsWith("Pulled")) {
			return;
		}

		String sensorId = frame.getSender();
		if (!sensors.contains(sensorId)) {
			sensors.add(sensorId);
			MongoUtil.getInstance().insertSensor(sensorId);
		}
	}

}
