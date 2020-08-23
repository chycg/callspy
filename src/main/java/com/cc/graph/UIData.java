package com.cc.graph;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

public class UIData {

	private Painter painter;

	private Graphics2D g2d;

	private int width;

	private final List<Line> links = new ArrayList<>();

	private List<Node> nodes = new ArrayList<>();

	public UIData(Painter painter, Graphics2D g2d) {
		this.painter = painter;
		this.g2d = g2d;
		compute0();
	}

	public void compute() {
		boolean changed = nodes.size() != painter.getNodeSize() || links.stream().filter(e -> !e.isExitLine()).count() != painter.getLinkSize();

		if (changed) {
			compute0();
		}
	}

	private void compute0() {
		nodes.clear();
		nodes = painter.getAllNodes().stream().sorted((a, b) -> a.getOrder() - b.getOrder()).collect(Collectors.toList());
		nodes.forEach(e -> e.resetCounts());

		computeLinks();
		computeNodes();
	}

	private void computeLinks() {
		List<Line> allLinks = painter.getAllLinks();
		links.clear();
		Stack<Line> stack = new Stack<>();

		for (Line line : allLinks) {
			// if (line.isSelfInvoke()) {
			// links.add(line);
			// continue;
			// }

			int level = line.getLevel();
			if (stack.size() > 0 && level <= stack.peek().getLevel()) {
				while (!stack.isEmpty()) {
					Line last = stack.peek();
					if (last.getLevel() < level) {
						break;
					}

					stack.pop();
					links.add(last.makeExitLine());
				}
			}

			stack.push(line);
			links.add(line);
		}

		while (!stack.isEmpty()) {
			Line last = stack.pop();
			links.add(last.makeExitLine());
		}

		int i = 1;
		int index = 1;
		for (Line line : links) {
			line.setOrder(i++);
			if (line.isExitLine())
				continue;

			line.setIndex(index++);
			line.getFrom().addFromCount();
			line.getTo().addToCount();
		}
	}

	private void computeNodes() {
		nodes.removeIf(e -> e.getFromCount() == 0 && e.getToCount() == 0);
		if (nodes.isEmpty())
			return;

		int offsetX = 20;
		for (Node node : nodes) {
			node.setX(offsetX);
			offsetX += node.getWidth(g2d) + 20;
		}

		Node last = nodes.get(nodes.size() - 1);

		List<String> methods = links.stream().filter(e -> e.getFrom() == last && e.isSelfInvoke() && e.getInvoke() != null).map(e -> e.getMethod())
				.sorted((a, b) -> a.length() - b.length()).collect(Collectors.toList());

		if (methods.size() > 0) {
			String longText = methods.get(methods.size() - 1);
			offsetX += g2d.getFontMetrics().stringWidth(longText);
		}

		this.width = offsetX;
	}

	/**
	 * 总宽度
	 * 
	 * @return
	 */
	public int getWidth() {
		return width + Line.rectW;
	}

	public int getHeight() {
		return links.isEmpty() ? 100 : links.size() * 50 + Line.gap + (Node.height + painter.getGap()) * 2;
	}

	/**
	 * 包含返回行
	 * 
	 * @return
	 */
	public List<Line> getLinks() {
		return links;
	}

	public List<Node> getNodes() {
		return nodes;
	}
}
