package com.github.tutertlob.mailboxnotifier.sensorsystemreceiver;

import java.util.List;
import java.nio.file.Path;
import com.github.tutertlob.subghz.NoticePacketInterface;
import com.github.tutertlob.subghz.SubGHzFrame;

public interface DatabaseUtil {
	public void insertImageRecord(NoticePacketInterface notice, Path path);
	
	public void insertEventRecord(SubGHzFrame frame, String message);

	public String getSensorList();

	public void insertSensor(String sensor);
}