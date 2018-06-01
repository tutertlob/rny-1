package com.github.tutertlob.mailboxnotifier.sensorsystemreceiver;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.tutertlob.im920wireless.packet.Im920Packet;
import com.github.tutertlob.im920wireless.util.Im920;
import com.github.tutertlob.im920wireless.util.Im920Interface;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;

class Transceiver extends Thread {

	private static final Logger logger = Logger.getLogger(Transceiver.class.getName());

	private static final Transceiver INSTANCE = new Transceiver();

	private static final byte MAILBOX_CHECK_CMD = (byte) 0x80;

	private Im920Interface im920Interface;

	private Im920 im920;

	private static boolean running = false;

	private Transceiver() {
		Im920Interface.BaudRate baud = Im920Interface.BaudRate.B_19200;
		String port = "/dev/ttyUSB0";
		try {
			Integer b = Integer.parseInt(AppProperties.getInstance().getProperty("serial.baud"));
			baud = Im920Interface.BaudRate.valueOf(b);

			port = AppProperties.getInstance().getProperty("serial.port");
		} catch (IllegalArgumentException e) {
			logger.log(Level.WARNING,
					"The property 'serial.baud' includes non numerical characters or is not supported for Im920Interface.",
					e);
			logger.info("19200 bps is used for Im920Interface instead of 'serial.baud'");
		} catch (MissingResourceException | NullPointerException e) {
			logger.log(Level.WARNING, "The key is null or not supported.", e);
			logger.warning("The default properties are used instead");
		}

		try {
			im920Interface = Im920Interface.open(port, baud);
			im920 = new Im920(im920Interface);
		} catch (IOException | NoSuchPortException | PortInUseException e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, String.format("Serial port '%s' already in used.",
					AppProperties.getInstance().getProperty("serial.port")), e);
			System.exit(-1);
		}
	}

	public static Transceiver startTransceiver() {
		if (!running) {
			INSTANCE.start();
		}
		return INSTANCE;
	}

	public static void sendMailboxCheckCommand() {
		INSTANCE.im920.sendCommand(MAILBOX_CHECK_CMD, "Capture command.");
	}

	@Override
	public void run() {
		logger.info("Transceiver running...");
		running = true;

		try {
			List<PacketHandler> handlers = Arrays.asList(new SensorNoticeHandler(), new JpegImageReassembler());

			while (running) {
				Im920Packet packet = im920.readPacket();
				for (PacketHandler hnd : handlers)
					hnd.handle(packet);
			}
		} catch (InterruptedException e) {
			logger.log(Level.INFO, "Caught an interruption then Tranceiver thread is preparing to exit thread main.",
					e);
		} catch (Throwable e) {
			e.printStackTrace();
			logger.log(Level.WARNING, "Caught an unexpected exception: ", e);
			System.exit(-1);
		} finally {
			running = false;
			logger.warning("Transceiver exited");
		}
	}

	public static void finish() {
		if (running) {
			running = false;
			INSTANCE.im920Interface.close();
			INSTANCE.interrupt();
			logger.warning("Transceiver exited");
		}
	}

	public static interface PacketHandler {
		public void handle(Im920Packet packet);
	}
}
