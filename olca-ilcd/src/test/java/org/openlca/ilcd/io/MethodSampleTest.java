package org.openlca.ilcd.io;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.function.Consumer;

import javax.xml.bind.JAXB;

import org.junit.Test;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.methods.AreaOfProtection;
import org.openlca.ilcd.methods.DataSetInfo;
import org.openlca.ilcd.methods.Geography;
import org.openlca.ilcd.methods.ImpactModel;
import org.openlca.ilcd.methods.LCIAMethod;
import org.openlca.ilcd.methods.Modelling;
import org.openlca.ilcd.methods.QuantitativeReference;
import org.openlca.ilcd.methods.Time;

public class MethodSampleTest {

	@Test
	public void testDataSetInfo() throws Exception {
		with(m -> {
			DataSetInfo info = m.methodInfo.dataSetInfo;
			assertEquals("00000000-0000-0000-0000-000000000000", info.uuid);
			assertEquals(2, info.name.size());
			assertEquals(2, info.methods.size());
			assertEquals("ILCD", info.classifications.get(0).name);
			assertEquals("Acidification", info.impactCategories.get(1));
			assertEquals(AreaOfProtection.NATURAL_RESOURCES, info.areasOfProtection.get(0));
			assertEquals(2, info.comment.size());
			assertEquals(2, info.externalDocs.size());
		});
	}

	@Test
	public void testQuantitativeReference() throws Exception {
		with(m -> {
			QuantitativeReference qRef = m.methodInfo.quantitativeReference;
			assertEquals(DataSetType.FLOW_PROPERTY, qRef.quantity.type);
		});
	}

	@Test
	public void testTime() throws Exception {
		with(m -> {
			Time time = m.methodInfo.time;
			assertEquals(1234, time.referenceYear.intValue());
			assertEquals("duration1", time.duration.get(1).value);
			assertEquals(2, time.description.size());
		});
	}

	@Test
	public void testGeography() throws Exception {
		with(m -> {
			Geography geo = m.methodInfo.geography;
			assertEquals("RER", geo.interventionLocation.code);
			assertEquals(2, geo.interventionSubLocations.size());
			assertEquals("0;100", geo.impactLocation.latLong);
			assertEquals(2, geo.description.size());
		});
	}

	@Test
	public void testImpactModel() throws Exception {
		with(m -> {
			ImpactModel model = m.methodInfo.impactModel;
			assertEquals("modelName0", model.name);
			assertEquals(2, model.description.size());
			assertEquals(2, model.sources.size());
			assertEquals(2, model.includedMethods.size());
			assertEquals(2, model.consideredMechanisms.size());
			assertEquals(2, model.flowCharts.size());
		});
	}

	@Test
	public void testModelling() throws Exception {
		with(m -> {
			Modelling modelling = m.modelling;
			assertEquals(2, modelling.useAdvice.size());
			assertEquals(2, modelling.dataSources.size());
		});
	}

	private void with(Consumer<LCIAMethod> fn) throws Exception {
		try (InputStream in = getClass()
				.getResourceAsStream("sdk_sample_lciamethod.xml")) {
			LCIAMethod m = JAXB.unmarshal(in, LCIAMethod.class);
			fn.accept(m);
		}
	}

}
