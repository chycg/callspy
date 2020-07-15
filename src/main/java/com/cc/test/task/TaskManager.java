package com.cc.test.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;

/**
 * @author: chenyong
 * @description:
 * @create: 2019-12-20 15:33
 **/
public final class TaskManager {

	/**
	 * 线程池
	 */
	private static ForkJoinPool threadPool = new ForkJoinPool();

	/**
	 * @param list
	 *            入参集合
	 * @param function
	 *            单个入参，单个返回
	 * @return
	 */
	public static <T, R> List<R> submit(List<T> list, Function<T, R> function) {
		if (CollectionUtils.isEmpty(list) || function == null) {
			return new ArrayList<>();
		}

		SingleTask<T, R> task = new SingleTask<>(list, function);
		threadPool.invoke(task);
		return task.join();
	}

	/**
	 * @param list
	 *            入参集合
	 * @param function
	 *            单个入参，多个返回
	 * @param <T>
	 * @param <R>
	 * @return
	 */
	public static <T, R> List<R> submitMulti(List<T> list, Function<T, List<R>> function) {
		if (CollectionUtils.isEmpty(list) || function == null) {
			return new ArrayList<>();
		}

		MultiTask<T, R> task = new MultiTask<>(list, function);
		threadPool.invoke(task);
		return task.join();
	}

	/**
	 * @param list
	 *            入参集合
	 * @param function
	 *            多个入参，多个返回
	 * @param groupSize
	 *            分组最大量
	 * @param <T>
	 * @param <R>
	 * @return
	 */
	public static <T, R> List<R> submitList(List<T> list, Function<List<T>, List<R>> function, int groupSize) {
		if (CollectionUtils.isEmpty(list) || function == null) {
			return new ArrayList<>();
		}

		ListTask<T, R> task = new ListTask<>(list, function, groupSize);
		threadPool.invoke(task);
		return task.join();
	}

	/**
	 * 测试用
	 * 
	 * @param <T>
	 * @param <R>
	 * @param list
	 * @param function
	 * @return
	 */
	public static <T, R> List<R> submitList2(List<List<T>> list, Function<List<T>, List<R>> function) {
		if (CollectionUtils.isEmpty(list) || function == null)
			return new ArrayList<>();

		List<CompletableFuture<List<R>>> futures = list.stream()
				.map(e -> CompletableFuture.completedFuture(e).thenApply(t -> function.apply(t))).collect(Collectors.toList());

		return futures.stream().map(CompletableFuture::join).reduce(new ArrayList<>(), (a, b) -> {
			a.addAll(b);
			return a;
		});
	}

	public static void submitTasks(Runnable runnable, Function<?, ?>... tasks) {

	}

	/**
	 * 普通单个任务
	 *
	 * @param task
	 */
	public static void submit(Runnable task) {
		if (task == null) {
			return;
		}

		threadPool.submit(task);
	}
}
