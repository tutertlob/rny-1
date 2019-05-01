package com.github.tutertlob.mailboxnotifier.sensorsystemreceiver;

import java.util.Objects;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public enum SubGHzCommand {

	MAILBOX_CHECK_CMD((byte)0);

	private static final Logger logger = Logger.getLogger(SubGHzCommand.class.getName());

	private final byte cmd;

	private static Map<Byte,SubGHzCommand> m = new HashMap<Byte, SubGHzCommand>();

	static {
		for (SubGHzCommand t : SubGHzCommand.values()) {
			m.put(Byte.valueOf(t.cmd), t);
		}
	}

	private SubGHzCommand(byte cmd) {
		this.cmd = cmd;
	}

	public byte cmd() {
		return this.cmd;
	}

	public static boolean isValid(byte i) {
		return Objects.nonNull(m.get(Byte.valueOf(i)));
	}

	public static SubGHzCommand valueOf(byte i) {
		if (!isValid(i)) {
			String msg = String.format("Invalid command %d", i);
			logger.log(Level.WARNING, msg);
			throw new IllegalArgumentException(msg);
		}
		return m.get(Byte.valueOf(i));
	}
}
