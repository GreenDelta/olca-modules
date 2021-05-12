package org.openlca.core.matrix.linking;

import java.util.List;

import org.openlca.core.matrix.CalcExchange;
import org.openlca.core.matrix.index.TechFlow;

/**
 * A LinkingCallback is a function that can be injected into the creation of
 * product systems links to control the linking process.
 */
public interface LinkingCallback {

	/**
	 * Returns true if the linking process should be canceled.
	 */
	boolean cancel();

	/**
	 * This function is called when there are multiple possible providers for
	 * the given product input or waste output. The callback should select one
	 * or more providers from the given candidates. Returning null will lead to
	 * an undefined behavior. If exactly one provider is returned, the exchanges
	 * will be linked with this provider. When there are multiple providers
	 * returned, the linking process will select one option based on its own
	 * rules (e.g. this is what happens when the user selects `continue with
	 * auto-select` in the UI).
	 *
	 * TODO: update doc
	 */
	List<TechFlow> select(CalcExchange e, List<TechFlow> providerCandidates);

}
