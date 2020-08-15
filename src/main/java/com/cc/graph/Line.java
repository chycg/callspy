package com.cc.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Line extends Element {

	private static final long serialVersionUID = 1007906709323772156L;

	private static final int rectW = 50;
	private static final int rectH = 25;

	private final Node from;

	private final Node to;

	private int count = 1;

	public Line(Node from, Node to, String method, int order) {
		super(method, order);
		this.from = from;
		this.to = to;
	}

	@Override
	public ElementType getType() {
		return ElementType.LINE;
	}

	public Node getFrom() {
		return from;
	}

	public Node getTo() {
		return to;
	}

	public String getMethod() {
		return count == 1 ? getText() : getText() + " *" + count;
	}

	public void addCount() {
		count++;
	}

	@Override
	public void paint(Graphics2D g2d) {
		Node from = getFrom();
		Node to = getTo();
		int angleWidth = 6;
		int angleWidth2 = angleWidth / 2;

		int sx = from.getCenterX();
		int tx = to.getCenterX();
		int sy = getOrder() * 50;

		g2d.setStroke(new BasicStroke(isSelected() ? 2f : 1f));
		g2d.setColor(isSelected() ? Color.green.darker() : Color.black);

		if (from.getOrder() < to.getOrder()) {
			g2d.drawLine(sx, sy, tx, sy);
			g2d.drawLine(tx, sy, tx - angleWidth, sy - angleWidth2);
			g2d.drawLine(tx, sy, tx - angleWidth, sy + angleWidth2);

			paintText(g2d, getMethod(), sx + 10, sy - 5);
			// g2d.drawString(getMethod(), sx + 10, sy - angleWidth);
		} else if (from.getOrder() == to.getOrder()) {
			g2d.drawLine(sx, sy, sx + rectW, sy);

			g2d.drawLine(sx + rectW, sy, sx + rectW, sy + rectH);
			g2d.drawLine(sx + rectW, sy + rectH, sx, sy + rectH);

			g2d.drawLine(sx, sy + rectH, sx + angleWidth, sy + rectH - angleWidth2);
			g2d.drawLine(sx, sy + rectH, sx + angleWidth, sy + rectH + angleWidth2);

			paintText(g2d, getMethod(), sx + rectW + angleWidth, sy + g2d.getFontMetrics().getAscent());
			// g2d.drawString(getMethod(), sx + rectW + angleWidth, sy + rectH / 2);
		} else if (from.getOrder() > to.getOrder()) {
			g2d.drawLine(sx, sy, tx, sy);
			g2d.drawLine(tx, sy, tx + angleWidth, sy - angleWidth2);
			g2d.drawLine(tx, sy, tx + angleWidth, sy + angleWidth2);

			paintText(g2d, getMethod(), sx - getStrWidth(g2d, getMethod()) - 10, sy - 5);
			// g2d.drawString(getMethod(), sx - getStrWidth(g2d, getMethod()) - 10, sy - angleWidth);
		}
	}

	@Override
	public Rectangle getBounds() {
		if (from == to) {
			int sx = from.getCenterX();
			int sy = getOrder() * 50;

			return new Rectangle(sx, sy, rectW, rectH);
		}

		int x = Math.min(from.getCenterX(), to.getCenterX());
		int y = getOrder() * 50;
		int width = Math.abs(from.getCenterX() - to.getCenterX());

		return new Rectangle(x - 2, y - 30, width + 4, 40);
	}

}
