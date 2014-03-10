package org.openlca.io.maps.content;

import org.openlca.simapro.csv.model.refdata.Quantity;

public class CSVQuantityContent implements IMappingContent {

	private Quantity quantity;

	public CSVQuantityContent() {

	}

	public CSVQuantityContent(Quantity quantity) {
		this.quantity = quantity;
	}

	public Quantity getQuantity() {
		return quantity;
	}

	public void setQuantity(Quantity quantity) {
		this.quantity = quantity;
	}

	@Override
	public String getKey() {
		// TODO Auto-generated method stub
		return null;
	}

}
