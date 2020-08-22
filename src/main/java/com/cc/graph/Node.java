package com.cc.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

public class Node extends Element {

	private static final long serialVersionUID = -1365511871749616230L;

	public static final int gap = 2;

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

	public void decreaseFromCount() {
		fromCount--;
	}

	public void decreaseToCount() {
		toCount--;
	}

	public boolean isIsolated() {
		return fromCount == 0 && toCount == 0;
	}

	@Override
	public void paint(Graphics2D g2d) {
		g2d.setColor(Color.black);
		g2d.setStroke(new BasicStroke(isSelected() ? 1.5f : 1.1f));
		int w = getWidth(g2d);
		int h2 = getHeight();

		g2d.drawRect(x, 0, w, h2 + 1);
		g2d.setColor(new Color(240, 220, 200));
		g2d.fillRect(x + 1, 1, w - 2, h2);
		paintText(g2d, getName(), x + Node.gap, Node.gap + g2d.getFontMetrics().getAscent());
	}

	@Override
	public Rectangle getBounds() {
		Rectangle rect = getViewRect();
		return new Rectangle(x, rect.y + parent.getGap(), width, height);
	}

	@Override
	public boolean isContain(Point point) {
		Rectangle rect = getBounds();
		return rect.getX() <= point.getX() && rect.getX() + rect.getWidth() >= point.getX();
	}

	public boolean isNodeRange(Point point) {
		if (super.isContain(point))
			return true;

		Rectangle rect = getBounds();
		Rectangle viewRect = getViewRect();
		rect.y = viewRect.y + viewRect.height - getHeight() - parent.getGap();

		return rect.contains(point);
	}

	@Override
	public boolean isFullShowing() {
		Rectangle rect = getViewRect();
		return rect.x < x && rect.x + rect.width > x + width + parent.getGap();
	}

}
