/*
 * Minecraft Forge
 * Copyright (c) 2016-2018.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package shadows.placebo.config;

import static shadows.placebo.config.Property.Type.BOOLEAN;
import static shadows.placebo.config.Property.Type.DOUBLE;
import static shadows.placebo.config.Property.Type.INTEGER;
import static shadows.placebo.config.Property.Type.STRING;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Floats;

import shadows.placebo.Placebo;
import shadows.placebo.platform.Services;

/**
 * This class offers advanced configurations capabilities, allowing to provide
 * various categories for configuration variables.
 */
public class Configuration {
	private static final Logger LOGGER = LogManager.getLogger();

	public static final String CATEGORY_GENERAL = "general";
	public static final String CATEGORY_CLIENT = "client";
	public static final String ALLOWED_CHARS = "._-";
	public static final String DEFAULT_ENCODING = "UTF-8";
	public static final String CATEGORY_SPLITTER = ".";
	private static final Pattern CONFIG_START = Pattern.compile("START: \"([^\\\"]+)\"");
	private static final Pattern CONFIG_END = Pattern.compile("END: \"([^\\\"]+)\"");
	public static final CharMatcher allowedProperties = CharMatcher.forPredicate(Character::isLetterOrDigit).or(CharMatcher.anyOf(ALLOWED_CHARS));

	File file;

	private Map<String, ConfigCategory> categories = new LinkedHashMap<String, ConfigCategory>();

	private boolean caseSensitiveCustomCategories;
	public String defaultEncoding = DEFAULT_ENCODING;
	private String fileName = null;
	public boolean isChild = false;
	private boolean changed = false;
	private String title = "";
	private String mainComment = null;

	public Configuration(File file) {
		this.file = file;
		try {
			this.load();
		} catch (Throwable e) {
			File fileBak = new File(file.getAbsolutePath() + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".errored");
			Placebo.LOGGER.fatal("An exception occurred while loading config file {}. This file will be renamed to {} " + "and a new config file will be generated.", file.getName(), fileBak.getName(), e);

			file.renameTo(fileBak);
			this.load();
		}
	}

	public Configuration(String modid) {
		this(new File(Services.PLATFORM.getConfigDir().toFile(), modid + ".cfg"));
	}

	public void setTitle(String title) {
		this.title = Preconditions.checkNotNull(title);
	}

	/**
	 * Sets a comment displayed at the top of the file.
	 * Supports newlines via \n.
	 * @param comment The comment to set.
	 */
	public void setComment(String comment) {
		this.mainComment = comment;
	}

	@Override
	public String toString() {
		return this.file.getAbsolutePath();
	}

	/******************************************************************************************************************
	 *
	 * BOOLEAN gets
	 *
	 *****************************************************************************************************************/

	/**
	 * Gets a boolean Property object without a comment using the default settings.
	 *
	 * @param category the config category
	 * @param key the Property key value
	 * @param defaultValue the default value
	 * @return a boolean Property object without a comment
	 */
	public Property get(String category, String key, boolean defaultValue) {
		return this.get(category, key, defaultValue, null);
	}

	/**
	 * Gets a boolean Property object with a comment using the default settings.
	 *
	 * @param category the config category
	 * @param key the Property key value
	 * @param defaultValue the default value
	 * @param comment a String comment
	 * @return a boolean Property object without a comment
	 */
	public Property get(String category, String key, boolean defaultValue, String comment) {
		Property prop = this.get(category, key, Boolean.toString(defaultValue), comment, BOOLEAN);
		prop.setDefaultValue(Boolean.toString(defaultValue));

		if (!prop.isBooleanValue()) {
			prop.setValue(defaultValue);
		}
		return prop;

	}

	/**
	 * Gets a boolean array Property without a comment using the default settings.
	 *
	 * @param category the config category
	 * @param key the Property key value
	 * @param defaultValues an array containing the default values
	 * @return a boolean array Property without a comment using these defaults: isListLengthFixed = false, maxListLength = -1
	 */
	public Property get(String category, String key, boolean[] defaultValues) {
		return this.get(category, key, defaultValues, null);
	}

	/**
	 * Gets a boolean array Property with a comment using the default settings.
	 *
	 * @param category the config category
	 * @param key the Property key value
	 * @param defaultValues an array containing the default values
	 * @param comment a String comment
	 * @return a boolean array Property with a comment using these defaults: isListLengthFixed = false, maxListLength = -1
	 */
	public Property get(String category, String key, boolean[] defaultValues, String comment) {
		return this.get(category, key, defaultValues, comment, false, -1);
	}

	/**
	 * Gets a boolean array Property with all settings defined.
	 *
	 * @param category the config category
	 * @param key the Property key value
	 * @param defaultValues an array containing the default values
	 * @param comment a String comment
	 * @param isListLengthFixed boolean for whether this array is required to be a specific length (defined by the default value array
	 *            length or maxListLength)
	 * @param maxListLength the maximum length of this array, use -1 for no max length
	 * @return a boolean array Property with all settings defined
	 */
	public Property get(String category, String key, boolean[] defaultValues, String comment, boolean isListLengthFixed, int maxListLength) {
		String[] values = new String[defaultValues.length];
		for (int i = 0; i < defaultValues.length; i++) {
			values[i] = Boolean.toString(defaultValues[i]);
		}

		Property prop = this.get(category, key, values, comment, BOOLEAN);
		prop.setDefaultValues(values);
		prop.setIsListLengthFixed(isListLengthFixed);
		prop.setMaxListLength(maxListLength);

		if (!prop.isBooleanList()) {
			prop.setValues(values);
		}

		return prop;
	}

	/* ****************************************************************************************************************
	 *
	 * INTEGER gets
	 *
	 *****************************************************************************************************************/

