package com.github.tutertlob.iot_applications.sensorsystemreceiver;

import java.util.logging.Logger;

import com.github.tutertlob.iotgateway.Transceiver;
import com.github.tutertlob.iotgateway.Transceiver.PacketHandler;
import com.github.tutertlob.iotgateway.DatabaseUtil;
import com.github.tutertlob.iotgateway.DatabaseUtilFactory;
import com.github.tutertlob.iotgateway.SensorEntity;
import com.github.tutertlob.iotgateway.SensorRecord;
import com.github.tutertlob.subghz.NoticePacketInterface;
import com.github.tutertlob.subghz.PacketImplementation;
import com.github.tutertlob.subghz.SubGHzFrame;

public class SensorNoticeHandler implements PacketHandler {

	private static final Logger logger = Logger.getLogger(SensorNoticeHandler.class.getName());

	@Override
	public void handle(SubGHzFrame frame) {
		PacketImplementation packet = frame.getPacket();
		if (!(packet instanceof NoticePacketInterface)) {
			return;
		}

		NoticePacketInterface notice = (NoticePacketInterface) packet;
		String event = notice.getNotice();

		String message;
		if (event.startsWith("Posted")) {
			message = "郵便が投函されました。";
		} else if (event.startsWith("Pulled")) {
			message = "郵便物が取り出されました。";
		} else {
			message = "不明なイベント通知を受信しました。";
		}
		MailboxNotifierRecord data = new MailboxNotifierRecord(event, message);

		SensorEntity sensor = null;
		try {
			sensor = SensorEntity.lookUpSensor(frame.getSenderAddr());
		} catch (NullPointerException e) {
			sensor = new SensorEntity().setAddr(frame.getSenderAddr()).setPanid(frame.getSenderExtAddr());
		}

		SensorRecord<MailboxNotifierRecord> record = new SensorRecord<>(sensor, data)
				.setRssi(Integer.valueOf(frame.getRssi())).setPacketType(packet.getPacketType().toString())
				.setContentType("application/json");

		DatabaseUtil db = DatabaseUtilFactory.getDatabaseUtil();
		db.insertSensorRecord(record);

		logger.info("a notification has been received: \n" + event);
	}

}
