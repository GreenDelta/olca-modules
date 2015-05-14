package org.openlca.ilcd.io;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.schlichtherle.truezip.file.TFile;

class ZipEntryIterator<T> implements Iterator<T> {

	private final ZipStore zipStore;
	private Iterator<TFile> entryIterator;
	private Class<T> clazz;

	public ZipEntryIterator(ZipStore zipStore, Class<T> clazz) {
		this.zipStore = zipStore;
		this.clazz = clazz;
		List<TFile> list = this.zipStore.findEntries(Path.forClass(clazz));
		this.entryIterator = list.iterator();
	}

	@Override
	public boolean hasNext() {
		return entryIterator.hasNext();
	}

	@Override
	public T next() {
		T next = null;
		TFile entry = entryIterator.next();
		try {
			next = this.zipStore.unmarshal(clazz, entry);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(this.getClass());
			log.error("Cannot load type " + clazz + " from Zip entry "
					+ entry.getName(), e);
		}
		return next;
	}

	@Override
	public void remove() {
		entryIterator.remove();
	}
}