package com.github.tutertlob.mailboxnotifier.sensorsystemreceiver;

import com.github.tutertlob.subghz.PacketImplementation;
import com.github.tutertlob.subghz.SubGHzFrame;

import java.lang.Thread;
import java.util.Map;
import java.util.HashMap;

public abstract class Transceiver extends Thread {

	public abstract Transceiver startTransceiver();

	public abstract void sendMailboxCheckCommand(Map<String, String> args);

	public abstract void run();

	public abstract void finish();

	public static interface PacketHandler {

		public void handle(SubGHzFrame frame);

	}

}
