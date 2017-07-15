package graphdb.extractors.utils;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterators;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class CollectionUtils {
	public static <A, B> Set<Pair<A, B>> cartesianProduct(Set<A> setA, Set<B> setB) {
		return new CartesianSet<>(setA, setB);
	}

	public static List<String> lcs(List<String> s1, List<String> s2) {
		List<String> lcs = new ArrayList<>();
		int[][] maxLength = new int[s1.size() + 1][s2.size() + 1];
		int[][] mark = new int[s1.size() + 1][s2.size() + 1];
		// 如果是0，两维都-1
		// 如果是1，s1维-1
		// 如果是2，s2维-1

		for (int i = 0; i <= s1.size(); i++)
			maxLength[i][0] = 0;

		for (int j = 0; j <= s2.size(); j++)
			maxLength[0][j] = 0;

		for (int i = 1; i <= s1.size(); i++) {
			for (int j = 1; j <= s2.size(); j++) {
				if (s1.get(i - 1).equals(s2.get(j - 1))) {
					maxLength[i][j] = maxLength[i - 1][j - 1] + 1;
					mark[i][j] = 0;
				} else if (maxLength[i - 1][j] >= maxLength[i][j - 1]) {
					maxLength[i][j] = maxLength[i - 1][j];
					mark[i][j] = 1;
				} else {
					maxLength[i][j] = maxLength[i][j - 1];
					mark[i][j] = 2;
				}
			}
		}

		int i = s1.size(), j = s2.size();
		while (i > 0 && j > 0) {
			if (mark[i][j] == 0) {
				lcs.add(0, s1.get(i - 1));// 或者lcs.add(0, s2.get(j - 1));
				i--;
				j--;
			} else if (mark[i][j] == 1) {
				i--;
			} else {
				j--;
			}
		}

		return lcs;
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
