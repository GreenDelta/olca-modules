package org.openlca.io.maps.content;

import org.openlca.simapro.csv.model.SPQuantity;

public class CSVQuantityContent implements IMappingContent {

	private SPQuantity quantity;

	public CSVQuantityContent() {

	}

	public CSVQuantityContent(SPQuantity quantity) {
		this.quantity = quantity;
	}

	public SPQuantity getQuantity() {
		return quantity;
	}

	public void setQuantity(SPQuantity quantity) {
		this.quantity = quantity;
	}

	@Override
	public String getKey() {
		// TODO Auto-generated method stub
		return null;
	}

}
