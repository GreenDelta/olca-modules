package org.openlca.sd;

import java.io.File;

import org.openlca.sd.model.SdModel;

public class XmileWriterExample {

	public static void main(String[] args) {
		var input = new File("examples/plastic-subs.stmx");
		var output = new File("examples/roundtrip.xml");
		SdModel.readFrom(input)
			.orElseThrow()
			.writeTo(output)
			.orElseThrow();
	}

}