	/**
	 * Gets an integer Property object without a comment using default settings.
	 *
	 * @param category the config category
	 * @param key the Property key value
	 * @param defaultValue the default value
	 * @return an integer Property object with default bounds of Integer.MIN_VALUE and Integer.MAX_VALUE
	 */
	public Property get(String category, String key, int defaultValue) {
		return this.get(category, key, defaultValue, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * Gets an integer Property object with a comment using default settings.
	 *
	 * @param category the config category
	 * @param key the Property key value
	 * @param defaultValue the default value
	 * @param comment a String comment
	 * @return an integer Property object with default bounds of Integer.MIN_VALUE and Integer.MAX_VALUE
	 */
	public Property get(String category, String key, int defaultValue, String comment) {
		return this.get(category, key, defaultValue, comment, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * Gets an integer Property object with the defined comment, minimum and maximum bounds.
	 *
	 * @param category the config category
	 * @param key the Property key value
	 * @param defaultValue the default value
	 * @param comment a String comment
	 * @param minValue minimum boundary
	 * @param maxValue maximum boundary
	 * @return an integer Property object with the defined comment, minimum and maximum bounds
	 */
	public Property get(String category, String key, int defaultValue, String comment, int minValue, int maxValue) {
		Property prop = this.get(category, key, Integer.toString(defaultValue), comment, INTEGER);
		prop.setDefaultValue(Integer.toString(defaultValue));
		prop.setMinValue(minValue);
		prop.setMaxValue(maxValue);

		if (!prop.isIntValue()) {
			prop.setValue(defaultValue);
		}
		return prop;
	}

	/**
	 * Gets an integer array Property object without a comment using default settings.
	 *
	 * @param category the config category
	 * @param key the Property key value
	 * @param defaultValues an array containing the default values
	 * @return an integer array Property object with default bounds of Integer.MIN_VALUE and Integer.MAX_VALUE, isListLengthFixed = false,
	 *         maxListLength = -1
	 */
	public Property get(String category, String key, int[] defaultValues) {
		return this.get(category, key, defaultValues, null);
	}

	/**
	 * Gets an integer array Property object with a comment using default settings.
	 *
	 * @param category the config category
	 * @param key the Property key value
	 * @param defaultValues an array containing the default values
	 * @param comment a String comment
	 * @return an integer array Property object with default bounds of Integer.MIN_VALUE and Integer.MAX_VALUE, isListLengthFixed = false,
	 *         maxListLength = -1
	 */
	public Property get(String category, String key, int[] defaultValues, String comment) {
		return this.get(category, key, defaultValues, comment, Integer.MIN_VALUE, Integer.MAX_VALUE, false, -1);
	}

	/**
	 * Gets an integer array Property object with the defined comment, minimum and maximum bounds.
	 *
	 * @param category the config category
	 * @param key the Property key value
	 * @param defaultValues an array containing the default values
	 * @param comment a String comment
	 * @param minValue minimum boundary
	 * @param maxValue maximum boundary
	 * @return an integer array Property object with the defined comment, minimum and maximum bounds, isListLengthFixed
	 *         = false, maxListLength = -1
	 */
	public Property get(String category, String key, int[] defaultValues, String comment, int minValue, int maxValue) {
		return this.get(category, key, defaultValues, comment, minValue, maxValue, false, -1);
	}

	/**
	 * Gets an integer array Property object with all settings defined.
	 *
	 * @param category the config category
	 * @param key the Property key value
	 * @param defaultValues an array containing the default values
	 * @param comment a String comment
	 * @param minValue minimum boundary
	 * @param maxValue maximum boundary
	 * @param isListLengthFixed boolean for whether this array is required to be a specific length (defined by the default value array
	 *            length or maxListLength)
	 * @param maxListLength the maximum length of this array, use -1 for no max length
	 * @return an integer array Property object with all settings defined
	 */
	public Property get(String category, String key, int[] defaultValues, String comment, int minValue, int maxValue, boolean isListLengthFixed, int maxListLength) {
		String[] values = new String[defaultValues.length];
		for (int i = 0; i < defaultValues.length; i++) {
			values[i] = Integer.toString(defaultValues[i]);
		}

		Property prop = this.get(category, key, values, comment, INTEGER);
		prop.setDefaultValues(values);
		prop.setMinValue(minValue);
		prop.setMaxValue(maxValue);
		prop.setIsListLengthFixed(isListLengthFixed);
		prop.setMaxListLength(maxListLength);

		if (!prop.isIntList()) {
			prop.setValues(values);
		}

		return prop;
	}

	/* ****************************************************************************************************************
	 *
	 * DOUBLE gets
	 *
	 *****************************************************************************************************************/

	/**
	 * Gets a double Property object without a comment using default settings.
	 *
	 * @param category the config category
	 * @param key the Property key value
	 * @param defaultValue the default value
	 * @return a double Property object with default bounds of Double.MIN_VALUE and Double.MAX_VALUE
	 */
	public Property get(String category, String key, double defaultValue) {
		return this.get(category, key, defaultValue, null);
	}

	/**
	 * Gets a double Property object with a comment using default settings.
	 *
	 * @param category the config category
	 * @param key the Property key value
	 * @param defaultValue the default value
	 * @param comment a String comment
	 * @return a double Property object with default bounds of Double.MIN_VALUE and Double.MAX_VALUE
	 */
	public Property get(String category, String key, double defaultValue, String comment) {
		return this.get(category, key, defaultValue, comment, -Double.MAX_VALUE, Double.MAX_VALUE);
	}

	/**
	 * Gets a double Property object with the defined comment, minimum and maximum bounds
	 *
	 * @param category the config category
	 * @param key the Property key value
	 * @param defaultValue the default value
	 * @param comment a String comment
	 * @param minValue minimum boundary
	 * @param maxValue maximum boundary
	 * @return a double Property object with the defined comment, minimum and maximum bounds
	 */
	public Property get(String category, String key, double defaultValue, String comment, double minValue, double maxValue) {
		Property prop = this.get(category, key, Double.toString(defaultValue), comment, DOUBLE);
		prop.setDefaultValue(Double.toString(defaultValue));
		prop.setMinValue(minValue);
		prop.setMaxValue(maxValue);

		if (!prop.isDoubleValue()) {
			prop.setValue(defaultValue);
		}
		return prop;
	}

	/**
	 * Gets a double array Property object without a comment using default settings.
	 *
	 * @param category the config category
	 * @param key the Property key value
	 * @param defaultValues an array containing the default values
	 * @return a double array Property object with default bounds of Double.MIN_VALUE and Double.MAX_VALUE, isListLengthFixed = false,
	 *         maxListLength = -1
	 */
	public Property get(String category, String key, double[] defaultValues) {
		return this.get(category, key, defaultValues, null);
	}

	/**
	 * Gets a double array Property object without a comment using default settings.
	 *
	 * @param category the config category
	 * @param key the Property key value
	 * @param defaultValues an array containing the default values
	 * @param comment a String comment
	 * @return a double array Property object with default bounds of Double.MIN_VALUE and Double.MAX_VALUE, isListLengthFixed = false,
	 *         maxListLength = -1
	 */
	public Property get(String category, String key, double[] defaultValues, String comment) {
		return this.get(category, key, defaultValues, comment, -Double.MAX_VALUE, Double.MAX_VALUE, false, -1);
	}

	/**
	 * Gets a double array Property object with the defined comment, minimum and maximum bounds.
	 *
	 * @param category the config category
	 * @param key the Property key value
	 * @param defaultValues an array containing the default values
	 * @param comment a String comment
	 * @param minValue minimum boundary
	 * @param maxValue maximum boundary
	 * @return a double array Property object with the defined comment, minimum and maximum bounds, isListLengthFixed =
	 *         false, maxListLength = -1
	 */
	public Property get(String category, String key, double[] defaultValues, String comment, double minValue, double maxValue) {
		return this.get(category, key, defaultValues, comment, minValue, maxValue, false, -1);
	}

	/**
	 * Gets a double array Property object with all settings defined.
	 *
	 * @param category the config category
	 * @param key the Property key value
	 * @param defaultValues an array containing the default values
	 * @param comment a String comment
	 * @param minValue minimum boundary
	 * @param maxValue maximum boundary
	 * @param isListLengthFixed boolean for whether this array is required to be a specific length (defined by the default value array
	 *            length or maxListLength)
	 * @param maxListLength the maximum length of this array, use -1 for no max length
	 * @return a double array Property object with all settings defined
	 */
	public Property get(String category, String key, double[] defaultValues, String comment, double minValue, double maxValue, boolean isListLengthFixed, int maxListLength) {
		String[] values = new String[defaultValues.length];
		for (int i = 0; i < defaultValues.length; i++) {
			values[i] = Double.toString(defaultValues[i]);
		}

		Property prop = this.get(category, key, values, comment, DOUBLE);
		prop.setDefaultValues(values);
		prop.setMinValue(minValue);
		prop.setMaxValue(maxValue);
		prop.setIsListLengthFixed(isListLengthFixed);
		prop.setMaxListLength(maxListLength);

		if (!prop.isDoubleList()) {
			prop.setValues(values);
		}

		return prop;
	}

	/* ****************************************************************************************************************
	 *
	 * STRING gets
	 *
	 *****************************************************************************************************************/

	/**
	 * Gets a string Property without a comment using the default settings.
	 *
	 * @param category the config category
	 * @param key the Property key value
	 * @param defaultValue the default value
	 * @return a string Property with validationPattern = null, validValues = null
	 */
	public Property get(String category, String key, String defaultValue) {
		return this.get(category, key, defaultValue, null);
	}

	/**
	 * Gets a string Property with a comment using the default settings.
	 *
	 * @param category the config category
	 * @param key the Property key value
	 * @param defaultValue the default value
	 * @param comment a String comment
	 * @return a string Property with validationPattern = null, validValues = null
	 */
	public Property get(String category, String key, String defaultValue, String comment) {
		return this.get(category, key, defaultValue, comment, STRING);
	}

	/**
	 * Gets a string Property with a comment using the defined validationPattern and otherwise default settings.
	 *
	 * @param category the config category
	 * @param key the Property key value
	 * @param defaultValue the default value
	 * @param comment a String comment
	 * @param validationPattern a Pattern object for input validation
	 * @return a string Property with the defined validationPattern, validValues = null
	 */
	public Property get(String category, String key, String defaultValue, String comment, Pattern validationPattern) {
		Property prop = this.get(category, key, defaultValue, comment, STRING);
		prop.setValidationPattern(validationPattern);
		return prop;
	}

	/**
	 * Gets a string Property with a comment using the defined validValues array and otherwise default settings.
	 *
	 * @param category the config category
	 * @param key the Property key value
	 * @param defaultValue the default value
	 * @param comment a String comment
	 * @param validValues an array of valid values that this Property can be set to. If an array is provided the Config GUI control will be
	 *            a value cycle button.
	 * @return a string Property with the defined validValues array, validationPattern = null
	 */
	public Property get(String category, String key, String defaultValue, String comment, String[] validValues) {
		Property prop = this.get(category, key, defaultValue, comment, STRING);
		prop.setValidValues(validValues);
		return prop;
	}

	/**
	 * Gets a string array Property without a comment using the default settings.
	 *
	 * @param category the config category
	 * @param key the Property key value
	 * @param defaultValues an array containing the default values
	 * @return a string array Property with validationPattern = null, isListLengthFixed = false, maxListLength = -1
	 */
	public Property get(String category, String key, String[] defaultValues) {
		return this.get(category, key, defaultValues, null, false, -1, null);
	}

	/**
	 * Gets a string array Property with a comment using the default settings.
	 *
	 * @param category the config category
	 * @param key the Property key value
	 * @param defaultValues an array containing the default values
	 * @param comment a String comment
	 * @return a string array Property with validationPattern = null, isListLengthFixed = false, maxListLength = -1
	 */
	public Property get(String category, String key, String[] defaultValues, String comment) {
		return this.get(category, key, defaultValues, comment, false, -1, null);
	}

	/**
	 * Gets a string array Property with a comment using the defined validationPattern and otherwise default settings.
	 *
	 * @param category the config category
	 * @param key the Property key value
	 * @param defaultValues an array containing the default values
	 * @param comment a String comment
	 * @param validationPattern a Pattern object for input validation
	 * @return a string array Property with the defined validationPattern, isListLengthFixed = false, maxListLength = -1
	 */
	public Property get(String category, String key, String[] defaultValues, String comment, Pattern validationPattern) {
		return this.get(category, key, defaultValues, comment, false, -1, validationPattern);
	}

	/**
	 * Gets a string array Property with a comment with all settings defined.
	 *
	 * @param category the config category
	 * @param key the Property key value
	 * @param defaultValues an array containing the default values
	 * @param comment a String comment
	 * @param isListLengthFixed boolean for whether this array is required to be a specific length (defined by the default value array
	 *            length or maxListLength)
	 * @param maxListLength the maximum length of this array, use -1 for no max length
	 * @param validationPattern a Pattern object for input validation
	 * @return a string array Property with a comment with all settings defined
	 */
	public Property get(String category, String key, String[] defaultValues, String comment, boolean isListLengthFixed, int maxListLength, Pattern validationPattern) {
		Property prop = this.get(category, key, defaultValues, comment, STRING);
		prop.setIsListLengthFixed(isListLengthFixed);
		prop.setMaxListLength(maxListLength);
		prop.setValidationPattern(validationPattern);
		return prop;
	}

	/* ****************************************************************************************************************
	 *
	 * GENERIC gets
	 *
	 *****************************************************************************************************************/

	/**
	 * Gets a Property object of the specified type using default settings.
	 *
	 * @param category the config category
	 * @param key the Property key value
	 * @param defaultValue the default value
	 * @param comment a String comment
	 * @param type a Property.Type enum value
	 * @return a Property object of the specified type using default settings
	 */
	public Property get(String category, String key, String defaultValue, String comment, Property.Type type) {
		ConfigCategory cat = this.getCategory(category);

		if (cat.containsKey(key)) {
			Property prop = cat.get(key);

			if (prop.getType() == null) {
				prop = new Property(prop.getName(), prop.getString(), type);
				cat.put(key, prop);
			}

			prop.setDefaultValue(defaultValue);
			prop.setComment(comment);
			return prop;
		} else if (defaultValue != null) {
			Property prop = new Property(key, defaultValue, type);
			prop.setValue(defaultValue); //Set and mark as dirty to signify it should save
			cat.put(key, prop);
			prop.setDefaultValue(defaultValue);
			prop.setComment(comment);
			return prop;
		} else {
			return null;
		}
	}

	/**
	 * Gets a list (array) Property object of the specified type using default settings.
	 *
	 * @param category the config category
	 * @param key the Property key value
	 * @param defaultValues an array containing the default values
	 * @param comment a String comment
	 * @param type a Property.Type enum value
	 * @return a list (array) Property object of the specified type using default settings
	 */
	public Property get(String category, String key, String[] defaultValues, String comment, Property.Type type) {
		ConfigCategory cat = this.getCategory(category);

		if (cat.containsKey(key)) {
			Property prop = cat.get(key);

			if (prop.getType() == null) {
				prop = new Property(prop.getName(), prop.getString(), type);
				cat.put(key, prop);
			}

			prop.setDefaultValues(defaultValues);
			prop.setComment(comment);

			return prop;
		} else if (defaultValues != null) {
			Property prop = new Property(key, defaultValues, type);
			prop.setDefaultValues(defaultValues);
			prop.setComment(comment);
			cat.put(key, prop);
			return prop;
		} else {
			return null;
		}
	}

	/* ****************************************************************************************************************
	 *
	 * Other methods
	 *
	 *************************************************************************************************************** */

	public boolean hasCategory(String category) {
		if (!this.caseSensitiveCustomCategories) category = category.toLowerCase(Locale.ENGLISH);
		return this.categories.get(category) != null;
	}

	public boolean hasKey(String category, String key) {
		if (!this.caseSensitiveCustomCategories) category = category.toLowerCase(Locale.ENGLISH);
		ConfigCategory cat = this.categories.get(category);
		return cat != null && cat.containsKey(key);
	}

	public void load() {
		BufferedReader buffer = null;
		UnicodeInputStreamReader input = null;
		try {
			if (this.file.getParentFile() != null) {
				this.file.getParentFile().mkdirs();
			}

			if (!this.file.exists()) {
				// Either a previous load attempt failed or the file is new; clear maps
				this.categories.clear();
				if (!this.file.createNewFile()) return;
			}

			if (this.file.canRead()) {
				input = new UnicodeInputStreamReader(new FileInputStream(this.file), this.defaultEncoding);
				this.defaultEncoding = input.getEncoding();
				buffer = new BufferedReader(input);

				String line;
				ConfigCategory currentCat = null;
				Property.Type type = null;
				ArrayList<String> tmpList = null;
				int lineNum = 0;
				String name = null;

				while (true) {
					lineNum++;
					line = buffer.readLine();

					if (line == null) {
						break;
					}

					Matcher start = CONFIG_START.matcher(line);
					Matcher end = CONFIG_END.matcher(line);

					if (start.matches()) {
						this.fileName = start.group(1);
						this.categories = new TreeMap<String, ConfigCategory>();
						continue;
					} else if (end.matches()) {
						this.fileName = end.group(1);
						//	Configuration child = new Configuration(null);
						//	child.categories = categories;
						//	this.children.put(fileName, child);
						continue;
					}

					int nameStart = -1, nameEnd = -1;
					boolean skip = false;
					boolean quoted = false;
					boolean isFirstNonWhitespaceCharOnLine = true;

					for (int i = 0; i < line.length() && !skip; ++i) {
						if (Character.isLetterOrDigit(line.charAt(i)) || ALLOWED_CHARS.indexOf(line.charAt(i)) != -1 || quoted && line.charAt(i) != '"') {
							if (nameStart == -1) {
								nameStart = i;
							}

							nameEnd = i;
							isFirstNonWhitespaceCharOnLine = false;
						} else if (Character.isWhitespace(line.charAt(i))) {
							// ignore space characters
						} else {
							switch (line.charAt(i)) {
							case '#':
								if (tmpList != null) // allow special characters as part of string lists
									break;
								skip = true;
								continue;

							case '"':
								if (tmpList != null) // allow special characters as part of string lists
									break;
								if (quoted) {
									quoted = false;
								}
								if (!quoted && nameStart == -1) {
									quoted = true;
								}
								break;

							case '{':
								if (tmpList != null) // allow special characters as part of string lists
									break;
								name = line.substring(nameStart, nameEnd + 1);
								if (!this.caseSensitiveCustomCategories) name = name.toLowerCase(Locale.ENGLISH);
								String qualifiedName = ConfigCategory.getQualifiedName(name, currentCat);

								ConfigCategory cat = this.categories.get(qualifiedName);
								if (cat == null) {
									currentCat = new ConfigCategory(name, currentCat);
									this.categories.put(qualifiedName, currentCat);
								} else {
									currentCat = cat;
								}
								name = null;

								break;

							case '}':
								if (tmpList != null) // allow special characters as part of string lists
									break;
								if (currentCat == null) {
									throw new RuntimeException(String.format("Config file corrupt, attempted to close to many categories '%s:%d'", this.fileName, lineNum));
								}
								currentCat = currentCat.parent;
								break;

							case '=':
								if (tmpList != null) // allow special characters as part of string lists
									break;
								name = line.substring(nameStart, nameEnd + 1);

								if (currentCat == null) {
									throw new RuntimeException(String.format("'%s' has no scope in '%s:%d'", name, this.fileName, lineNum));
								}

								Property prop = new Property(name, line.substring(i + 1), type, true);
								i = line.length();

								currentCat.put(name, prop);

								break;

							case ':':
								if (tmpList != null) // allow special characters as part of string lists
									break;
								type = Property.Type.tryParse(line.substring(nameStart, nameEnd + 1).charAt(0));
								nameStart = nameEnd = -1;
								break;

							case '<':
								if (tmpList != null && i + 1 == line.length() || tmpList == null && i + 1 != line.length()) {
									throw new RuntimeException(String.format("Malformed list property \"%s:%d\"", this.fileName, lineNum));
								} else if (i + 1 == line.length()) {
									name = line.substring(nameStart, nameEnd + 1);

									if (currentCat == null) {
										throw new RuntimeException(String.format("'%s' has no scope in '%s:%d'", name, this.fileName, lineNum));
									}

									tmpList = new ArrayList<String>();

									skip = true;
								}

								break;

							case '>':
								if (tmpList == null) {
									throw new RuntimeException(String.format("Malformed list property \"%s:%d\"", this.fileName, lineNum));
								}

								if (isFirstNonWhitespaceCharOnLine) {
									currentCat.put(name, new Property(name, tmpList.toArray(new String[tmpList.size()]), type));
									name = null;
									tmpList = null;
									type = null;
								} // else allow special characters as part of string lists
								break;

							case '~':
								if (tmpList != null) // allow special characters as part of string lists
									break;

								break;

							default:
								if (tmpList != null) // allow special characters as part of string lists
									break;
								throw new RuntimeException(String.format("Unknown character '%s' in '%s:%d'", line.charAt(i), this.fileName, lineNum));
							}
							isFirstNonWhitespaceCharOnLine = false;
						}
					}

					if (quoted) {
						throw new RuntimeException(String.format("Unmatched quote in '%s:%d'", this.fileName, lineNum));
					} else if (tmpList != null && !skip) {
						tmpList.add(line.trim());
					}
				}
			}
		} catch (IOException e) {
			LOGGER.error("Error while loading config {}.", this.fileName, e);
		} finally {
			IOUtils.closeQuietly(buffer);
			IOUtils.closeQuietly(input);
		}

		this.resetChangedState();
	}

	public void save() {

		try {
			if (this.file.getParentFile() != null) {
				this.file.getParentFile().mkdirs();
			}

			if (!this.file.exists() && !this.file.createNewFile()) { return; }

			if (this.file.canWrite()) {
				FileOutputStream fos = new FileOutputStream(this.file);
				BufferedWriter buffer = new BufferedWriter(new OutputStreamWriter(fos, this.defaultEncoding));

				buffer.write("# File Specification: https://gist.github.com/Shadows-of-Fire/88ac714a758636c57a52e32ace5474c1");
				buffer.newLine();
				buffer.newLine();
				buffer.write(String.format("# %s", this.title != null ? this.title : "Configuration File"));
				buffer.newLine();
				buffer.newLine();
				if (this.mainComment != null) {
					writeComment(buffer, this.mainComment);
					buffer.newLine();
					buffer.newLine();
				}
				this.save(buffer);

				buffer.close();
				fos.close();
			}
		} catch (IOException e) {
			LOGGER.error("Error while saving config {}.", this.fileName, e);
		}
	}

	public static void writeComment(BufferedWriter writer, String comment) throws IOException {
		if (comment == null || comment.isEmpty()) return;
		String[] split = comment.split("\\n");
		for (int i = 0; i < split.length; i++) {
			writer.write("# " + split[i]);
			writer.newLine();
		}
	}

	private void save(BufferedWriter out) throws IOException {
		for (ConfigCategory cat : this.categories.values()) {
			if (!cat.isChild()) {
				cat.write(out, 0);
				out.newLine();
			}
		}
	}

	public ConfigCategory getCategory(String category) {
		if (!this.caseSensitiveCustomCategories) category = category.toLowerCase(Locale.ENGLISH);

		ConfigCategory ret = this.categories.get(category);

		if (ret == null) {
			if (category.contains(CATEGORY_SPLITTER)) {
				String[] hierarchy = category.split("\\" + CATEGORY_SPLITTER);
				ConfigCategory parent = this.categories.get(hierarchy[0]);

				if (parent == null) {
					parent = new ConfigCategory(hierarchy[0]);
					this.categories.put(parent.getQualifiedName(), parent);
					this.changed = true;
				}

				for (int i = 1; i < hierarchy.length; i++) {
					String name = ConfigCategory.getQualifiedName(hierarchy[i], parent);
					ConfigCategory child = this.categories.get(name);

					if (child == null) {
						child = new ConfigCategory(hierarchy[i], parent);
						this.categories.put(name, child);
						this.changed = true;
					}

					ret = child;
					parent = child;
				}
			} else {
				ret = new ConfigCategory(category);
				this.categories.put(category, ret);
				this.changed = true;
			}
		}

		return ret;
	}

	public void removeCategory(ConfigCategory category) {
		for (ConfigCategory child : category.getChildren()) {
			this.removeCategory(child);
		}

		if (this.categories.containsKey(category.getQualifiedName())) {
			this.categories.remove(category.getQualifiedName());
			if (category.parent != null) {
				category.parent.removeChild(category);
			}
			this.changed = true;
		}
	}

	/**
	 * Adds a comment to the specified ConfigCategory object
	 *
	 * @param category the config category
	 * @param comment a String comment
	 */
	public Configuration setCategoryComment(String category, String comment) {
		this.getCategory(category).setComment(comment);
		return this;
	}

	/**
	 * Adds a language key to the specified ConfigCategory object
	 *
	 * @param category the config category
	 * @param langKey a language key string such as configcategory.general
	 */
	public Configuration setCategoryLanguageKey(String category, String langKey) {
		this.getCategory(category).setLanguageKey(langKey);
		return this;
	}

	/**
	 * Sets the custom IConfigEntry class that should be used in place of the standard entry class (which is just a button that
	 * navigates into the category). This class MUST provide a constructor with the following parameter types: {@link GuiConfig} (the parent
	 * GuiConfig screen will be provided), {@link GuiConfigEntries} (the parent GuiConfigEntries will be provided), {@link IConfigElement}
	 * (the IConfigElement for this Property will be provided).
	 *
	 * @see GuiConfigEntries.ListEntryBase
	 * @see GuiConfigEntries.StringEntry
	 * @see GuiConfigEntries.BooleanEntry
	 * @see GuiConfigEntries.DoubleEntry
	 * @see GuiConfigEntries.IntegerEntry
	 * /
	public Configuration setCategoryConfigEntryClass(String category, Class<? extends IConfigEntry> clazz)
	{
	    getCategory(category).setConfigEntryClass(clazz);
	    return this;
	}
	*/
	/**
	 * Sets the flag for whether or not this category can be edited while a world is running. Care should be taken to ensure
	 * that only properties that are truly dynamic can be changed from the in-game options menu. Only set this flag to
	 * true if all child properties/categories are unable to be modified while a world is running.
	 */
	public Configuration setCategoryRequiresWorldRestart(String category, boolean requiresWorldRestart) {
		this.getCategory(category).setRequiresWorldRestart(requiresWorldRestart);
		return this;
	}

	/**
	 * Sets whether or not this ConfigCategory requires Minecraft to be restarted when changed.
	 * Defaults to false. Only set this flag to true if ALL child properties/categories require
	 * Minecraft to be restarted when changed. Setting this flag will also prevent modification
	 * of the child properties/categories while a world is running.
	 */
	public Configuration setCategoryRequiresMcRestart(String category, boolean requiresMcRestart) {
		this.getCategory(category).setRequiresMcRestart(requiresMcRestart);
		return this;
	}

	/**
	 * Sets the order that direct child properties of this config category will be written to the config file and will be displayed in
	 * config GUIs.
	 */
	public Configuration setCategoryPropertyOrder(String category, List<String> propOrder) {
		this.getCategory(category).setPropertyOrder(propOrder);
		return this;
	}

	public static class UnicodeInputStreamReader extends Reader {
		private final InputStreamReader input;

		public UnicodeInputStreamReader(InputStream source, String encoding) throws IOException {
			String enc = encoding;
			byte[] data = new byte[4];

			PushbackInputStream pbStream = new PushbackInputStream(source, data.length);
			int read = pbStream.read(data, 0, data.length);
			int size = 0;

			int bom16 = (data[0] & 0xFF) << 8 | data[1] & 0xFF;
			int bom24 = bom16 << 8 | data[2] & 0xFF;
			int bom32 = bom24 << 8 | data[3] & 0xFF;

			if (bom24 == 0xEFBBBF) {
				enc = "UTF-8";
				size = 3;
			} else if (bom16 == 0xFEFF) {
				enc = "UTF-16BE";
				size = 2;
			} else if (bom16 == 0xFFFE) {
				enc = "UTF-16LE";
				size = 2;
			} else if (bom32 == 0x0000FEFF) {
				enc = "UTF-32BE";
				size = 4;
			} else if (bom32 == 0xFFFE0000) //This will never happen as it'll be caught by UTF-16LE,
			{ //but if anyone ever runs across a 32LE file, i'd like to dissect it.
				enc = "UTF-32LE";
				size = 4;
			}

			if (size < read) {
				pbStream.unread(data, size, read - size);
			}

			this.input = new InputStreamReader(pbStream, enc);
		}

		public String getEncoding() {
			return this.input.getEncoding();
		}

		@Override
		public int read(char[] cbuf, int off, int len) throws IOException {
			return this.input.read(cbuf, off, len);
		}

		@Override
		public void close() throws IOException {
			this.input.close();
		}
	}

	public boolean hasChanged() {
		if (this.changed) return true;

		for (ConfigCategory cat : this.categories.values()) {
			if (cat.hasChanged()) return true;
		}

		return false;
	}

	private void resetChangedState() {
		this.changed = false;
		for (ConfigCategory cat : this.categories.values()) {
			cat.resetChangedState();
		}
	}

	public Set<String> getCategoryNames() {
		return ImmutableSet.copyOf(this.categories.keySet());
	}

	/**
	 * Renames a property in a given category.
	 *
	 * @param category the category in which the property resides
	 * @param oldPropName the existing property name
	 * @param newPropName the new property name
	 * @return true if the category and property exist, false otherwise
	 */
	public boolean renameProperty(String category, String oldPropName, String newPropName) {
		if (this.hasCategory(category)) {
			ConfigCategory cat = this.getCategory(category);
			if (cat.containsKey(oldPropName) && !oldPropName.equalsIgnoreCase(newPropName)) {
				Property prop = cat.remove(oldPropName);
				prop.setName(newPropName);
				cat.put(newPropName, prop);
				return true;
			}
		}
		return false;
	}

	/**
	 * Moves a property from one category to another.
	 *
	 * @param oldCategory the category the property currently resides in
	 * @param propName the name of the property to move
	 * @param newCategory the category the property should be moved to
	 * @return true if the old category and property exist, false otherwise
	 */
	public boolean moveProperty(String oldCategory, String propName, String newCategory) {
		if (!oldCategory.equals(newCategory)) if (this.hasCategory(oldCategory)) if (this.getCategory(oldCategory).containsKey(propName)) {
			this.getCategory(newCategory).put(propName, this.getCategory(oldCategory).remove(propName));
			return true;
		}
		return false;
	}

	/**
	 * Copies property objects from another Configuration object to this one using the list of category names. Properties that only exist in the
	 * "from" object are ignored. Pass null for the ctgys array to include all categories.
	 */
	public void copyCategoryProps(Configuration fromConfig, String[] ctgys) {
		if (ctgys == null) ctgys = this.getCategoryNames().toArray(new String[this.getCategoryNames().size()]);

		for (String ctgy : ctgys)
			if (fromConfig.hasCategory(ctgy) && this.hasCategory(ctgy)) {
				ConfigCategory thiscc = this.getCategory(ctgy);
				ConfigCategory fromcc = fromConfig.getCategory(ctgy);
				for (Entry<String, Property> entry : thiscc.getValues().entrySet())
					if (fromcc.containsKey(entry.getKey())) thiscc.put(entry.getKey(), fromcc.get(entry.getKey()));
			}
	}

	/**
	 * Creates a string property.
	 *
	 * @param name Name of the property.
	 * @param category Category of the property.
	 * @param defaultValue Default value of the property.
	 * @param comment A brief description what the property does.
	 * @return The value of the new string property.
	 */
	public String getString(String name, String category, String defaultValue, String comment) {
		return this.getString(name, category, defaultValue, comment, name, null);
	}

	/**
	 * Creates a string property.
	 *
	 * @param name Name of the property.
	 * @param category Category of the property.
	 * @param defaultValue Default value of the property.
	 * @param comment A brief description what the property does.
	 * @param langKey A language key used for localization of GUIs
	 * @return The value of the new string property.
	 */
	public String getString(String name, String category, String defaultValue, String comment, String langKey) {
		return this.getString(name, category, defaultValue, comment, langKey, null);
	}

	/**
	 * Creates a string property.
	 *
	 * @param name Name of the property.
	 * @param category Category of the property.
	 * @param defaultValue Default value of the property.
	 * @param comment A brief description what the property does.
	 * @return The value of the new string property.
	 */
	public String getString(String name, String category, String defaultValue, String comment, Pattern pattern) {
		return this.getString(name, category, defaultValue, comment, name, pattern);
	}

	/**
	 * Creates a string property.
	 *
	 * @param name Name of the property.
	 * @param category Category of the property.
	 * @param defaultValue Default value of the property.
	 * @param comment A brief description what the property does.
	 * @param langKey A language key used for localization of GUIs
	 * @return The value of the new string property.
	 */
	public String getString(String name, String category, String defaultValue, String comment, String langKey, Pattern pattern) {
		Property prop = this.get(category, name, defaultValue);
		prop.setLanguageKey(langKey);
		prop.setValidationPattern(pattern);
		prop.setComment(comment + "\nDefault: " + defaultValue);
		return prop.getString();
	}

	/**
	 * Creates a string property.
	 *
	 * @param name Name of the property.
	 * @param category Category of the property.
	 * @param defaultValue Default value of the property.
	 * @param comment A brief description what the property does.
	 * @param validValues A list of valid values that this property can be set to.
	 * @return The value of the new string property.
	 */
	public String getString(String name, String category, String defaultValue, String comment, String[] validValues) {
		return this.getString(name, category, defaultValue, comment, validValues, name);
	}

	/**
	 * Creates a string property.
	 *
	 * @param name Name of the property.
	 * @param category Category of the property.
	 * @param defaultValue Default value of the property.
	 * @param comment A brief description what the property does.
	 * @param validValues A list of valid values that this property can be set to.
	 * @param langKey A language key used for localization of GUIs
	 * @return The value of the new string property.
	 */
	public String getString(String name, String category, String defaultValue, String comment, String[] validValues, String langKey) {
		Property prop = this.get(category, name, defaultValue);
		prop.setValidValues(validValues);
		prop.setLanguageKey(langKey);
		prop.setComment(comment + "\nDefault: " + defaultValue);
		return prop.getString();
	}

	/**
	 * Creates a string list property.
	 *
	 * @param name Name of the property.
	 * @param category Category of the property.
	 * @param defaultValues Default values of the property.
	 * @param comment A brief description what the property does.
	 * @return The value of the new string property.
	 */
	public String[] getStringList(String name, String category, String[] defaultValues, String comment) {
		return this.getStringList(name, category, defaultValues, comment, null, name);
	}

	/**
	 * Creates a string list property.
	 *
	 * @param name Name of the property.
	 * @param category Category of the property.
	 * @param defaultValue Default value of the property.
	 * @param comment A brief description what the property does.
	 * @return The value of the new string property.
	 */
	public String[] getStringList(String name, String category, String[] defaultValue, String comment, String[] validValues) {
		return this.getStringList(name, category, defaultValue, comment, validValues, name);
	}

	/**
	 * Creates a string list property.
	 *
	 * @param name Name of the property.
	 * @param category Category of the property.
	 * @param defaultValue Default value of the property.
	 * @param comment A brief description what the property does.
	 * @return The value of the new string property.
	 */
	public String[] getStringList(String name, String category, String[] defaultValue, String comment, String[] validValues, String langKey) {
		Property prop = this.get(category, name, defaultValue);
		prop.setLanguageKey(langKey);
		prop.setValidValues(validValues);
		prop.setComment(comment + "\nDefault: " + toComment(prop.getDefaults()));
		return prop.getStringList();
	}

	private static String toComment(String[] values) {
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < values.length; i++) {
			sb.append(values[i].toString());
			if (i != values.length - 1) {
				sb.append("], [");
			} else {
				sb.append("]");
			}
		}
		return sb.toString();
	}

	/**
	 * Creates a boolean property.
	 *
	 * @param name Name of the property.
	 * @param category Category of the property.
	 * @param defaultValue Default value of the property.
	 * @param comment A brief description what the property does.
	 * @return The value of the new boolean property.
	 */
	public boolean getBoolean(String name, String category, boolean defaultValue, String comment) {
		return this.getBoolean(name, category, defaultValue, comment, name);
	}

	/**
	 * Creates a boolean property.
	 *
	 * @param name Name of the property.
	 * @param category Category of the property.
	 * @param defaultValue Default value of the property.
	 * @param comment A brief description what the property does.
	 * @param langKey A language key used for localization of GUIs
	 * @return The value of the new boolean property.
	 */
	public boolean getBoolean(String name, String category, boolean defaultValue, String comment, String langKey) {
		Property prop = this.get(category, name, defaultValue);
		prop.setLanguageKey(langKey);
		prop.setComment(comment + "\nDefault: " + defaultValue);
		return prop.getBoolean(defaultValue);
	}

	/**
	 * Creates a integer property.
	 *
	 * @param name Name of the property.
	 * @param category Category of the property.
	 * @param defaultValue Default value of the property.
	 * @param minValue Minimum value of the property.
	 * @param maxValue Maximum value of the property.
	 * @param comment A brief description what the property does.
	 * @return The value of the new integer property.
	 */
	public int getInt(String name, String category, int defaultValue, int minValue, int maxValue, String comment) {
		return this.getInt(name, category, defaultValue, minValue, maxValue, comment, name);
	}

	/**
	 * Creates a integer property.
	 *
	 * @param name Name of the property.
	 * @param category Category of the property.
	 * @param defaultValue Default value of the property.
	 * @param minValue Minimum value of the property.
	 * @param maxValue Maximum value of the property.
	 * @param comment A brief description what the property does.
	 * @param langKey A language key used for localization of GUIs
	 * @return The value of the new integer property.
	 */
	public int getInt(String name, String category, int defaultValue, int minValue, int maxValue, String comment, String langKey) {
		Property prop = this.get(category, name, defaultValue);
		prop.setLanguageKey(langKey);
		prop.setComment(comment + "\nDefault: " + defaultValue + "; " + "Range: [" + minValue + " ~ " + maxValue + "]");
		prop.setMinValue(minValue);
		prop.setMaxValue(maxValue);
		return prop.getInt(defaultValue) < minValue ? minValue : prop.getInt(defaultValue) > maxValue ? maxValue : prop.getInt(defaultValue);
	}

	/**
	 * Creates a float property.
	 *
	 * @param name Name of the property.
	 * @param category Category of the property.
	 * @param defaultValue Default value of the property.
	 * @param minValue Minimum value of the property.
	 * @param maxValue Maximum value of the property.
	 * @param comment A brief description what the property does.
	 * @return The value of the new float property.
	 */
	public float getFloat(String name, String category, float defaultValue, float minValue, float maxValue, String comment) {
		return this.getFloat(name, category, defaultValue, minValue, maxValue, comment, name);
	}

	/**
	 * Creates a float property.
	 *
	 * @param name Name of the property.
	 * @param category Category of the property.
	 * @param defaultValue Default value of the property.
	 * @param minValue Minimum value of the property.
	 * @param maxValue Maximum value of the property.
	 * @param comment A brief description what the property does.
	 * @param langKey A language key used for localization of GUIs
	 * @return The value of the new float property.
	 */
	public float getFloat(String name, String category, float defaultValue, float minValue, float maxValue, String comment, String langKey) {
		Property prop = this.get(category, name, Float.toString(defaultValue), name);
		prop.setLanguageKey(langKey);
		prop.setComment(comment + "\nDefault: " + defaultValue + "; " + "Range: [" + minValue + " ~ " + maxValue + "]");
		prop.setMinValue(minValue);
		prop.setMaxValue(maxValue);
		try {
			float parseFloat = Float.parseFloat(prop.getString());
			return Floats.constrainToRange(parseFloat, minValue, maxValue);
		} catch (Exception e) {
			LOGGER.error("Failed to get float for {}/{}", name, category, e);
		}
		return defaultValue;
	}

	public File getConfigFile() {
		return this.file;
	}
}