package org.openlca.sd;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.openlca.sd.xmile.Xmile;
import org.openlca.sd.xmile.svg.Svg;

public class XmileViewTest {

	public static void main(String[] args) throws Exception {
		var xmile = Xmile
			.readFrom(new File("examples/environment.stmx"))
			.orElseThrow();
		var svg = Svg.xmlOf(xmile).orElseThrow();
		Files.writeString(Paths.get("target/model.svg"), svg);
	}
}
