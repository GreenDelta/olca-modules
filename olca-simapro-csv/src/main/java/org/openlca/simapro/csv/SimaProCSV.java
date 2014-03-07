package org.openlca.simapro.csv;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openlca.simapro.csv.model.annotations.BlockHandler;
import org.openlca.simapro.csv.model.annotations.BlockModel;
import org.openlca.simapro.csv.reader.BlockReader;
import org.openlca.simapro.csv.reader.ModelReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimaProCSV {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final File file;
	private final Object handler;
	private HashMap<Class<?>, List<Method>> methodHandlers = new HashMap<>();

	private SimaProCSV(File file, Object handler) {
		this.file = file;
		this.handler = handler;
	}

	public static void parse(File file, Object handler) throws Exception {
		if (file == null || handler == null)
			return;
		new SimaProCSV(file, handler).parse();
	}

	private void parse() throws Exception {
		log.trace("parse file {} with handler {}", file, handler);
		registerMethods();
		if (methodHandlers.isEmpty()) {
			log.warn("no method handlers registerred, do nothing");
			return;
		}
		try (BlockReader reader = new BlockReader(file);
				ModelReader modelReader = createModelReader(reader)) {
			Object model = null;
			while ((model = modelReader.read()) != null) {
				handleModel(model);
			}
		}
	}

	private ModelReader createModelReader(BlockReader blockReader) {
		Class<?>[] classes = new Class<?>[methodHandlers.size()];
		int i = 0;
		for (Class<?> clazz : methodHandlers.keySet()) {
			classes[i] = clazz;
			i++;
		}
		log.trace("create a model reader with {} classes", i);
		return new ModelReader(blockReader, CsvConfig.getDefault(), classes);
	}

	private void registerMethods() {
		log.trace("register method handlers");
		for (Method method : handler.getClass().getDeclaredMethods()) {
			if (!method.isAnnotationPresent(BlockHandler.class))
				continue;
			Class<?> paramType = getParameterType(method);
			if (paramType == null)
				continue;
			log.trace("register method {} for type {}", method, paramType);
			List<Method> handlers = methodHandlers.get(paramType);
			if (handlers == null) {
				handlers = new ArrayList<>();
				methodHandlers.put(paramType, handlers);
			}
			method.setAccessible(true);
			handlers.add(method);
		}
	}

	private Class<?> getParameterType(Method method) {
		Class<?>[] paramTypes = method.getParameterTypes();
		if (paramTypes.length != 1) {
			logInvalidMethod(method);
			return null;
		}
		Class<?> paramType = paramTypes[0];
		if (!paramType.isAnnotationPresent(BlockModel.class)) {
			logInvalidMethod(method);
			return null;
		}
		return paramType;
	}

	private void logInvalidMethod(Method method) {
		log.error("The method {} is declared as block handler but is not valid. "
				+ "A block handler takes exactly 1 argument which must be a class "
				+ "annotated with @BlockModel. ");
	}

	private void handleModel(Object model) throws Exception {
		List<Method> methods = methodHandlers.get(model.getClass());
		if (methods == null)
			return;
		for (Method method : methods) {
			method.invoke(handler, model);
		}
	}
}
