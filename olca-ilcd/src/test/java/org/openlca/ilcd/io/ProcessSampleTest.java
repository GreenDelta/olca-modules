package org.openlca.ilcd.io;

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
import org.openlca.ilcd.processes.ComplianceDeclaration;
import org.openlca.ilcd.processes.DataEntry;
import org.openlca.ilcd.processes.DataSetInfo;
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
			Assert.assertEquals("00.00", pub.version);
			Assert.assertEquals(2, pub.precedingVersions.size());
			Assert.assertEquals("http://www.ilcd-network.org/data/processes/sample_process.xml", pub.uri.trim());
			Assert.assertEquals(PublicationStatus.WORKING_DRAFT, pub.status);
			Assert.assertEquals(DataSetType.SOURCE, pub.republication.type);
			Assert.assertEquals(DataSetType.CONTACT, pub.registrationAuthority.type);
			Assert.assertEquals(DataSetType.CONTACT, pub.owner.type);
			Assert.assertEquals(2, pub.accessRestrictions.size());

			DataEntry e = p.adminInfo.dataEntry;
			Assert.assertNotNull(e.timeStamp);
			Assert.assertEquals(2, e.formats.size());
			Assert.assertEquals(DataSetType.SOURCE, e.originalDataSet.type);
			Assert.assertEquals(DataSetType.CONTACT, e.documentor.type);
			Assert.assertEquals(2, e.useApprovals.size());
		});
	}

	@Test
	public void testDataSetInfo() throws Exception {
		with(p -> {
			DataSetInfo info = p.processInfo.dataSetInfo;
			Assert.assertEquals(2, info.complementingProcesses.length);
			Assert.assertEquals("identifierOfSubDataSet0", info.subIdentifier);
			Assert.assertEquals(2, info.classifications.size());
		});
	}

	@Test
	public void testTime() throws Exception {
		with(p -> {
			Time time = p.processInfo.time;
			Assert.assertEquals(1234, time.referenceYear.intValue());
			Assert.assertEquals(1234, time.validUntil.intValue());
			Assert.assertEquals(2, time.description.size());
		});
	}

	@Test
	public void testGeography() throws Exception {
		with(p -> {
			Location loc = p.processInfo.geography.location;
			Assert.assertEquals("EU-28", loc.code);
			Assert.assertEquals(2, loc.description.size());
		});
	}

	@Test
	public void testParameters() throws Exception {
		with(p -> {
			ParameterSection section = p.processInfo.parameters;
			Assert.assertEquals(2, section.description.size());
			Assert.assertEquals(2, section.parameters.size());
			Parameter param = section.parameters.get(0);
			Assert.assertEquals("formula0", param.formula);
			Assert.assertEquals(0.0, param.mean.doubleValue(), 0);
			Assert.assertEquals(0.0, param.min.doubleValue(), 0);
			Assert.assertEquals(0.0, param.max.doubleValue(), 0);
			Assert.assertEquals(12.123, param.dispersion.doubleValue(), 1e-16);
			Assert.assertEquals(UncertaintyDistribution.UNDEFINED, param.distribution);
			Assert.assertEquals(2, param.comment.size());
		});
	}

	@Test
	public void testCompliance() throws Exception {
		with(p -> {
			Assert.assertEquals(2, p.modelling.complianceDeclatations.length);
			ComplianceDeclaration c = p.modelling.complianceDeclatations[0];
			Assert.assertNotNull(c.system);
			Compliance v = Compliance.FULLY_COMPLIANT;
			Assert.assertEquals(v, c.approval);
			Assert.assertEquals(v, c.nomenclature);
			Assert.assertEquals(v, c.method);
			Assert.assertEquals(v, c.review);
			Assert.assertEquals(v, c.documentation);
			Assert.assertEquals(v, c.quality);
		});
	}

	@Test
	public void testMethod() throws Exception {
		with(p -> {
			Method method = p.modelling.method;
			Assert.assertEquals(ProcessType.UNIT_PROCESS, method.processType);
			Assert.assertEquals(ModellingPrinciple.ATTRIBUTIONAL, method.principle);
			Assert.assertEquals(2, method.approaches.size());
			Assert.assertEquals(2, method.approachComment.size());
			Assert.assertEquals(2, method.constants.size());
			Assert.assertEquals(2, method.constantsComment.size());
			Assert.assertEquals(2, method.methodSources.size());
			Assert.assertEquals(2, method.principleComment.size());
		});
	}

	@Test
	public void testReviews() throws Exception {
		with(p -> {
			List<Review> reviews = p.modelling.validation.reviews;
			Assert.assertEquals(2, reviews.size());
			for (Review r : reviews) {
				Assert.assertEquals(2, r.scopes.size());
				Assert.assertEquals(2, r.details.size());
				Assert.assertEquals(2, r.indicators.length);
			}
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
