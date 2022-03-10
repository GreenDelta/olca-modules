package org.openlca.util;

import gnu.trove.TLongCollection;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.procedure.TLongProcedure;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import org.openlca.core.model.AbstractEntity;
import org.openlca.core.model.descriptors.Descriptor;

import java.util.Collection;

public final class TLongSets {

	private TLongSets() {
	}

	public static TLongSet singleton(long v) {
		return new SingletonSet(v);
	}

	public static TLongSet addEntityIds(Iterable<? extends AbstractEntity> entities) {
		var set = new TLongHashSet();
		if (entities == null)
			return set;
		entities.forEach(e -> set.add(e.id));
		return set;
	}

	public static TLongSet allDescriptorIds(Iterable<? extends Descriptor> entities) {
		var set = new TLongHashSet();
		if (entities == null)
			return set;
		entities.forEach(e -> set.add(e.id));
		return set;
	}

	public static String join(String separator, TLongSet set) {
		if (set == null || separator == null || set.isEmpty())
			return "";
		var buff = new StringBuilder();
		var it = set.iterator();
		while (it.hasNext()) {
			var next = it.next();
			if (!buff.isEmpty()) {
				buff.append(separator);
			}
			buff.append(next);
		}
		return buff.toString();
	}

	public static long first(TLongSet set) {
		return set.isEmpty()
			? set.getNoEntryValue()
			: set.iterator().next();
	}

	private record SingletonSet(long value) implements TLongSet {

		@Override
		public long getNoEntryValue() {
			return value == 0 ? -1 : 0;
		}

		@Override
		public int size() {
			return 1;
		}

		@Override
		public boolean isEmpty() {
			return false;
		}

		@Override
		public boolean contains(long entry) {
			return entry == value;
		}

		@Override
		public TLongIterator iterator() {
			return new TLongIterator() {

				private boolean moved = false;

				@Override
				public long next() {
					moved = true;
					return value;
				}

				@Override
				public boolean hasNext() {
					return !moved;
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}

		@Override
		public long[] toArray() {
			return new long[]{value};
		}

		@Override
		public long[] toArray(long[] dest) {
			if (dest == null || dest.length == 0)
				return toArray();
			dest[0] = value;
			return dest;
		}

		@Override
		public boolean add(long entry) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean remove(long entry) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean containsAll(Collection<?> collection) {
			if (collection == null || collection.isEmpty())
				return false;
			for (var next : collection) {
				if (!(next instanceof Number num) || num.longValue() != value)
					return false;
			}
			return true;
		}

		@Override
		public boolean containsAll(TLongCollection collection) {
			if (collection == null || collection.isEmpty())
				return false;
			var it = collection.iterator();
			while (it.hasNext()) {
				if (it.next() != value)
					return false;
			}
			return true;
		}

		@Override
		public boolean containsAll(long[] array) {
			if (array == null || array.length == 0)
				return false;
			for (var v : array) {
				if (v != value)
					return false;
			}
			return true;
		}

		@Override
		public boolean addAll(Collection<? extends Long> collection) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll(TLongCollection collection) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll(long[] array) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(Collection<?> collection) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(TLongCollection collection) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(long[] array) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeAll(Collection<?> collection) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeAll(TLongCollection collection) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeAll(long[] array) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean forEach(TLongProcedure procedure) {
			return procedure.execute(value);
		}
	}

}
