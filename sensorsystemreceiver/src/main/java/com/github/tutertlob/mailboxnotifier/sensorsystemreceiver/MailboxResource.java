package com.github.tutertlob.mailboxnotifier.sensorsystemreceiver;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.util.logging.Logger;

/**
 * Root resource (exposed at "mailbox" path)
 */
@Path("mailbox")
public class MailboxResource {
	protected static final Logger logger = Logger.getLogger(MailboxResource.class.getName());
	
    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN+";charset=UTF-8")
    public String getIt() {
		String msg = "ちょっとポストの中を確認に行ってきます！";
		logger.info(msg);
		Transceiver.sendMailboxCheckCommand();
        return msg;
    }
}
