package com.cc.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

public class Node extends Element {

	private static final long serialVersionUID = -1365511871749616230L;

	public static final int gap = 3;

	public static final int height = 20;

	private int width;

	private int x;

	public Node(String name, int order) {
		super(name, order);
	}

	@Override
	public ElementType getType() {
		return ElementType.NODE;
	}

	public int getWidth(Graphics2D g) {
		if (width < 1) {
			width = g.getFontMetrics().stringWidth(getText()) + gap * 2;
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

	@Override
	public void paint(Graphics2D g2d) {
		g2d.setColor(Color.black);
		g2d.setStroke(new BasicStroke(isSelected() ? 1.5f : 1.1f));
		int w = getWidth(g2d);
		int y = 10;
		int h2 = getHeight();

		g2d.drawRect(x, y, w, h2 + 2);

		paintText(g2d, getText(), x + Node.gap, y + Node.gap + g2d.getFontMetrics().getAscent());
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(x, 10, width, height);
	}

	@Override
	public boolean isContain(Point2D p) {
		Rectangle rect = getBounds();
		return rect.x <= p.getX() && rect.x + rect.width >= p.getX();
	}

}
