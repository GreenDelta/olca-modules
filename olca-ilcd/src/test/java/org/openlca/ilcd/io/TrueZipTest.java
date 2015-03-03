package org.openlca.ilcd.io;

import java.io.File;
import java.util.UUID;

import javax.xml.bind.JAXB;

import org.junit.Test;
import org.openlca.ilcd.flows.AdministrativeInformation;
import org.openlca.ilcd.flows.DataSetInformation;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.flows.FlowInformation;
import org.openlca.ilcd.flows.Publication;

import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileOutputStream;

public class TrueZipTest {

	@Test
	public void testCreateZip() throws Exception {
		String dirPath = System.getProperty("java.io.tmpdir") + File.separator
				+ "test-zip.zip";
		TFile dir = new TFile(dirPath);
		dir.mkdirs();
		TFile file = new TFile(dir, "flows/Flow2.xml");
		try (TFileOutputStream fos = new TFileOutputStream(file)) {
			Flow flow = makeFlow();
			JAXB.marshal(flow, fos);
		}
		TFile.rm(file);
	}

	private Flow makeFlow() {
		String id = UUID.randomUUID().toString();
		Flow flow = new Flow();
		FlowInformation info = new FlowInformation();
		flow.setFlowInformation(info);
		DataSetInformation dataInfo = new DataSetInformation();
		dataInfo.setUUID(id);
		info.setDataSetInformation(dataInfo);
		AdministrativeInformation adminInfo = new AdministrativeInformation();
		Publication pub = new Publication();
		adminInfo.setPublication(pub);
		pub.setDataSetVersion("01.00.000");
		flow.setAdministrativeInformation(adminInfo);
		return flow;
	}
}
