package org.openlca.core.library.csv;

public record LibEnviEntry(
	int index,
	LibFlow flow,
	LibLocationInfo location,
	boolean isInput
) {

}
