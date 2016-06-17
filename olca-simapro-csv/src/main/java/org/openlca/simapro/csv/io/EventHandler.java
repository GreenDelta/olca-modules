package org.openlca.simapro.csv.io;

/**
 * Functional for a parser event handler.
 */
@FunctionalInterface
public interface EventHandler {

	void accept(Event evt, String content);

}
