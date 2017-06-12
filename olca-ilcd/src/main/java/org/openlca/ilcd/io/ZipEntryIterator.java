package org.openlca.ilcd.io;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ZipEntryIterator<T> implements Iterator<T> {

	private final ZipStore zipStore;
	private Iterator<Path> it;
	private Class<T> clazz;

	public ZipEntryIterator(ZipStore zipStore, Class<T> clazz) {
		this.zipStore = zipStore;
		this.clazz = clazz;
		List<Path> list = new ArrayList<>();
		for (Path p : zipStore.getEntries(Dir.get(clazz))) {
			if (Util.isXml(p))
				list.add(p);
		}
		this.it = list.iterator();
	}

	@Override
	public boolean hasNext() {
		return it != null && it.hasNext();
	}

	@Override
	public T next() {
		if (it == null)
			return null;
		Path entry = it.next();
		try {
			return zipStore.unmarshal(clazz, entry);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(this.getClass());
			log.error("Cannot load type " + clazz + " from entry " + entry, e);
			return null;
		}
	}

	@Override
	public void remove() {
		it.remove();
	}
}