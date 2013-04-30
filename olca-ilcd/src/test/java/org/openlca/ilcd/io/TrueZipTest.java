package org.openlca.ilcd.io;

import java.io.File;

import javax.xml.bind.JAXB;

import org.junit.Test;
import org.openlca.ilcd.commons.FlowType;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.util.FlowBuilder;

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
			Flow flow = FlowBuilder.makeFlow()
					.withFlowType(FlowType.ELEMENTARY_FLOW).getFlow();
			JAXB.marshal(flow, fos);
		}
		TFile.rm(file);
	}
}
