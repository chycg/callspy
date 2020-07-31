package com.cc.spy2;

import net.bytebuddy.agent.builder.AgentBuilder.Listener;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

public class DefaultListener implements Listener {

	@Override
	public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, DynamicType dynamicType) {
	}

	@Override
	public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module) {
	}

	@Override
	public void onError(String typeName, ClassLoader classLoader, JavaModule module, Throwable throwable) {
	}

	@Override
	public void onComplete(String typeName, ClassLoader classLoader, JavaModule module) {
	}

}
