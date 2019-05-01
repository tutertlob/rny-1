package com.github.tutertlob.mailboxnotifier.sensorsystemreceiver;

import com.github.tutertlob.lazurite.LazuriteParams;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.lang.NumberFormatException;
import java.util.Objects;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppProperties {
	private static final Logger logger = Logger.getLogger(AppProperties.class.getName());

	private static final String CURRENT_PACKAGE = AppProperties.class.getPackage().getName();

	private static final AppProperties INSTANCE = new AppProperties();

	private final String propertyfile;

	private final Properties properties;

	private final LazuriteParams lazuriteParams;

	private AppProperties() {
		properties = new Properties(setupDefaultProperties());

		String[] parts = CURRENT_PACKAGE.split("\\.");
		propertyfile = parts[parts.length - 1] + ".properties";

		try {
			InputStream file = new FileInputStream(propertyfile);
			properties.load(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			logger.log(Level.WARNING, String.format("The property file %s doesn't exist.", propertyfile), e);
			System.exit(-1);
		} catch (IOException e) {
			e.printStackTrace();
			logger.log(Level.WARNING, "Couldn't read properties from the property file.", e);
		}
		lazuriteParams = buildLazuriteParams();
	}

	private Properties setupDefaultProperties() {
		Properties defaults = new Properties();
		defaults.setProperty("serial.baud", "19200");
		defaults.setProperty("serial.port", "/dev/ttyUSB0");
		defaults.setProperty("lazurite.ch", "36");
		defaults.setProperty("lazurite.rate", "100");
		defaults.setProperty("lazurite.pwr", "20");
		defaults.setProperty("lazurite.txretry", "10");
		defaults.setProperty("lazurite.txinterval", "500");
		defaults.setProperty("rest.host", "localhost");
		defaults.setProperty("rest.port", "1198");
		defaults.setProperty("receiver.base", "receiver");
		defaults.setProperty("receiver.resource", "mailbox");
		defaults.setProperty("database", "mongodb");
		defaults.setProperty("database.host", "localhost");
		defaults.setProperty("database.port", "27017");
		defaults.setProperty("database.db", "mailbox_notifier");
		defaults.setProperty("database.collection", "posting_records");
		defaults.setProperty("filestore.path", "./");
		return defaults;
	}

	public static AppProperties getInstance() {
		return INSTANCE;
	}

	public final String getProperty(String key) {
		String value = properties.getProperty(key);
		if (Objects.isNull(value)) {
			throw new MissingResourceException(
				String.format("The property of specified key couldn't be found in the property file '%s'.", propertyfile),
				AppProperties.class.getName(),
				key);
		}
		return value;
	}

	private LazuriteParams buildLazuriteParams() {
		LazuriteParams.Builder builder;

		String value;
		byte ch;
		try {
			value = getProperty("lazurite.ch");
			ch = Byte.parseByte(value);
		} catch (MissingResourceException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
			ch = 36;
		} catch (NumberFormatException e) {
			logger.log(Level.INFO, e.getMessage(), e);
			ch = 36;
		}

		short panid;
		try {
			value = getProperty("lazurite.panid");
			panid = (short)Integer.parseInt(value, 16);
		} catch (MissingResourceException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
			panid = (short)0xffff;
		} catch (NumberFormatException e) {
			logger.log(Level.INFO, e.getMessage(), e);
			panid = (short)0xffff;
		}

		builder = new LazuriteParams.Builder(ch, panid);

		try {
			byte rate;
			value = getProperty("lazurite.rate");
			rate = Byte.parseByte(value);
			builder.rate(rate);
		} catch (MissingResourceException e) {
			logger.log(Level.INFO, e.getMessage(), e);
		} catch (NumberFormatException e) {
			logger.log(Level.INFO, e.getMessage(), e);
		}

		try {
			byte addrType;
			value = getProperty("lazurite.addrtype");
			addrType = Byte.parseByte(value);
			builder.addrType(addrType);
		} catch (MissingResourceException e) {
			logger.log(Level.INFO, e.getMessage(), e);
		} catch (NumberFormatException e) {
			logger.log(Level.INFO, e.getMessage(), e);
		}

		try {
			byte txretry;
			value = getProperty("lazurite.txretry");
			txretry = (byte)Short.parseShort(value);
			builder.txRetry(txretry);
		} catch (MissingResourceException e) {
			logger.log(Level.INFO, e.getMessage(), e);
		} catch (NumberFormatException e) {
			logger.log(Level.INFO, e.getMessage(), e);
		}

		try {
			short txinterval;
			value = getProperty("lazurite.txinterval");
			txinterval = Short.parseShort(value);
			builder.txInterval(txinterval);
		} catch (MissingResourceException e) {
			logger.log(Level.INFO, e.getMessage(), e);
		} catch (NumberFormatException e) {
			logger.log(Level.INFO, e.getMessage(), e);
		}

		return builder.build();
		
	}

	public LazuriteParams getLazuriteParams() {
		return lazuriteParams;
	}
}
