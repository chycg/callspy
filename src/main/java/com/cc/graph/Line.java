package com.cc.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import com.cc.tree.Invocation;
import com.cc.tree.Mod;

public class Line extends Element {

	private static final long serialVersionUID = 1007906709323772156L;

	public static final int rectW = 50;
	public static final int rectH = 18;

	private final Node from;

	private final Node to;

	private int repeatCount = 1;

	private int textWidth;

	private int mod;

	private Line entryLine;

	private Line exitLine;

	/**
	 * 行号，影子线不计入；影子线与实际线编号保持一致
	 */
	private int index;

	public Line(Node from, Node to, String method, int order) {
		this(0, from, to, method, order);
	}

	public Line(int id, Node from, Node to, String method, int order) {
		super(id, method, order, from.getParent());
		this.from = from;
		this.to = to;
	}

	public Line makeExitLine() {
		Line newLine = new Line(to, from, getName(), 0);
		newLine.selected = selected;
		newLine.mod = mod;

		newLine.entryLine = this;
		this.exitLine = newLine;
		return newLine;
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
		return repeatCount == 1 ? getName() : getName() + " *" + repeatCount;
	}

	public Line getEntryLine() {
		return entryLine;
	}

	public Line getExitLine() {
		return exitLine;
	}

	/**
	 * repeat count
	 */
	public void addRepeatCount() {
		repeatCount++;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public boolean isExitLine() {
		return entryLine != null;
	}

	public Invocation getInvoke() {
		return (Invocation) getUserObject();
	}

	public void setInvoke(Invocation invoke) {
		setUserObject(invoke);
	}

	@Override
	public void setSelected(boolean selected) {
		super.setSelected(selected);
		if (entryLine != null)
			entryLine.selected = selected;

		if (exitLine != null)
			exitLine.selected = selected;
	}

	/**
	 * 调用层级
	 * 
	 * @return
	 */
	public int getLevel() {
		return getInvoke().getCount();
	}

	public int getY() {
		return getOrder() * 50;
	}

	public boolean isSelfInvoke() {
		return from == to;
	}

	@Override
	public void paint(Graphics2D g2d) {
		Node from = getFrom();
		Node to = getTo();
		int angleWidth = 6;
		int angleWidth2 = angleWidth / 2;

		int sx = from.getCenterX();
		int tx = to.getCenterX();
		int sy = getY();
		String content = (isExitLine() ? entryLine.getIndex() : getIndex()) + ": " + getMethod();

		if (isExitLine()) {
			g2d.setStroke(new BasicStroke(isSelected() ? 1.2f : 1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 2f, new float[] { 5, 4 }, 0f));
		} else {
			g2d.setStroke(new BasicStroke(isSelected() ? 2f : 1f));
		}

		g2d.setColor(getLineColor());

		textWidth = getStrWidth(g2d, content);
		int textY = g2d.getFontMetrics().getDescent();

		if (from.getOrder() < to.getOrder()) {
			g2d.drawLine(sx, sy, tx, sy);
			g2d.drawLine(tx, sy, tx - angleWidth, sy - angleWidth2);
			g2d.drawLine(tx, sy, tx - angleWidth, sy + angleWidth2);

			paintText(g2d, content, sx + 10, sy - textY);
			if (tx - sx > 600)
				paintText(g2d, content, tx - textWidth - 10, sy - textY);
		} else if (from.getOrder() > to.getOrder()) {
			g2d.drawLine(sx, sy, tx, sy);
			g2d.drawLine(tx, sy, tx + angleWidth, sy - angleWidth2);
			g2d.drawLine(tx, sy, tx + angleWidth, sy + angleWidth2);

			int leftX = tx;
			if (sx - leftX < textWidth + 20) // 短线，文字左对齐
				leftX = leftX + 10;
			else
				leftX = sx - textWidth - 10; // 长线，文字右对齐

			paintText(g2d, content, leftX, sy - textY);

			if (sx - tx > 600)
				paintText(g2d, content, tx + 10, sy - textY);
		} else if (isSelfInvoke()) {
			int offsetX = 5;
			g2d.drawLine(sx, sy, sx + rectW - offsetX, sy);

			g2d.drawLine(sx + rectW - offsetX, sy, sx + rectW - offsetX, sy + rectH);
			g2d.drawLine(sx + rectW - offsetX, sy + rectH, sx, sy + rectH);

			g2d.drawLine(sx, sy + rectH, sx + angleWidth, sy + rectH - angleWidth2);
			g2d.drawLine(sx, sy + rectH, sx + angleWidth, sy + rectH + angleWidth2);

			paintText(g2d, content, sx + 10, sy - textY);
		}
	}

	protected Color getLineColor() {
		if (isSelected())
			return Color.red;

		return Mod.getModByCode(mod).getColor();
	}

	/**
	 * 影子选中也算
	 */
	@Override
	public boolean isContain(Point point) {
		if (super.isContain(point))
			return true;

		// if (from == to) {
		// int sx = from.getCenterX();
		// int sy = getY();
		//
		// return new Rectangle(sx, sy - 20, textWidth, rectH).contains(point);
		// }

		return entryLine != null && entryLine.getBounds().contains(point) || exitLine != null && exitLine.getBounds().contains(point);
	}

	@Override
	public Rectangle getTextBounds() {
		if (isSelfInvoke())
			return getBounds();

		int x = Math.min(from.getCenterX(), to.getCenterX());
		int y = getY();
		int width = textWidth + 100;

		return new Rectangle(x, y, width, rectH);
	}

	@Override
	public Rectangle getBounds() {
		if (isSelfInvoke()) {
			int sx = from.getCenterX();
			int sy = getY();

			return new Rectangle(sx, sy, rectW, rectH);
		}

		int x = Math.min(from.getCenterX(), to.getCenterX());
		int y = getY();
		int width = Math.abs(from.getCenterX() - to.getCenterX());

		return new Rectangle(x - 2, y - 15, width + 4, 20);
	}

}
