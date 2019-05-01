package com.github.tutertlob.mailboxnotifier.sensorsystemreceiver;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.util.logging.Logger;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

/**
 * Root resource (exposed at "mailboxes" path)
 */
@Path("mailboxes")
public class MailboxResource {
	protected static final Logger logger = Logger.getLogger(MailboxResource.class.getName());
	
    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    public String getMailboxes() {
        logger.info("Obtaining sensor lists.");
        Map<String, String> args = Collections.emptyMap();
        TransceiverFactory.getTransceiver().sendMailboxCheckCommand(args);
        DatabaseUtil db = DatabaseUtilFactory.getDatabaseUtil();
        return db.getSensorList();
    }

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Path("/mailbox/{room}")
    @Produces(MediaType.TEXT_PLAIN+";charset=UTF-8")
    public String getMailbox(@PathParam("room") String room) {
		String msg = "ちょっとポストの中を確認に行ってきます！";
        logger.info(msg);
        Map<String, String> args = new HashMap<>();
        args.put("mailbox", room);
        TransceiverFactory.getTransceiver().sendMailboxCheckCommand(args);
        return msg;
    }
}
