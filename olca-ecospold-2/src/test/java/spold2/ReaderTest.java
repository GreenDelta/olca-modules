package spold2;

import java.util.List;

import org.junit.Assert;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

@RunWith(Theories.class)
public class ReaderTest {

	@DataPoints
	public static String[] dataSets = {
			"sample_ecospold2.xml",
			"sample_child_ecospold2.xml"
	};

	@Theory
	public void testDataSetNotNull(String file) {
		DataSet dataSet = read(file);
		Assert.assertNotNull(dataSet);
	}

	@Theory
	public void testActivity(String file) {
		DataSet dataSet = read(file);
		Activity activity = dataSet.description.activity;
		Assert.assertNotNull(activity);
		Assert.assertEquals("Sample", activity.name);
		Assert.assertEquals("synonym 1", activity.synonyms.get(0));
		Assert.assertEquals("synonym 2", activity.synonyms.get(1));
	}

	@Theory
	public void testClassifications(String file) {
		DataSet dataSet = read(file);
		List<Classification> classifications = dataSet.description.classifications;
		Assert.assertEquals(2, classifications.size());
		Classification classification = classifications.get(0);
		Assert.assertEquals("ISIC rev.4 ecoinvent",
				classification.system);
	}

	@Theory
	public void testGeography(String file) {
		DataSet dataSet = read(file);
		Geography geo = dataSet.description.geography;
		Assert.assertEquals("geography comment", geo.comment.texts.get(0).value);
	}

	@Theory
	public void testTechnology(String file) {
		DataSet dataSet = read(file);
		Technology tech = dataSet.description.technology;
		Assert.assertEquals(3, tech.level.intValue());
	}

	@Theory
	public void testElementaryExchanges(String file) {
		DataSet dataSet = read(file);
		Assert.assertEquals(dataSet.flowData.elementaryExchanges.size(), 3);
		double sum = 0;
		for (ElementaryExchange e : dataSet.flowData.elementaryExchanges) {
			sum += e.amount;
		}
		Assert.assertEquals(27, sum, 1e-15);
	}

	@Theory
	public void testIntermediateExchanges(String file) {
		DataSet dataSet = read(file);
		Assert.assertEquals(2, dataSet.flowData.intermediateExchanges.size());
		boolean found = false;
		for (IntermediateExchange e : dataSet.flowData.intermediateExchanges) {
			if (e.outputGroup == null || e.outputGroup != 0)
				continue;
			found = true;
			Assert.assertEquals("1-pentanol", e.name);
		}
		Assert.assertTrue(found);
	}

	@Theory
	public void testTags(String file) {
		var ds = read(file);
		var tags = ds.description.activity.tags;
		Assert.assertEquals(2, tags.size());
		Assert.assertTrue(tags.contains("tag1"));
		Assert.assertTrue(tags.contains("tag2"));
	}

	private DataSet read(String file) {
		try (var stream = getClass().getResourceAsStream(file)) {
			return EcoSpold2.read(stream).activity();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
