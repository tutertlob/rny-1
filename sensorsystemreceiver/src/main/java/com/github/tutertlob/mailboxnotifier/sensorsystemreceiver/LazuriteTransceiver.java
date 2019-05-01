package com.github.tutertlob.mailboxnotifier.sensorsystemreceiver;

import com.github.tutertlob.lazurite.LazuriteParams;
import com.github.tutertlob.lazurite.LazuriteUtils;
import com.github.tutertlob.subghz.PacketImplementation;
import com.github.tutertlob.subghz.SubGHzFrame;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.lang.NumberFormatException;
import java.lang.InterruptedException;

public final class LazuriteTransceiver extends Transceiver {

	private static final Logger logger = Logger.getLogger(LazuriteTransceiver.class.getName());

	private static final LazuriteTransceiver INSTANCE = new LazuriteTransceiver();

	private boolean running = false;

	public static LazuriteTransceiver getInstance() {
		return INSTANCE;
	}

	private LazuriteTransceiver() {

	}

	@Override
	public LazuriteTransceiver startTransceiver() {
		if (!running) {
			logger.info("Transceiver is starting...");
			LazuriteParams lazParams = AppProperties.getInstance().getLazuriteParams();
			LazuriteUtils.begin(lazParams);
			running = true;
			this.start();
		}
		return INSTANCE;
	}

	@Override
	public void sendMailboxCheckCommand(Map<String, String> args) {
		String panid = AppProperties.getInstance().getProperty("lazurite.panid");
		String dstAddr = args.getOrDefault("mailbox", "ffff");
		try {
			LazuriteUtils.sendCommand((short) Integer.parseInt(panid, 16), (short) Integer.parseInt(dstAddr, 16),
					SubGHzCommand.MAILBOX_CHECK_CMD.cmd(), "Capture command.");
		} catch (NumberFormatException e) {
			e.printStackTrace();
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	@Override
	public void run() {
		logger.info("Transceiver running...");

		try {
			List<PacketHandler> handlers = Arrays.asList(new SensorNoticeHandler(), new JpegImageReassembler(), new SensorCollectionHandler());

			while (running) {
				SubGHzFrame frame = LazuriteUtils.readFrame();
				logger.log(Level.INFO, frame.toString());
				for (PacketHandler hnd : handlers)
					hnd.handle(frame);
			}
		} catch (Throwable e) {
			e.printStackTrace();
			logger.log(Level.WARNING, "Caught an unexpected exception: ", e);
			System.exit(-1);
		} finally {
			running = false;
			logger.warning("Transceiver exited");
		}
	}

	@Override
	public void finish() {
		if (running) {
			running = false;
			this.interrupt();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// Do nothing
			}
			LazuriteUtils.close();
			logger.info("Transceiver exited.");
		}
	}

}
