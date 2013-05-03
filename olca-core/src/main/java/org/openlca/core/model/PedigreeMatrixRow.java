package org.openlca.core.model;

public enum PedigreeMatrixRow {

	RELIABILITY(0),

	COMPLETENESS(1),

	TIME(2),

	GEOGRAPHY(3),

	TECHNOLOGY(4);

	private final int index;

	private PedigreeMatrixRow(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

}
