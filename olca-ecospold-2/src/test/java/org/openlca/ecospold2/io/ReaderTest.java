package org.openlca.ecospold2.io;

import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.openlca.ecospold2.Activity;
import org.openlca.ecospold2.Classification;
import org.openlca.ecospold2.DataSet;

@RunWith(Theories.class)
public class ReaderTest {

	private Logger log = Logger.getLogger("ReaderTest");

	@DataPoints
	public static String[] dataSets = { "sample_ecospold2.xml",
			"sample_child_ecospold2.xml" };

	@Theory
	public void testDataSetNotNull(String file) throws Exception {
		DataSet dataSet = read(file);
		Assert.assertNotNull(dataSet);
	}

	@Theory
	public void testActivity(String file) throws Exception {
		DataSet dataSet = read(file);
		Activity activity = dataSet.getActivity();
		Assert.assertNotNull(activity);
		Assert.assertEquals("Sample", activity.getName());
		Assert.assertEquals("synonym 1", activity.getSynonyms().get(0));
		Assert.assertEquals("synonym 2", activity.getSynonyms().get(1));
	}

	@Theory
	public void testClassifications(String file) throws Exception {
		DataSet dataSet = read(file);
		List<Classification> classifications = dataSet.getClassifications();
		Assert.assertEquals(2, classifications.size());
	}

	private DataSet read(String file) throws Exception {
		log.info("parse file " + file);
		InputStream stream = getClass().getResourceAsStream(file);
		return EcoSpold2.read(stream);
	}
}
