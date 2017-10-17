package org.openlca.core.database;

import org.openlca.core.model.Exchange;

public class ExchangeDao extends BaseDao<Exchange> {

	public ExchangeDao(IDatabase database) {
		super(Exchange.class, database);
	}

}
