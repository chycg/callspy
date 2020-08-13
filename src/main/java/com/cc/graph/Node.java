package com.cc.graph;

import java.awt.Graphics2D;
import java.io.Serializable;

public class Node implements Serializable {

	private static final long serialVersionUID = -1365511871749616230L;

	public static final int gap = 3;

	public static final int height = 20;

	private String name;

	private int order;

	private int width;

	private int x;

	public Node(String name, int order) {
		this.name = name;
		this.order = order;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public int getWidth(Graphics2D g) {
		if (width < 1) {
			width = g.getFontMetrics().stringWidth(name) + gap * 2;
		}

		return width;
	}

	public int getWidth() {
		return width;
	}

	public int getCenterX() {
		return (int) (x + width / 2f);
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getHeight() {
		return height;
	}

	public String getName() {
		return name;
	}
}
