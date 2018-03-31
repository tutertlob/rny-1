package com.github.tutertlob.mailboxnotifier.sensorsystemreceiver;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.tutertlob.im920wireless.packet.DataPacket;
import com.github.tutertlob.im920wireless.packet.Im920Packet;
import com.github.tutertlob.im920wireless.packet.NoticePacket;

class JpegImageReassembler implements Transceiver.PacketHandler {

	private static final Logger logger = Logger.getLogger(JpegImageReassembler.class.getName());
	
	private State state = State.END;

	private enum State {
		PROCESSING, END;
	}

	private static final byte[] SOI = {(byte)0xFF, (byte)0xD8};

	private static final byte[] EOI = {(byte)0xFF, (byte)0xD9};

	private static final String JPEG_EXT = ".jpg";

	private BufferedOutputStream jpegOStream;
	
	private Path path;

	private NoticePacket lastEvent;
	
	@Override
	public void handle(Im920Packet packet) {
		if (packet instanceof NoticePacket) {
			lastEvent = (NoticePacket) packet;
			return;
		}
		
		if (!(packet instanceof DataPacket)) {
			return;
		}

		DataPacket data = (DataPacket)packet;
		byte[] jpegChoppedData = data.getData();

		if (state == State.END) {
			byte[] soi = Arrays.copyOf(jpegChoppedData, SOI.length);
			if (Arrays.equals(SOI, soi)) {
				LocalDateTime timestamp = LocalDateTime.now();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
				String jpegFileName = timestamp.format(formatter);

				String basepath = AppProperties.getProperty("filestore.path");
				path = FileSystems.getDefault().getPath(basepath, jpegFileName + JPEG_EXT).toAbsolutePath().normalize();
				try {
					jpegOStream = new BufferedOutputStream(new FileOutputStream(path.toString()));
				} catch (FileNotFoundException | SecurityException e) {
					logger.log(Level.WARNING, "Can't create a file or write data to the file.", e);
					System.exit(-1);
				}
				state = State.PROCESSING;
				try {
					jpegOStream.write(jpegChoppedData);
				} catch (IOException e) {
					logger.log(Level.WARNING, "Writing data to the file failed.", e);
					e.printStackTrace();
					try {
						jpegOStream.close();
					} catch (IOException ee) {
						logger.log(Level.WARNING, "Closing jpeg output stream failed.", ee);
					}
					jpegOStream = null;
					path = null;
					state = State.END;
					return;
				}
			}
		} else if (state == State.PROCESSING) {
			try {
				byte[] eoi = Arrays.copyOfRange(jpegChoppedData, jpegChoppedData.length - EOI.length, jpegChoppedData.length);
				jpegOStream.write(jpegChoppedData);
				if (!packet.isFragmented()) {
					jpegOStream.close();
					jpegOStream = null;
					postJpegFile(path);
					state = State.END;
					if (!Arrays.equals(EOI, eoi))
						throw new IllegalStateException("Reached to the end of fragmented packets seriese although the end of jpeg chunk data never detected.");
				}
			} catch (IOException e) {
				logger.log(Level.WARNING, "Closing file or Writing data to the jpeg file failed.", e);
				e.printStackTrace();
				state = State.END;
				return;
			}
		}
	}
	
	private void postJpegFile(Path path) {
		logger.info("posting jpeg image.");
		DatabaseUtil db = DatabaseUtilFactory.getDatabaseUtil();
		db.insertImageRecord(lastEvent, path);
	}
}