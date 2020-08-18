package com.cc.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

import com.cc.tree.Mod;

public class Line extends Element {

	private static final long serialVersionUID = 1007906709323772156L;

	private static final int rectW = 50;
	private static final int rectH = 18;

	private final Node from;

	private final Node to;

	private int count = 1;

	private int textWidth;

	private int mod;

	public Line(Node from, Node to, String method, int order) {
		this(0, from, to, method, order);
	}

	public Line(int id, Node from, Node to, String method, int order) {
		super(id, method, order, from.getParent());
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

	public int getMod() {
		return mod;
	}

	public void setMod(int mod) {
		this.mod = mod;
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
		String content = getOrder() + ": " + getMethod();

		g2d.setStroke(new BasicStroke(isSelected() ? 2f : 1f));
		g2d.setColor(getLineColor());

		textWidth = getStrWidth(g2d, content);
		if (from.getOrder() < to.getOrder()) {
			g2d.drawLine(sx, sy, tx, sy);
			g2d.drawLine(tx, sy, tx - angleWidth, sy - angleWidth2);
			g2d.drawLine(tx, sy, tx - angleWidth, sy + angleWidth2);

			paintText(g2d, content, sx + 10, sy - 5);
			if (tx - sx > 800)
				paintText(g2d, content, tx - textWidth - 10, sy - 5);
		} else if (from.getOrder() == to.getOrder()) {
			int offsetX = 8;
			g2d.drawLine(sx, sy, sx + rectW - offsetX, sy);

			g2d.drawLine(sx + rectW - offsetX, sy, sx + rectW - offsetX, sy + rectH);
			g2d.drawLine(sx + rectW - offsetX, sy + rectH, sx, sy + rectH);

			g2d.drawLine(sx, sy + rectH, sx + angleWidth, sy + rectH - angleWidth2);
			g2d.drawLine(sx, sy + rectH, sx + angleWidth, sy + rectH + angleWidth2);

			paintText(g2d, content, sx + 2, sy - g2d.getFontMetrics().getDescent());
		} else if (from.getOrder() > to.getOrder()) {
			g2d.drawLine(sx, sy, tx, sy);
			g2d.drawLine(tx, sy, tx + angleWidth, sy - angleWidth2);
			g2d.drawLine(tx, sy, tx + angleWidth, sy + angleWidth2);

			paintText(g2d, content, sx - textWidth - 10, sy - 5);
			if (sx - tx > 800)
				paintText(g2d, content, tx + 10, sy - 5);
		}
	}

	protected Color getLineColor() {
		if (isSelected())
			return Color.red;

		return Mod.getModByCode(mod).getColor();
	}

	@Override
	public boolean isContain(Point2D p) {
		if (getBounds().contains(p))
			return true;

		if (from == to) {
			int sx = from.getCenterX();
			int sy = getOrder() * 50;

			return new Rectangle(sx, sy - 20, textWidth, rectH).contains(p);
		}

		return false;
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
