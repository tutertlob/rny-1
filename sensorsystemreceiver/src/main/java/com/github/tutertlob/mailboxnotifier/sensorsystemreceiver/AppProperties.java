package com.github.tutertlob.mailboxnotifier.sensorsystemreceiver;

import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppProperties {
	private static final Logger logger = Logger.getLogger(AppProperties.class.getName());

	private static final String CURRENT_PACKAGE = AppProperties.class.getPackage().getName();

	private static final Map<String, String> defaults = new HashMap<>();

	private static final AppProperties INSTANCE = new AppProperties();

	private static String propertyfile;

	private static ResourceBundle properties;

	private AppProperties() {
		String[] parts = CURRENT_PACKAGE.split("\\.");
		propertyfile = parts[parts.length - 1];

		try {
			properties = ResourceBundle.getBundle(propertyfile);
		} catch (MissingResourceException e) {
			logger.log(Level.WARNING, "The property file doesn't exist.", e);
		}
		setupDefaultProperties();
	}

	private void setupDefaultProperties() {
		defaults.put("serial.baud", "19200");
		defaults.put("serial.port", "/dev/ttyUSB0");
		defaults.put("rest.host", "localhost");
		defaults.put("rest.port", "49152");
		defaults.put("receiver.base", "receiver");
		defaults.put("receiver.resource", "mailbox");
		defaults.put("database", "mongodb");
		defaults.put("database.host", "localhost");
		defaults.put("database.port", "27017");
		defaults.put("database.db", "mailbox_notifier");
		defaults.put("database.collection", "posting_records");
		defaults.put("filestore.path", "./");
	}

	public static final String getProperty(String key) {
		if (properties == null) {
			return defaults.get(key);
		}

		try {
			return properties.getString(key);
		} catch (NullPointerException e) {
			logger.log(Level.WARNING, "The argument key is null.", e);
			throw e;
		} catch (MissingResourceException e) {
			logger.log(Level.WARNING, String.format(
					"The property of specified key couldn't be found in the property file '%s'.", propertyfile), e);

			String property = defaults.get(key);
			if (property == null) {
				String msg = String.format("The property %s is not supported for this application.", key);
				logger.warning(msg);
				throw e;
			}
			logger.info(String.format("Default property value '%s' be used instead.", property));

			return property;
		} catch (ClassCastException e) {
			logger.log(Level.WARNING, String.format("The key %s's property contains non strings.", key), e);

			String property = defaults.get(key);
			if (property == null) {
				String msg = String.format("The key %s is not supported for this application.", key);
				logger.log(Level.WARNING, msg, e);
				throw new IllegalArgumentException(msg, e);
			}
			logger.info(String.format("Default property '%s' be used instead.", property));

			return property;
		}
	}
}
