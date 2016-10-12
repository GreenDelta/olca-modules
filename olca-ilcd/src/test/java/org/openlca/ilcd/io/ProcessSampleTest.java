package org.openlca.ilcd.io;

import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;

import javax.xml.bind.JAXB;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.PublicationStatus;
import org.openlca.ilcd.processes.DataEntry;
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
