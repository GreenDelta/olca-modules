/*******************************************************************************
 * Copyright (c) 2007 - 2010 GreenDeltaTC. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Mozilla
 * Public License v1.1 which accompanies this distribution, and is available at
 * http://www.openlca.org/uploads/media/MPL-1.1.html
 * 
 * Contributors: GreenDeltaTC - initial API and implementation
 * www.greendeltatc.com tel.: +49 30 4849 6030 mail: gdtc@greendeltatc.com
 ******************************************************************************/
package org.openlca.core.model.modelprovider;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for model components
 * 
 * @author Sebastian Greve
 * 
 */
public class ModelComponentRegistry {

	/**
	 * Singleton instance
	 */
	private static ModelComponentRegistry registry;

	/**
	 * key = name of a model component; value = class of the model component
	 */
	private final Map<String, Class<? extends IModelComponent>> modelComponentClasses = new HashMap<>();

	/**
	 * key = name of a model component; value = hierarchy level in category
	 * viewer
	 */
	private final Map<String, Integer> modelComponentLevels = new HashMap<>();

	/**
	 * Getter of the singleton instance
	 * 
	 * @return the singleton instance
	 */
	public static ModelComponentRegistry getRegistry() {
		if (registry == null) {
			registry = new ModelComponentRegistry();
		}
		return registry;
	}

	/**
	 * Getter of the class of a given model component name
	 * 
	 * @param modelComponentName
	 *            the name of the model component
	 * @return the class for the given model component name
	 */
	public Class<? extends IModelComponent> getClassForName(
			final String modelComponentName) {
		return modelComponentClasses.get(modelComponentName);
	}

	/**
	 * Getter of the hierarchy level in the category viewer of a given model
	 * component name
	 * 
	 * @param modelComponentName
	 *            the name of the model component
	 * @return the hierarchy level in the category viewer for the given model
	 *         component name
	 */
	public Integer getLevelForName(final String modelComponentName) {
		return modelComponentLevels.get(modelComponentName);
	}

	/**
	 * Getter of all registered model component names
	 * 
	 * @return The names of all registered model components
	 */
	public String[] getModelComponents() {
		return modelComponentClasses.keySet().toArray(
				new String[modelComponentClasses.size()]);
	}

	/**
	 * Registers a model component to the registry
	 * 
	 * @param modelComponentClass
	 *            the class of the model component
	 * @param modelComponentName
	 *            the name of the model component
	 * @param level
	 *            the hierarchy level of the model component in the category
	 *            viewer
	 */
	public void registerModelComponent(
			final Class<? extends IModelComponent> modelComponentClass,
			final String modelComponentName, final int level) {
		if (modelComponentClass != null && modelComponentName != null
				&& modelComponentClasses.get(modelComponentName) == null) {
			modelComponentClasses.put(modelComponentName, modelComponentClass);
			modelComponentLevels.put(modelComponentName, level);
		}
	}

	/**
	 * Unregisters a model component
	 * 
	 * @param modelComponentName
	 *            the model component to be unregistered
	 */
	public void unregisterModelComponent(final String modelComponentName) {
		if (modelComponentName != null
				&& modelComponentClasses.get(modelComponentName) != null) {
			modelComponentClasses.remove(modelComponentName);
		}
	}

}
