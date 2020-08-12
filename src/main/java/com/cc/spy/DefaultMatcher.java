package com.cc.spy;

import net.bytebuddy.matcher.ElementMatcher;

public class DefaultMatcher<T> extends ElementMatcher.Junction.AbstractBase<T> {

    private final ElementMatcher<? super T> matcher;

    public DefaultMatcher(ElementMatcher<? super T> matcher) {
        this.matcher = matcher;
    }

    @Override
	public boolean matches(T target) {
        try {
            return this.matcher.matches(target);
        } catch (Throwable t) {
            return false;
        }
    }
}
