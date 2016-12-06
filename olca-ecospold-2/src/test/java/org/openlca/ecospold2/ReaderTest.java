package org.openlca.ecospold2;

import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

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
		Activity activity = dataSet.activity;
		Assert.assertNotNull(activity);
		Assert.assertEquals("Sample", activity.name);
		Assert.assertEquals("synonym 1", activity.synonyms.get(0));
		Assert.assertEquals("synonym 2", activity.synonyms.get(1));
	}

	@Theory
	public void testClassifications(String file) throws Exception {
		DataSet dataSet = read(file);
		List<Classification> classifications = dataSet.classifications;
		Assert.assertEquals(2, classifications.size());
		Classification classification = classifications.get(0);
		Assert.assertEquals("ISIC rev.4 ecoinvent",
				classification.classificationSystem);
	}

	@Theory
	public void testGeography(String file) throws Exception {
		DataSet dataSet = read(file);
		Geography geo = dataSet.geography;
		Assert.assertEquals("geography comment", geo.comment);
	}

	@Theory
	public void testTechnology(String file) throws Exception {
		DataSet dataSet = read(file);
		Technology tech = dataSet.technology;
		Assert.assertEquals(3, tech.technologyLevel.intValue());
	}

	public void testElementaryExchanges(String file) throws Exception {
		DataSet dataSet = read(file);
		Assert.assertEquals(dataSet.elementaryExchanges.size(), 3);
		double sum = 0;
		for (ElementaryExchange e : dataSet.elementaryExchanges) {
			sum += e.amount;
		}
		Assert.assertEquals(27, sum, 1e-15);
	}

	@Theory
	public void testIntermediateExchanges(String file) throws Exception {
		DataSet dataSet = read(file);
		Assert.assertEquals(2, dataSet.intermediateExchanges.size());
		boolean found = false;
		for (IntermediateExchange e : dataSet.intermediateExchanges) {
			if (e.outputGroup == null || e.outputGroup != 0)
				continue;
			found = true;
			Assert.assertEquals("1-pentanol", e.name);
		}
		Assert.assertTrue(found);
	}

	private DataSet read(String file) throws Exception {
		log.info("parse file " + file);
		InputStream stream = getClass().getResourceAsStream(file);
		return EcoSpold2.readDataSet(stream);
	}
}
