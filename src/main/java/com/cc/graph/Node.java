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

	private int fromCount;

	private int toCount;

	public Node(String name, int order, Painter parent) {
		this(0, name, order, parent);
	}

	public Node(int id, String name, int order, Painter parent) {
		super(id, name, order, parent);
	}

	@Override
	public ElementType getType() {
		return ElementType.NODE;
	}

	public int getWidth(Graphics2D g) {
		if (width < 1) {
			width = g.getFontMetrics().stringWidth(getName()) + gap * 2;
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

	public void resetCounts() {
		this.fromCount = 0;
		this.toCount = 0;
	}

	public String getCounter() {
		return fromCount + ":" + toCount;
	}

	public int getHeight() {
		return height;
	}

	public int getFromCount() {
		return fromCount;
	}

	public int getToCount() {
		return toCount;
	}

	public void addFromCount() {
		fromCount++;
	}

	public void addToCount() {
		toCount++;
	}

	@Override
	public void paint(Graphics2D g2d) {
		g2d.setColor(Color.black);
		g2d.setStroke(new BasicStroke(isSelected() ? 1.5f : 1.1f));
		int w = getWidth(g2d);
		int gap = parent.getGap();
		int h2 = getHeight();

		g2d.drawRect(x, gap, w, h2 + 2);
		g2d.setColor(new Color(240, 220, 150));
		g2d.fillRect(x + 1, gap + 1, w - 2, h2);
		paintText(g2d, getName(), x + Node.gap, gap + Node.gap + g2d.getFontMetrics().getAscent());
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

	public boolean isNodeRange(Point2D point) {
		Rectangle rect = getBounds();
		if (rect.contains(point))
			return true;

		Rectangle viewRect = parent.getViewRect();
		rect.y = viewRect.y + viewRect.height - getHeight() - parent.getGap();

		return rect.contains(point);
	}
}
