package com.github.tutertlob.mailboxnotifier.sensorsystemreceiver;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.MissingResourceException;
import java.lang.NullPointerException;
import java.io.IOException;

import com.github.tutertlob.im920wireless.util.Im920;
import com.github.tutertlob.im920wireless.util.Im920Interface;
import com.github.tutertlob.subghz.SubGHzFrame;
import com.github.tutertlob.subghz.PacketImplementation;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;

public final class Im920Transceiver extends Transceiver {

	private static final Logger logger = Logger.getLogger(Im920Transceiver.class.getName());

	private static final Im920Transceiver INSTANCE = new Im920Transceiver();

	private Im920Interface im920Interface;

	private Im920 im920;

	private boolean running = false;

	public static Im920Transceiver getInstance() {
		return INSTANCE;
	}

	private Im920Transceiver() {
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

	@Override
	public Im920Transceiver startTransceiver() {		
		if (!running) {
			this.start();
		}
		return INSTANCE;
	}

	@Override
	public void sendMailboxCheckCommand(Map<String, String> args) {
		this.im920.sendCommand(SubGHzCommand.MAILBOX_CHECK_CMD.cmd(), "Capture command.");
	}

	@Override
	public void run() {
		logger.info("Transceiver running...");
		running = true;

		try {
				List<PacketHandler> handlers = Arrays.asList(new SensorNoticeHandler(), new JpegImageReassembler());

				while (running) {
						SubGHzFrame frame = im920.readFrame();
						logger.log(Level.INFO, frame.toString());
						for (PacketHandler hnd : handlers)
								hnd.handle(frame);
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

	@Override
	public void finish() {
		if (running) {
			running = false;
			this.im920Interface.close();
			this.interrupt();
			logger.warning("Transceiver exited");
		}
	}

}
