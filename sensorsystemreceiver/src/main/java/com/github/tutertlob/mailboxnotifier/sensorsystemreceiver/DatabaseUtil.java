package com.github.tutertlob.mailboxnotifier.sensorsystemreceiver;

import java.nio.file.Path;
import com.github.tutertlob.im920wireless.packet.NoticePacket;

public interface DatabaseUtil {
	public void insertImageRecord(NoticePacket notice, Path path);
	
	public void insertEventRecord(NoticePacket notice, String message);
}