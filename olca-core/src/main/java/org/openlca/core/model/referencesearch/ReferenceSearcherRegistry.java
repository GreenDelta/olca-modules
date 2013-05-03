/*******************************************************************************
 * Copyright (c) 2007 - 2010 GreenDeltaTC. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Mozilla
 * Public License v1.1 which accompanies this distribution, and is available at
 * http://www.openlca.org/uploads/media/MPL-1.1.html
 * 
 * Contributors: GreenDeltaTC - initial API and implementation
 * www.greendeltatc.com tel.: +49 30 4849 6030 mail: gdtc@greendeltatc.com
 ******************************************************************************/
package org.openlca.core.model.referencesearch;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registry for reference searcher for a given class
 * 
 * @author Sebastian Greve
 * 
 */
public class ReferenceSearcherRegistry {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * The singleton instance
	 */
	private static ReferenceSearcherRegistry registry;

	/**
	 * Key = class name, value = reference searcher
	 */
	private final Map<String, IReferenceSearcher<?>> referenceSearchers = new HashMap<>();

	/**
	 * Getter of the singleton instance
	 * 
	 * @return the singleton instance
	 */
	public static ReferenceSearcherRegistry getRegistry() {
		if (registry == null) {
			registry = new ReferenceSearcherRegistry();
			registry.initializeProblemHandlers();
		}
		return registry;
	}

	/**
	 * Initializes the reference searcher registered in the extension point
	 * org.openlca.core.model.component
	 */
	private void initializeProblemHandlers() {
		// final IExtensionRegistry extensionRegistry = Platform
		// .getExtensionRegistry();
		// final IConfigurationElement[] elements = extensionRegistry
		// .getConfigurationElementsFor("org.openlca.core.model.component");
		// for (final IConfigurationElement confElement : elements) {
		// try {
		// if (confElement.getChildren("referenceSearcher").length > 0) {
		// final IConfigurationElement handlerConfElement = confElement
		// .getChildren("referenceSearcher")[0];
		// final Object o = handlerConfElement
		// .createExecutableExtension("class");
		// if (o != null && o instanceof IReferenceSearcher<?>) {
		// final IReferenceSearcher<?> handler = (IReferenceSearcher<?>) o;
		// final String componentClass = confElement
		// .getAttribute("class");
		// referenceSearchers.put(componentClass, handler);
		// }
		// }
		// } catch (final CoreException e) {
		// log.error("Initializing problem handlers failed", e);
		// }
		// }
	}

	/**
	 * Getter of reference searcher
	 * 
	 * @param componentClass
	 *            the class of the component which needs a reference searcher
	 * @return the reference searcher for the given component class
	 */
	public IReferenceSearcher<?> getSearcher(final String componentClass) {
		return referenceSearchers.get(componentClass);
	}

	/**
	 * Registers a reference searcher with the given class
	 * 
	 * @param componentClass
	 *            the class of the component
	 * @param searcher
	 *            the reference searcher for the component
	 */
	public void registerSearcher(final String componentClass,
			final IReferenceSearcher<?> searcher) {
		referenceSearchers.put(componentClass, searcher);
	}

	/**
	 * Unregisters a reference searcher
	 * 
	 * @param searcher
	 *            the reference searcher to be unregistered
	 */
	public void unregisterSearcher(final IReferenceSearcher<?> searcher) {
		referenceSearchers.remove(searcher);
	}

}
