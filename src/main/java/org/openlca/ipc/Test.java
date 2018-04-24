package org.openlca.ipc;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.derby.DerbyDatabase;

public class Test {

	public static void main(String[] args) {
		IDatabase db = DerbyDatabase.createInMemory();
		Server server = new Server(8080, db); // Take 0 to select a random port
		System.out.println("Started server @" + server.getListeningPort());
	}
}
