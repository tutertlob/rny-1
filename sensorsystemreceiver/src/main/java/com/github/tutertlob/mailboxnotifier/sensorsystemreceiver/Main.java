package com.github.tutertlob.mailboxnotifier.sensorsystemreceiver;

import java.io.IOException;

import java.util.logging.Logger;
import java.util.logging.Level;

import java.util.logging.FileHandler;
import java.util.logging.Handler;

/**
 * Main class.
 *
 */
public class Main {
	
	private static final Logger logger = Logger.getLogger(Main.class.getName());
	
	private static final String CURRENT_PACKAGE = Main.class.getPackage().getName();
	
    /**
     * Main method.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
    		final Logger sensorsystemreceiverLogger = Logger.getLogger(Main.class.getPackage().getName());
    		logger.info("Starting the sensor system receiver...");
    		
		try {
			String[] parts = CURRENT_PACKAGE.split("\\.");
			Handler handler = new FileHandler(parts[parts.length-1] + ".log");
			sensorsystemreceiverLogger.addHandler(handler);
			
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				logger.warning("The sensor system receiver is exiting since the signal was received.");
				Transceiver.finish();
				ReceiverRestServer.finish();
			}));
			
			ReceiverRestServer.startRestServer();
			Transceiver.startTransceiver().join();
		} catch(IOException | SecurityException e) {
			logger.log(Level.INFO, "Starting your application failed.", e);
			e.printStackTrace();
			Transceiver.finish();
			return;
		} catch (InterruptedException e) {
			logger.log(Level.INFO, "Caught InterruptedException then exited the system.", e);
		} 
		finally {
			Transceiver.finish();
			ReceiverRestServer.finish();
		}
		logger.info("The sensor system receiver exited.");
    }
}

