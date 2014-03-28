package org.openlca.core.model;

import org.junit.Assert;
import org.junit.Test;

public class VersionTest {

	@Test
	public void testMajor() {
		Assert.assertEquals(0, new Version(0L).getMajor());
		Assert.assertEquals(10, new Version(0x0000_000a_0000_0000L).getMajor());
		long value = System.currentTimeMillis();
		Version version = new Version(value);
		version.setMajor((short) 255);
		Assert.assertEquals(255, version.getMajor());
		version.incMajor();
		Assert.assertEquals(256, version.getMajor());
		Assert.assertEquals(0, version.getMinor());
		Assert.assertEquals(0, version.getUpdate());
	}

	@Test
	public void testMinor() {
		Assert.assertEquals(0, new Version(0L).getMinor());
		Assert.assertEquals(255, new Version(0x0000_000a_00ff_0000L).getMinor());
		long value = System.currentTimeMillis();
		Version version = new Version(value);
		version.setMinor((short) 2000);
		Assert.assertEquals(2000, version.getMinor());
		version.incMinor();
		Assert.assertEquals(2001, version.getMinor());
		Assert.assertEquals(0, version.getUpdate());
	}

	@Test
	public void testUpdate() {
		Assert.assertEquals(0, new Version(0L).getMinor());
		Assert.assertEquals(255,
				new Version(0x0000_000a_00ff_00ffL).getUpdate());
		long value = System.currentTimeMillis();
		Version version = new Version(value);
		version.setUpdate((short) 2000);
		Assert.assertEquals(2000, version.getUpdate());
		version.incUpdate();
		Assert.assertEquals(2001, version.getUpdate());
	}

	@Test
	public void testFromString() {

		Version version = Version.fromString("1");
		Assert.assertEquals(new Version(1, 0, 0), version);

		version = Version.fromString("1.1");
		Assert.assertEquals(new Version(1, 1, 0), version);

		version = Version.fromString("1.1.1");
		Assert.assertEquals(new Version(1, 1, 1), version);

		version = Version.fromString("01.34.087");
		Assert.assertEquals(new Version(1, 34, 87), version);

		version = Version.fromString("00.00.000");
		Assert.assertEquals(new Version(), version);

		version = Version.fromString("32767.32767.32767");
		Assert.assertEquals(new Version(32767, 32767, 32767), version);
	}

	@Test
	public void testToString() {
		Version version = new Version();
		Assert.assertEquals("00.00.000", version.toString());
		version = new Version(1, 1, 1);
		Assert.assertEquals("01.01.001", version.toString());
		version = new Version(32767, 32767, 32767);
		Assert.assertEquals("32767.32767.32767", version.toString());
	}

}
