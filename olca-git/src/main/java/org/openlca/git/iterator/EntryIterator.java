package org.openlca.git.iterator;

import java.util.List;

import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;

public abstract class EntryIterator extends AbstractTreeIterator {

	private List<TreeEntry> entries;
	private int index;

	protected EntryIterator(List<TreeEntry> entries) {
		init(entries);
	}

	protected EntryIterator(AbstractTreeIterator parent, List<TreeEntry> entries) {
		super(parent);
		init(entries);
	}

	protected EntryIterator(String path, List<TreeEntry> entries) {
		super(path);
		init(entries);
	}

	private void init(List<TreeEntry> entries) {
		this.entries = entries.stream().sorted().toList();
		if (!eof()) {
			update();
		}
	}

	@Override
	public boolean hasId() {
		return false;
	}

	@Override
	public byte[] idBuffer() {
		var buf = new byte[40];
		ObjectId.zeroId().copyRawTo(buf, 0);
		return buf;
	}

	@Override
	public int idOffset() {
		return 0;
	}

	@Override
	public EntryIterator createSubtreeIterator(ObjectReader reader) {
		return null;
	}

	@Override
	public boolean first() {
		return index == 0;
	}

	@Override
	public boolean eof() {
		return index >= entries.size();
	}

	@Override
	public void next(int delta) throws CorruptObjectException {
		index += delta;
		if (eof())
			return;
		update();
	}

	@Override
	public void back(int delta) throws CorruptObjectException {
		index -= delta;
		if (index < 0)
			return;
		update();
	}

	@Override
	public void reset() {
		if (first())
			return;
		index = 0;
		if (eof())
			return;
		update();
	}

	private void update() {
		var entry = entries.get(index);
		var nameLen = entry.name.getBytes().length;
		ensurePathCapacity(pathOffset + nameLen, pathOffset);
		System.arraycopy(entry.name.getBytes(), 0, path, pathOffset, nameLen);
		pathLen = pathOffset + nameLen;
		mode = entry.fileMode.getBits();
	}

	@SuppressWarnings("unchecked")
	public <T> T getEntryData() {
		if (eof())
			return null;
		return (T) entries.get(index).data;
	}

	public String getEntryFilePath() {
		if (eof())
			return null;
		return entries.get(index).filePath;
	}

}
