package org.openlca.core.model;

import java.util.Objects;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class for handling versions of root entities in openLCA. We support
 * the following version scheme major.minor.update but store the parts of the
 * version number in a single field. This makes the version number easy to
 * compare, automatically increment (the update part), and can be converted to
 * the ILCD and ecoinvent version schemes.
 *
 * For each version field we allow a value between 0 and 32767 so the minimum
 * version number is 0.0.0 and the maximum number 32767.32767.32767. The exact
 * data type for a version field would be a 2-byte positive integer (short).
 * However, we use integers in the public methods of this class to avoid
 * casting.
 */
public class Version {

	private long value;

	public Version() {
		this(0);
	}

	public Version(long value) {
		this.value = value;
	}

	public Version(int major, int minor, int update) {
		this.value = 0L;
		setParts(major, minor, update);
	}

	public static long valueOf(int major, int minor, int update) {
		return new Version(major, minor, update).value;
	}

	public static String asString(long version) {
		return new Version(version).toString();
	}

	public static void incMajor(RootEntity entity) {
		inc(entity, Version::incMajor);
	}

	public static void incMinor(RootEntity entity) {
		inc(entity, Version::incMinor);
	}

	public static void incUpdate(RootEntity entity) {
		inc(entity, Version::incUpdate);
	}

	public static String format(String version) {
		var v = fromString(version);
		return v.toString();
	}

	private static void inc(RootEntity entity, Consumer<Version> inc) {
		Version v = new Version(entity.version);
		inc.accept(v);
		entity.version = v.getValue();
	}

	public int getMajor() {
		return (int) ((value >> 32) & 0xffff);
	}

	public void setMajor(int major) {
		setParts(major, getMinor(), getUpdate());
	}

	/**
	 * Increments the major part of the version which resets the minor and
	 * update part to 0.
	 */
	public void incMajor() {
		setParts((getMajor() + 1), 0, 0);
	}

	public int getMinor() {
		return (int) ((value >> 16) & 0xffff);
	}

	public void setMinor(int minor) {
		setParts(getMajor(), minor, getUpdate());
	}

	/**
	 * Increments the minor part of the version which resets the update part to
	 * 0.
	 */
	public void incMinor() {
		setParts(getMajor(), (getMinor() + 1), 0);
	}

	public int getUpdate() {
		return (int) (value & 0xffff);
	}

	public void setUpdate(int update) {
		setParts(getMajor(), getMinor(), update);
	}

	/**
	 * Increments the update part of the version. Normally this is automatically
	 * done by the persistence unit.
	 */
	public void incUpdate() {
		setParts(getMajor(), getMinor(), (getUpdate() + 1));
	}

	private void setParts(int major, int minor, int update) {
		// negative values are not allowed but better we set the first bit to 0.
		short clearFirstBit = 0b0111111111111111;
		long majorL = (0L | (((short) major) & clearFirstBit)) << 32;
		long minorL = (0L | (((short) minor) & clearFirstBit)) << 16;
		long updateL = 0L | (((short) update) & clearFirstBit);
		value = majorL | minorL | updateL;
	}

	public long getValue() {
		return value;
	}

	/**
	 * Returns the version from the given string. The string can be an ILCD
	 * compatible version string with 3 parts (major.minor.update, e.g.
	 * 01.01.000) where the two last parts and leading zeros are optional. Thus
	 * valid version strings are for example: 1, 1.1, 01.01.001, 44.1.96
	 */
	public static Version fromString(String s) {
		if (s == null || s.length() == 0)
			return new Version(0);
		try {
			String[] parts = s.trim().split("\\.");
			Version version = new Version(0);
			for (int i = 0; i < parts.length; i++)
				setPartFromString(i, parts, version);
			return version;
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Version.class);
			log.error("failed to parse version " + s, e);
			return new Version(0);
		}
	}

	private static void setPartFromString(int i, String[] parts,
			Version version) {
		short p = Short.parseShort(parts[i]);
		switch (i) {
		case 0:
			version.setMajor(p);
			break;
		case 1:
			version.setMinor(p);
			break;
		case 2:
			version.setUpdate(p);
			break;
		default:
			break;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (!obj.getClass().equals(this.getClass()))
			return false;
		Version other = (Version) obj;
		return this.value == other.value;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(value);
	}

	/**
	 * Converts the version to an ILCD compatible version string with 3 parts:
	 * major.minor.update.
	 */
	@Override
	public String toString() {
		String major = Integer.toString(getMajor());
		if (major.length() == 1)
			major = "0" + major;
		String minor = Integer.toString(getMinor());
		if (minor.length() == 1)
			minor = "0" + minor;
		String update = Integer.toString(getUpdate());
		if (update.length() == 1)
			update = "00" + update;
		else if (update.length() == 2)
			update = "0" + update;
		return major + "." + minor + "." + update;
	}

}
