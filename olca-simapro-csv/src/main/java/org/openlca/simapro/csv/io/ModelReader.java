package org.openlca.simapro.csv.io;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;

import org.openlca.simapro.csv.CsvConfig;
import org.openlca.simapro.csv.model.Block;
import org.openlca.simapro.csv.model.annotations.BlockModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelReader implements Closeable {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final BlockReader blockReader;
	private final BlockUnmarshaller unmarshaller;

	private final HashMap<String, Class<?>> blockTypes = new HashMap<>();

	public ModelReader(BlockReader blockReader, CsvConfig config,
			Class<?>... classes) {
		this.blockReader = blockReader;
		this.unmarshaller = new BlockUnmarshaller(config);
		registerClasses(classes);
	}

	private void registerClasses(Class<?>[] classes) {
		if (classes == null)
			return;
		log.trace("register model classes");
		for (Class<?> clazz : classes) {
			if (clazz.isAnnotationPresent(BlockModel.class)) {
				log.trace("register block model {}", clazz);
				BlockModel blockModel = clazz.getAnnotation(BlockModel.class);
				blockTypes.put(blockModel.value(), clazz);
			} else {
				log.warn("could not register class {}, it is not annotated with "
						+ "@BlockModel");
			}
		}
	}

	public Object read() throws Exception {
		Block block = null;
		Object model = null;
		while ((block = blockReader.read()) != null) {
			model = tryGetModel(block);
			if (model != null)
				break;
		}
		return model;
	}

	private Object tryGetModel(Block block) throws Exception {
		if (block == null || block.header == null)
			return null;
		Class<?> clazz = blockTypes.get(block.header);
		if (clazz == null)
			return null;
		return unmarshaller.unmarshall(block, clazz);
	}

	@Override
	public void close() throws IOException {
		if (blockReader != null)
			blockReader.close();
	}
}
