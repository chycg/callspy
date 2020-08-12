package com.cc.spy;

import static net.bytebuddy.matcher.ElementMatchers.isEquals;
import static net.bytebuddy.matcher.ElementMatchers.isHashCode;
import static net.bytebuddy.matcher.ElementMatchers.isInterface;
import static net.bytebuddy.matcher.ElementMatchers.isSetter;
import static net.bytebuddy.matcher.ElementMatchers.isToString;
import static net.bytebuddy.matcher.ElementMatchers.nameContainsIgnoreCase;
import static net.bytebuddy.matcher.ElementMatchers.not;

import java.lang.instrument.Instrumentation;

import com.cc.Config;
import com.cc.Stack;
import com.cc.Utils;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatcher.Junction;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

public class Agent {

	public static void premain(String args, Instrumentation instrumentation) {
		System.out.println("Agent.premain, args = " + args);

		Config config = new Config(args);
		TraceInterceptor.config = config;
		TraceAdvisor.config = config;

		Stack.init(config);
		Utils.init(config);

		transform(config, instrumentation);
	}

	/**
	 * @param instrumentation
	 */
	private static void transform(Config config, Instrumentation instrumentation) {
		Transformer transformer = new Transformer() {
			@Override
			public Builder<?> transform(Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module) {
				Implementation in = MethodDelegation.to(TraceInterceptor.class);
				// Implementation in = Advice.to(TraceInterceptor.class);
				return builder.method(ElementMatchers.any()).intercept(in);
			}
		};

		Junction<?> judge = new DefaultJunction<NamedElement>();
		judge = judge.and(not(isInterface())).and(not(isSetter())).and(not(isToString())).and(not(isHashCode())).and(not(isEquals()));

		judge = judge.and(ElementMatchers.nameStartsWith(config.getIncludes()));

		for (String e : config.getExcludes()) {
			judge = judge.and(not(nameContainsIgnoreCase(e)));
		}

		new AgentBuilder.Default().type(new DefaultMatcher(judge)).transform(transformer).installOn(instrumentation);
	}

}
