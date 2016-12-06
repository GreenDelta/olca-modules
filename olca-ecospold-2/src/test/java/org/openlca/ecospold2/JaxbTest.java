package org.openlca.ecospold2;

import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXB;

import org.junit.Assert;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

@RunWith(Theories.class)
public class JaxbTest {

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
		Activity a = dataSet.description.activity;
		Assert.assertEquals("08a78e38-fdbe-4ea8-869f-7735b41ecf85", a.id);
		Assert.assertEquals("Sample", a.name);
		Assert.assertEquals("synonym 1", a.synonyms.get(0));
		Assert.assertEquals("synonym 2", a.synonyms.get(1));
		Assert.assertEquals("included activities start",
				a.includedActivitiesStart);
		Assert.assertEquals("included activities end",
				a.includedActivitiesEnd);
		Assert.assertEquals("tag1", a.tags.get(0));
		Assert.assertEquals("tag2", a.tags.get(1));
		Assert.assertEquals("allocation comment",
				RichText.join(a.allocationComment));
		Assert.assertEquals("sample comment 2",
				a.generalComment.texts.get(1).value);
	}

	private DataSet read(String file) throws Exception {
		InputStream stream = getClass().getResourceAsStream(file);
		EcoSpold2 spold = JAXB.unmarshal(stream, EcoSpold2.class);
		StringWriter xml = new StringWriter();
		JAXB.marshal(spold, xml);
		System.out.println(xml);
		return spold.dataSet != null ? spold.dataSet : spold.childDataSet;
	}
}
