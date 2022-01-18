package examples;

import java.io.File;

import org.openlca.core.DataDir;
import org.openlca.core.database.Derby;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Process;
import org.openlca.julia.Julia;

public class LibTest {

	public static void main(String[] args) throws Exception {
		DataDir.setRoot(new File("C:/Users/ms/Desktop/libs"));
		Julia.load();
	}

}
