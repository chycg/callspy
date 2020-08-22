package com.cc.graph;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Element implements Serializable, Comparable<Element> {

	private static final long serialVersionUID = -8293213167943705488L;

	private static final AtomicInteger idMaker = new AtomicInteger(1);

	private final int id;

	protected final Painter parent;

	private final String name;

	protected boolean selected;

	private boolean highLighted;

	private int order;

	private Object userObject;

	public Element(int id, String name, int order, Painter parent) {
		this.id = id <= 0 ? idMaker.getAndIncrement() : id;
		this.order = order;
		this.name = name;
		this.parent = parent;
	}

	public abstract void paint(Graphics2D g);

	public abstract Rectangle getBounds();

	public Rectangle getTextBounds() {
		return getBounds();
	}

	public abstract ElementType getType();

	protected void paintText(Graphics2D g2d, String text, int x, int y) {
		int w = getStrWidth(g2d, text);
		int h = 20;
		if (isHighLighted()) {
			g2d.setColor(Color.yellow);
			g2d.fillRect(x, y - g2d.getFontMetrics().getAscent() - 1, w, h - 1);

			g2d.setColor(isSelected() ? Color.red : Color.green.darker());
		} else {
			g2d.setColor(getTextColor());
		}

		g2d.drawString(text, x, y);
	}

	protected Color getTextColor() {
		if (isSelected())
			return Color.red;

		return Color.black;
	}

	public boolean isNode() {
		return getType() == ElementType.NODE;
	}

	public boolean isLine() {
		return getType() == ElementType.LINE;
	}

	/**
	 * 在当前rectview中是否完全显示
	 * 
	 * @return
	 */
	public boolean isFullShowing() {
		return getViewRect().contains(getBounds());
	}

	public Painter getParent() {
		return parent;
	}

	public Rectangle getViewRect() {
		return parent.getViewRect();
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isSelected() {
		return selected;
	}

	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

	protected final int getStrWidth(Graphics2D g, String text) {
		return g.getFontMetrics().stringWidth(text);
	}

	public boolean isContain(Point point) {
		return getBounds().contains(point);
	}

	public boolean isHighLighted() {
		return highLighted;
	}

	public void highLighted(String text) {
		if (text == null || text.trim().isEmpty()) {
			highLighted = false;
			return;
		}

		highLighted = this.name.contains(text.trim());
	}

	@Override
	public String toString() {
		return getType().name() + "[" + name + "]";
	}

	@Override
	public int compareTo(Element o) {
		if (getType() == o.getType())
			return getOrder() - o.getOrder();

		if (isNode() && o.isLine())
			return -1;

		if (isLine() && o.isNode())
			return 1;

		return 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (id ^ id >>> 32);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Element other = (Element) obj;
		if (id != other.id)
			return false;

		return true;
	}
}
