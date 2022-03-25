package examples;

import java.io.File;

import org.openlca.npy.Array2d;

public class NpyExample {

	public static void main(String[] args) {

		var npy = "C:/Users/ms/openLCA-data-1.4/libraries/eora_199.82.000/A.npy";
		var col3 = Array2d.readColumn(new File(npy), 3)
			.asDoubleArray()
			.data();
		for (int i = 0; i < col3.length; i++) {
			System.out.printf("%05d %.4f%n", i, col3[i]);
		}

	}
}
