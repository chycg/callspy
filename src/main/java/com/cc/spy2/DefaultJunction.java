package com.cc.spy2;

import net.bytebuddy.matcher.ElementMatcher;

public class DefaultJunction<V> implements ElementMatcher.Junction<V> {
	@Override
	public <U extends V> Junction<U> and(ElementMatcher<? super U> other) {
		return new Conjunction<U>(this, other);
	}

	@Override
	public <U extends V> Junction<U> or(ElementMatcher<? super U> other) {
		return new Disjunction<U>(this, other);
	}

	@Override
	public boolean matches(V target) {
		return true;
	}
}
