package exps.codepattern.utils;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterators;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class SetUtils {
	public static <A, B> Set<Pair<A, B>> cartesianProduct(Set<A> setA, Set<B> setB) {
		return new CartesianSet<>(setA, setB);
	}

	private static class CartesianSet<A, B> implements Set<Pair<A, B>> {
		private Set<A> setA;
		private Set<B> setB;

		CartesianSet(Set<A> setA, Set<B> setB) {
			this.setA = setA;
			this.setB = setB;
		}

		@Override
		public String toString() {
			return "[" + Joiner.on(", ").join(iterator()) + "]";
		}

		@Override
		public int size() {
			return setA.size() * setB.size();
		}

		@Override
		public boolean isEmpty() {
			return size() == 0;
		}

		@Override
		public boolean contains(Object o) {
			return o != null && Iterators.contains(this.iterator(), o);
		}

		@Override
		public Iterator<Pair<A, B>> iterator() {
			return new CartesianSetIterator();
		}

		@Override
		public Object[] toArray() {
			int i = 0;
			Object element;
			Object[] array = new Object[size()];
			for (Iterator ite = iterator(); ite.hasNext(); array[i++] = element) element = ite.next();
			return array;
		}

		@Override
		public <T> T[] toArray(T[] a) {
			return (T[]) toArray();
		}

		@Deprecated
		public boolean add(Pair<A, B> abPair) {
			throw new UnsupportedOperationException();
		}

		@Deprecated
		public boolean remove(Object o) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			Iterator ite = c.iterator();

			while (ite.hasNext()) {
				if (!contains(ite.next())) return false;
			}

			return true;
		}

		@Deprecated
		public boolean addAll(Collection<? extends Pair<A, B>> c) {
			throw new UnsupportedOperationException();
		}

		@Deprecated
		public boolean retainAll(Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Deprecated
		public boolean removeAll(Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Deprecated
		public void clear() {
			throw new UnsupportedOperationException();
		}

		private class CartesianSetIterator implements Iterator<Pair<A, B>> {
			private Iterator<A> iteA = setA.iterator();
			private Iterator<B> iteB = setB.iterator();
			private boolean init = false;
			private A currentA;
			private B currentB;

			@Override
			public boolean hasNext() {
				if (init) return iteA.hasNext() || iteB.hasNext();
				return iteA.hasNext() && iteB.hasNext();
			}

			@Override
			public Pair<A, B> next() {
				if (!init) {
					currentA = iteA.next();
					currentB = iteB.next();
					init = true;
					return Pair.of(currentA, currentB);
				}
				if (iteB.hasNext()) {
					currentB = iteB.next();
					return Pair.of(currentA, currentB);
				}
				iteB = setB.iterator();
				currentA = iteA.next();
				currentB = iteB.next();
				return Pair.of(currentA, currentB);
			}
		}
	}
}
