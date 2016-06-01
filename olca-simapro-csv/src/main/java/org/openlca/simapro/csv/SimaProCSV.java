package org.openlca.simapro.csv;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.openlca.simapro.csv.io.BlockReader;
import org.openlca.simapro.csv.io.FileHeaderReader;
import org.openlca.simapro.csv.io.ModelReader;
import org.openlca.simapro.csv.model.FileHeader;
import org.openlca.simapro.csv.model.annotations.BlockHandler;
import org.openlca.simapro.csv.model.annotations.BlockModel;
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
		CsvConfig config = readConfig(file);
		try (BlockReader reader = new BlockReader(file);
				ModelReader modelReader = createModelReader(reader, config)) {
			Object model = null;
			while ((model = modelReader.read()) != null) {
				handleModel(model);
			}
		}
	}

	private ModelReader createModelReader(BlockReader blockReader,
			CsvConfig config) {
		Class<?>[] classes = new Class<?>[methodHandlers.size()];
		int i = 0;
		for (Class<?> clazz : methodHandlers.keySet()) {
			classes[i] = clazz;
			i++;
		}
		log.trace("create a model reader with {} classes", i);
		return new ModelReader(blockReader, config, classes);
	}

	private void registerMethods() {
		log.trace("register method handlers");
		for (Method method : handler.getClass().getDeclaredMethods()) {
			if (!method.isAnnotationPresent(BlockHandler.class))
				continue;
			List<Class<?>> paramTypes = getParameterTypes(method);
			if (paramTypes.isEmpty())
				continue;
			method.setAccessible(true);
			for (Class<?> paramType : paramTypes) {
				log.trace("register method {} for type {}", method, paramType);
				List<Method> handlers = methodHandlers.get(paramType);
				if (handlers == null) {
					handlers = new ArrayList<>();
					methodHandlers.put(paramType, handlers);
				}
				handlers.add(method);
			}
		}
	}

	private List<Class<?>> getParameterTypes(Method method) {
		Class<?>[] paramTypes = method.getParameterTypes();
		if (paramTypes.length != 1) {
			logInvalidMethod(method);
			return Collections.emptyList();
		}
		Class<?> paramType = paramTypes[0];
		BlockHandler blockHandler = method.getAnnotation(BlockHandler.class);
		List<Class<?>> result = new ArrayList<>();
		Class<?>[] subTypes = blockHandler.subTypes();
		if (subTypes == null || subTypes.length == 0) {
			if (!paramType.isAnnotationPresent(BlockModel.class))
				logInvalidMethod(method);
			else
				result.add(paramType);
		} else {
			for (Class<?> subType : subTypes) {
				if (!paramType.isAssignableFrom(subType))
					logInvalidMethod(method);
				else if (!subType.isAnnotationPresent(BlockModel.class))
					logInvalidMethod(method);
				else
					result.add(subType);
			}
		}
		return result;
	}

	private void logInvalidMethod(Method method) {
		log.warn("The method {} is declared as block handler but is not valid."
				+ "A block handler takes exactly 1 argument which must "
				+ "be a class annotated with @BlockModel or a super type "
				+ " of the classes given in the @BlockModel annotation "
				+ " which itself must be annotated with @BlockModel", method);
	}

	private void handleModel(Object model) throws Exception {
		List<Method> methods = methodHandlers.get(model.getClass());
		if (methods == null)
			return;
		for (Method method : methods) {
			method.invoke(handler, model);
		}
	}

	private CsvConfig readConfig(File file) {
		CsvConfig config = CsvConfig.getDefault();
		try {
			FileHeaderReader reader = new FileHeaderReader(file);
			FileHeader header = reader.read();
			if (header.getShortDateFormat() != null)
				config.setDateFormat(header.getShortDateFormat());
			if (Objects.equals("Semicolon", header.getCsvSeparator()))
				config.setSeparator(';');
			else if (Objects.equals("Comma", header.getCsvSeparator()))
				config.setSeparator(',');
		} catch (Exception e) {
			log.error("failed to read header CSV entries from " + file, e);
		}
		return config;
	}

}
