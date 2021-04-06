package examples;

import org.openlca.julia.Julia;

public class NativeLibExample {

	public static void main(String[] args) {
		System.out.println(Julia.getDefaultDir());
		Julia.load();
	}

}
