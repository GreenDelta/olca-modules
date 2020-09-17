package examples;

import java.io.File;

import org.openlca.core.matrix.io.npy.Npy;

public class NpyExample {

	public static void main(String[] args) {

		var npy = "C:/Users/Win10/openLCA-data-1.4/libraries/ecoinvent_2_2_unit_02.02.000/INV.npy";
		Npy.loadColumn(new File(npy), 3);

	}
}
