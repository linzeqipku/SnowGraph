package searcher.codepattern.utils;

import java.util.function.Predicate;

public class Predicates {
	private Predicates() {
	}

	public static <T> Predicate<T> isNull() {
		return x -> x == null;
	}

	public static <T> Predicate<T> notNull() {
		return x -> x != null;
	}
}
