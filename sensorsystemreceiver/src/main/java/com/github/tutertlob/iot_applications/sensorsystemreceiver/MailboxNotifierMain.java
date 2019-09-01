package com.github.tutertlob.iot_applications.sensorsystemreceiver;

import com.github.tutertlob.iotgateway.IoTApplication;
import com.github.tutertlob.iotgateway.Transceiver.PacketHandler;

import java.util.logging.Logger;
import java.util.List;
import java.util.Arrays;
import java.io.IOException;

/**
 * Main class.
 *
 */
public class MailboxNotifierMain extends IoTApplication {

    private static final Logger logger = Logger.getLogger(MailboxNotifierMain.class.getName());

    /**
     * Main method.
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        logger.info("Starting mailbox notifier...");

        List<PacketHandler> handlers = Arrays.asList(new SensorNoticeHandler(), new JpegImageReassembler());
        launch(handlers);

        logger.info("Mailbox notifier exited.");
    }

    public MailboxNotifierMain() {
        super();
    }

    @Override

    public void start() {

    }

    @Override
    public void finish() {

    }
}
