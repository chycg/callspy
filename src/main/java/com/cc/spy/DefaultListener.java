package com.cc.spy;

import net.bytebuddy.agent.builder.AgentBuilder.Listener;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

public class DefaultListener implements Listener {

	@Override
	public void onComplete(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
	}

	@Override
	public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
	}

	@Override
	public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded, Throwable throwable) {
	}

	@Override
	public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded) {
	}

	@Override
	public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded,
			DynamicType dynamicType) {
	}

}
