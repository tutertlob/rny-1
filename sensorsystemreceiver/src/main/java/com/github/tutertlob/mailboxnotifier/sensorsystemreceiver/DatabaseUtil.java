package com.github.tutertlob.mailboxnotifier.sensorsystemreceiver;

import java.nio.file.Path;
import java.util.Date;

public interface DatabaseUtil {
	public void insertImageRecord(Date capturedDate, Path file);
	
	public void insertEventRecord(Date occurredDate, String event, String message);
}
