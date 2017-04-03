package org.openlca.ilcd.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;

import javax.xml.bind.JAXB;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.ilcd.commons.Compliance;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.ModellingPrinciple;
import org.openlca.ilcd.commons.ProcessType;
import org.openlca.ilcd.commons.PublicationStatus;
import org.openlca.ilcd.commons.Time;
import org.openlca.ilcd.commons.UncertaintyDistribution;
import org.openlca.ilcd.processes.AllocationFactor;
import org.openlca.ilcd.processes.ComplianceDeclaration;
import org.openlca.ilcd.processes.DataEntry;
import org.openlca.ilcd.processes.DataSetInfo;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.LCIAResult;
import org.openlca.ilcd.processes.Location;
import org.openlca.ilcd.processes.Method;
import org.openlca.ilcd.processes.Parameter;
import org.openlca.ilcd.processes.ParameterSection;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.Publication;
import org.openlca.ilcd.processes.Review;

public class ProcessSampleTest {

	@Test
	public void testAdminInfo() throws Exception {
		with(p -> {
			Publication pub = p.adminInfo.publication;
			Assert.assertNotNull(pub.lastRevision);
			assertEquals("00.00", pub.version);
			assertEquals(2, pub.precedingVersions.size());
			assertEquals(
					"http://www.ilcd-network.org/data/processes/sample_process.xml",
					pub.uri.trim());
			assertEquals(PublicationStatus.WORKING_DRAFT, pub.status);
			assertEquals(DataSetType.SOURCE, pub.republication.type);
			assertEquals(DataSetType.CONTACT, pub.registrationAuthority.type);
			assertEquals(DataSetType.CONTACT, pub.owner.type);
			assertEquals(2, pub.accessRestrictions.size());

			DataEntry e = p.adminInfo.dataEntry;
			Assert.assertNotNull(e.timeStamp);
			assertEquals(2, e.formats.size());
			assertEquals(DataSetType.SOURCE, e.originalDataSet.type);
			assertEquals(DataSetType.CONTACT, e.documentor.type);
			assertEquals(2, e.useApprovals.size());
		});
	}

	@Test
	public void testDataSetInfo() throws Exception {
		with(p -> {
			DataSetInfo info = p.processInfo.dataSetInfo;
			assertEquals(2, info.complementingProcesses.length);
			assertEquals("identifierOfSubDataSet0", info.subIdentifier);
			assertEquals(2, info.classifications.size());
		});
	}

	@Test
	public void testTime() throws Exception {
		with(p -> {
			Time time = p.processInfo.time;
			assertEquals(1234, time.referenceYear.intValue());
			assertEquals(1234, time.validUntil.intValue());
			assertEquals(2, time.description.size());
		});
	}

	@Test
	public void testGeography() throws Exception {
		with(p -> {
			Location loc = p.processInfo.geography.location;
			assertEquals("EU-28", loc.code);
			assertEquals(2, loc.description.size());
		});
	}

	@Test
	public void testParameters() throws Exception {
		with(p -> {
			ParameterSection section = p.processInfo.parameters;
			assertEquals(2, section.description.size());
			assertEquals(2, section.parameters.size());
			Parameter param = section.parameters.get(0);
			assertEquals("formula0", param.formula);
			assertEquals(0.0, param.mean.doubleValue(), 0);
			assertEquals(0.0, param.min.doubleValue(), 0);
			assertEquals(0.0, param.max.doubleValue(), 0);
			assertEquals(12.123, param.dispersion.doubleValue(), 1e-16);
			assertEquals(UncertaintyDistribution.UNDEFINED, param.distribution);
			assertEquals(2, param.comment.size());
		});
	}

	@Test
	public void testCompliance() throws Exception {
		with(p -> {
			assertEquals(2, p.modelling.complianceDeclatations.length);
			ComplianceDeclaration c = p.modelling.complianceDeclatations[0];
			Assert.assertNotNull(c.system);
			Compliance v = Compliance.FULLY_COMPLIANT;
			assertEquals(v, c.approval);
			assertEquals(v, c.nomenclature);
			assertEquals(v, c.method);
			assertEquals(v, c.review);
			assertEquals(v, c.documentation);
			assertEquals(v, c.quality);
		});
	}

	@Test
	public void testMethod() throws Exception {
		with(p -> {
			Method method = p.modelling.method;
			assertEquals(ProcessType.UNIT_PROCESS, method.processType);
			assertEquals(ModellingPrinciple.ATTRIBUTIONAL, method.principle);
			assertEquals(2, method.approaches.size());
			assertEquals(2, method.approachComment.size());
			assertEquals(2, method.constants.size());
			assertEquals(2, method.constantsComment.size());
			assertEquals(2, method.methodSources.size());
			assertEquals(2, method.principleComment.size());
		});
	}

	@Test
	public void testReviews() throws Exception {
		with(p -> {
			List<Review> reviews = p.modelling.validation.reviews;
			assertEquals(2, reviews.size());
			for (Review r : reviews) {
				assertEquals(2, r.scopes.size());
				assertEquals(2, r.details.size());
				assertEquals(2, r.indicators.length);
			}
		});
	}

	@Test
	public void testAllocation() throws Exception {
		with(p -> {
			Exchange e = p.exchanges.get(0);
			assertEquals(2, e.allocations.length);
			AllocationFactor f = e.allocations[1];
			assertEquals(57.98, f.fraction, 1e-10);
			assertEquals(1, f.productExchangeId);
		});
	}

	@Test
	public void testLCIAResults() throws Exception {
		with(p -> {
			assertEquals(2, p.lciaResults.length);
			LCIAResult r1 = p.lciaResults[0];
			assertTrue(r1.method.isValid());
			assertEquals(DataSetType.LCIA_METHOD, r1.method.type);
		});
	}

	private void with(Consumer<Process> fn) throws Exception {
		try (InputStream in = getClass()
				.getResourceAsStream("sdk_sample_process.xml")) {
			Process p = JAXB.unmarshal(in, Process.class);
			fn.accept(p);
		}
	}

}
