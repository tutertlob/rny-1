package com.github.tutertlob.mailboxnotifier.sensorsystemreceiver;

import java.util.logging.Logger;

import com.github.tutertlob.subghz.NoticePacketInterface;
import com.github.tutertlob.subghz.PacketImplementation;
import com.github.tutertlob.subghz.SubGHzFrame;

class SensorNoticeHandler implements Transceiver.PacketHandler {

	private static final Logger logger = Logger.getLogger(SensorNoticeHandler.class.getName());

	@Override
	public void handle(SubGHzFrame frame) {
		PacketImplementation packet = frame.getPacket();
		if (!(packet instanceof NoticePacketInterface)) {
			return;
		}

		NoticePacketInterface notice = (NoticePacketInterface) packet;
		DatabaseUtil db = DatabaseUtilFactory.getDatabaseUtil();
		String event = notice.getNotice();

		if (event.startsWith("Posted")) {
				db.insertEventRecord(frame, "郵便が投函されました。");
		} else if (event.startsWith("Pulled")) {
				db.insertEventRecord(frame, "郵便物が取り出されました。");
		} else {
				logger.info("不明なイベント通知を受信しました: " + event);
		}

		logger.info("a notification has been received: \n" + event);
	}

}
