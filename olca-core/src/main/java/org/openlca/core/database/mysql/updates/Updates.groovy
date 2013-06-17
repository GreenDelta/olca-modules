package org.openlca.core.database.mysql.updates


import org.openlca.core.database.ConnectionData;

class Updates {

	static void checkAndRun(ConnectionData data) {
		def updater = new Updater(data)
		updater.run()
	}
}
