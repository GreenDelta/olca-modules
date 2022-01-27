package org.openlca.core.library.csv;

public record LibEnviEntry(
	int index,
	LibFlowInfo flow,
	LibLocationInfo location,
	boolean isInput
) {

}
