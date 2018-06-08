package org.openlca.julia;

import java.io.File;

public class Main {

	public static void main(String[] args) {
		System.out.println("Load libs");
		Julia.load(new File("./julia/libs"));
	}

}
