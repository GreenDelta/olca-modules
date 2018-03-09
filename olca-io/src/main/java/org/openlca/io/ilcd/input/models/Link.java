package org.openlca.io.ilcd.input.models;

import org.openlca.core.model.Exchange;

/**
 * Represents a link in an eILCD model: a provider of an output is linked to an
 * input of a recipient.
 */
class Link {

	/**
	 * This is a graph internal ID of the link. It is set when the link is added
	 * to the graph.
	 */
	int id;
	Exchange output;
	Exchange input;
	Node provider;
	Node recipient;

	@Override
	public String toString() {
		int from = provider != null ? provider.modelID : -1;
		int to = recipient != null ? recipient.modelID : -1;
		return "Link [ id=" + id + " : node[" + from
				+ "] -> node[" + to + "] ]";
	}
}
