package com.github.tutertlob.iot_applications.sensorsystemreceiver;

import com.github.tutertlob.iotgateway.Transceiver;
import com.github.tutertlob.iotgateway.TransceiverFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.util.logging.Logger;

/**
 * Root resource (exposed at "mailboxes" path)
 */
@Path("mailboxes")
public class MailboxResource {
    protected static final Logger logger = Logger.getLogger(MailboxResource.class.getName());

    private static final byte MAILBOX_CHECK_CMD = (byte) 0;

    /**
     * Method handling HTTP GET requests. The returned object will be sent to the
     * client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Path("/mailbox/{room}")
    @Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
    public String getMailbox(@PathParam("room") String room) {
        String msg = "ちょっとポストの中を確認に行ってきます！";
        logger.info(msg);
        try {
            TransceiverFactory.getTransceiver().sendCommand(room, "", MAILBOX_CHECK_CMD, "", false);
        } catch (NullPointerException e) {
            logger.info(String.format("指定されたポスト(%s)はありません。", room));
        }
        return msg;
    }
}
