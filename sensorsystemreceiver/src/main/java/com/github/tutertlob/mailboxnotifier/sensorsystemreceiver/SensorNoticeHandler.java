package com.github.tutertlob.mailboxnotifier.sensorsystemreceiver;

import java.util.logging.Logger;
import java.util.logging.Level;

import java.util.Date;

import com.github.tutertlob.im920wireless.packet.NoticePacket;
import com.github.tutertlob.im920wireless.packet.Im920Packet;

class SensorNoticeHandler implements Transceiver.PacketHandler {
	
	private static final Logger logger = Logger.getLogger(SensorNoticeHandler.class.getName());
	
	@Override
	public void handle(Im920Packet packet) {
		if (!(packet instanceof NoticePacket)) {
			return;
		}

		NoticePacket notice = (NoticePacket)packet;

		//
		// Implement HERE !!!
		// call a rest api of the Sender then a notice message goes out.
		// Implement HERE.
		//
		DatabaseUtil db = DatabaseUtilFactory.getDatabaseUtil();
		Date date = new Date();
		
		String event = notice.getNotice();
		if (event.startsWith("Posted")) {
			db.insertEventRecord(date, notice.getNotice(), "郵便が投函されました。");
		} else if (event.startsWith("Pulled")) {
			db.insertEventRecord(date, event, "郵便物が取り出されました。");
		} else {
			logger.info("不明なイベント通知を受信しました: " + event);
		}
		
		logger.info("a notification has been received: \n" + event);
	}
}
