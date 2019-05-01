package com.github.tutertlob.mailboxnotifier.sensorsystemreceiver;

import java.io.IOException;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Objects;
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
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        final Logger sensorsystemreceiverLogger = Logger.getLogger(Main.class.getPackage().getName());
        logger.info("Starting the sensor system receiver...");

        try {
            String[] parts = CURRENT_PACKAGE.split("\\.");
            Handler handler = new FileHandler(parts[parts.length - 1] + ".log");
            sensorsystemreceiverLogger.addHandler(handler);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.warning("The sensor system receiver is exiting since the signal was received.");
                TransceiverFactory.getTransceiver().finish();
                ReceiverRestServer.finish();
            }));

            ReceiverRestServer.startRestServer();
            TransceiverFactory.getTransceiver().startTransceiver().join();
        } catch (IOException | SecurityException e) {
            e.printStackTrace();
            logger.log(Level.WARNING, "Couldn't create log file.", e);
        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.log(Level.INFO, "Caught InterruptedException then exitting the system.", e);
        } finally {
            TransceiverFactory.getTransceiver().finish();
            ReceiverRestServer.finish();
            System.out.println("Exited");
        }
        logger.info("The sensor system receiver exited.");
    }
}
