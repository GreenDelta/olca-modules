package org.openlca.core.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;

/**
 * A helper class for handling versions of root entities in openLCA. We support
 * the following version scheme major.minor.update but store the parts of the
 * version number in a single field. This makes the version number easy to
 * compare, automatically increment (the update part), and can be converted to
 * the ILCD and ecoinvent version schemes.
 */
public class Version {

	private long value;

	public Version(long value) {
		this.value = value;
	}

	public Version(short major, short minor, short update) {
		this.value = 0L;
		setValues(major, minor, update);
	}

	public short getMajor() {
		return (short) ((value >> 32) & 0xffff);
	}

	public void setMajor(short major) {
		setValues(major, getMinor(), getUpdate());
	}

	/**
	 * Increments the major part of the version which resets the minor and
	 * update part to 0.
	 */
	public void incMajor() {
		setValues((short) (getMajor() + 1), (short) 0, (short) 0);
	}

	public short getMinor() {
		return (short) ((value >> 16) & 0xffff);
	}

	public void setMinor(short minor) {
		setValues(getMajor(), minor, getUpdate());
	}

	/**
	 * Increments the minor part of the version which resets the update part to
	 * 0.
	 */
	public void incMinor() {
		setValues(getMajor(), (short) (getMinor() + 1), (short) 0);
	}

	public short getUpdate() {
		return (short) (value & 0xffff);
	}

	public void setUpdate(short update) {
		setValues(getMajor(), getMinor(), update);
	}

	/**
	 * Increments the update part of the version. Normally this is automatically
	 * done by the persistence unit.
	 */
	public void incUpdate() {
		setValues(getMajor(), getMinor(), (short) (getUpdate() + 1));
	}

	public void setValues(short major, short minor, short update) {
		// negative values are not allowed but better we set the first bit to 0.
		short clearFirstBit = 0b0111111111111111;
		long majorL = (0L | (major & clearFirstBit)) << 32;
		long minorL = (0L | (minor & clearFirstBit)) << 16;
		long updateL = 0L | (update & clearFirstBit);
		value = majorL | minorL | updateL;
	}

	public long getValue() {
		return value;
	}

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

	private static void setPartFromString(int i, String[] parts, Version version) {
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

}
