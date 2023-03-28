package org.openlca.io.olca;

import org.openlca.core.database.IDatabase;

record Config(IDatabase source, IDatabase target, Sequence seq) {


}
