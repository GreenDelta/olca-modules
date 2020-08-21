package org.openlca.jsonld;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.FlowPropertyType;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.UncertaintyType;

public class Enums {

	private static Map<Enum<?>, String> valueToLabel = new HashMap<>();
	private static Map<Class<? extends Enum<?>>, Map<String, Enum<?>>> labelToValue = new HashMap<>();
	private static Map<Class<? extends Enum<?>>, Enum<?>> defaultValues = new HashMap<>();

	static {
		putFlowPropertyTypes();
		putAllocationMethods();
		putParameterScopes();
		putUncertaintyTypes();
	}

	private static void putFlowPropertyTypes() {
		put(FlowPropertyType.ECONOMIC, "ECONOMIC_QUANTITY");
		put(FlowPropertyType.PHYSICAL, "PHYSICAL_QUANTITY");
	}

	private static void putParameterScopes() {
		put(ParameterScope.GLOBAL, "GLOBAL_SCOPE");
		put(ParameterScope.PROCESS, "PROCESS_SCOPE");
		put(ParameterScope.IMPACT, "IMPACT_SCOPE");
		defaultValues.put(ParameterScope.class, ParameterScope.GLOBAL);
	}

	private static void putAllocationMethods() {
		put(AllocationMethod.CAUSAL, "CAUSAL_ALLOCATION");
		put(AllocationMethod.ECONOMIC, "ECONOMIC_ALLOCATION");
		put(AllocationMethod.PHYSICAL, "PHYSICAL_ALLOCATION");
		put(AllocationMethod.NONE, "NO_ALLOCATION");
		put(AllocationMethod.USE_DEFAULT, "USE_DEFAULT_ALLOCATION");
		defaultValues.put(AllocationMethod.class, AllocationMethod.NONE);
	}

	private static void putUncertaintyTypes() {
		put(UncertaintyType.UNIFORM, "UNIFORM_DISTRIBUTION");
		put(UncertaintyType.TRIANGLE, "TRIANGLE_DISTRIBUTION");
		put(UncertaintyType.NORMAL, "NORMAL_DISTRIBUTION");
		put(UncertaintyType.LOG_NORMAL, "LOG_NORMAL_DISTRIBUTION");

	}

	@SuppressWarnings("unchecked")
	private static <T extends Enum<T>> void put(Enum<T> value, String label) {
		valueToLabel.put(value, label);
		Class<T> clazz = (Class<T>) value.getClass();
		if (!labelToValue.containsKey(clazz))
			labelToValue.put(clazz, new HashMap<>());
		labelToValue.get(clazz).remove(value.name());
		labelToValue.get(clazz).put(label, value);
	}

	public static <T extends Enum<T>> String getLabel(Enum<T> value) {
		if (value == null)
			return null;
		if (valueToLabel.containsKey(value))
			return valueToLabel.get(value);
		return value.name();
	}

	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> T getValue(String label, Class<T> enumClass) {
		if (label == null)
			return null;
		if (enumClass == null)
			return null;
		if (labelToValue.containsKey(enumClass)) {
			Map<String, Enum<?>> values = labelToValue.get(enumClass);
			if (values.containsKey(label)) {
				Enum<?> value = values.get(label);
				if (enumClass.isInstance(value))
					return (T) value;
			}
		}
		Object defaultVal = defaultValues.get(enumClass);
		if (defaultVal != null) {
			return (T) defaultVal;
		}
		for (T t : enumClass.getEnumConstants()) {
			if (Objects.equals(t.name(), label)) {
				return t;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> T getDefaultValue(Class<T> enumClass) {
		return (T) defaultValues.get(enumClass);
	}

}
