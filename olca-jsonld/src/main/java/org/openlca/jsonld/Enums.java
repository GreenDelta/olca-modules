package org.openlca.jsonld;

import java.util.HashMap;
import java.util.Map;

import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.FlowPropertyType;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.RiskLevel;
import org.openlca.core.model.UncertaintyType;

public class Enums {

	private static Map<Enum<?>, String> valueToLabel = new HashMap<>();
	private static Map<Class<? extends Enum<?>>, Map<String, Enum<?>>> labelToValue = new HashMap<>();
	private static Map<Class<? extends Enum<?>>, Enum<?>> defaultValues = new HashMap<>();

	static {
		putUnmapped(ModelType.class, ModelType.values());
		putUnmapped(ProcessType.class, ProcessType.values());
		putUnmapped(FlowType.class, FlowType.values());
		putUnmapped(RiskLevel.class, RiskLevel.values());
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
		put(ParameterScope.IMPACT_METHOD, "LCIA_METHOD_SCOPE");
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

	private static <T extends Enum<T>> void putUnmapped(Class<T> clazz,
			Enum<?>[] values) {
		if (values == null || values.length == 0)
			return;
		Map<String, Enum<?>> labelToValue = new HashMap<>();
		for (Enum<?> value : values) {
			valueToLabel.put(value, value.name());
			labelToValue.put(value.name(), value);
		}
		Enums.labelToValue.put(clazz, labelToValue);
	}

	public static <T extends Enum<T>> String getLabel(Enum<T> value) {
		if (valueToLabel.containsKey(value))
			return valueToLabel.get(value);
		if (value != null)
			return value.name();
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> T getValue(String label,
			Class<T> enumClass) {
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
		return (T) defaultValues.get(enumClass);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> T getDefaultValue(Class<T> enumClass) {
		return (T) defaultValues.get(enumClass);
	}

}
