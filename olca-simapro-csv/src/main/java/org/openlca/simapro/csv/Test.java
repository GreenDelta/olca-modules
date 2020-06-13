package org.openlca.simapro.csv;

import java.io.File;

public class Test {

	public static void main(String[] args) throws Exception {
		var path = "C:/Users/Win10/Downloads/ag/AGB 3.0 (20200423) final.CSV";
		SimaProCSV.parse(new File(path), new Object() {

		});
	}
}
