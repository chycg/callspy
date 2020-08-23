package com.cc.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import com.cc.tree.Invocation;
import com.cc.tree.Mod;

public class Line extends Element {

	private static final long serialVersionUID = 1007906709323772156L;

	public static final int rectW = 50;
	public static final int rectH = 18;

	public static final int gap = 20;

	private static String upArrow = "↑";
	private static String downArrow = "↓";

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
		Line newLine = new Line(-1 * getId(), to, from, getName(), 0);
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
		// boolean fullShow = from.isFullShowing() && to.isFullShowing();
		String name = to.getName() + "." + getName();
		// if (!fullShow || !isSelfInvoke()) {
		// name = to.getName() + "." + name;
		// }

		return repeatCount == 1 ? name : name + " *" + repeatCount;
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

	public boolean isEntryLine() {
		return exitLine != null;
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
		return getOrder() * 50 + gap;
	}

	public boolean isSelfInvoke() {
		return from == to;
	}

	public boolean isL2R() {
		return from.getOrder() < to.getOrder();
	}

	public boolean isR2L() {
		return from.getOrder() > to.getOrder();
	}

	private String getContent() {
		int preIndex = isExitLine() ? entryLine.getIndex() : getIndex();
		String content = preIndex + ": " + getMethod();
		return content;
	}

	@Override
	public void paint(Graphics2D g2d) {
		int angleWidth = 6;
		int angleWidth2 = angleWidth / 2;

		int sx = from.getCenterX();
		int tx = to.getCenterX();
		int sy = getY();
		String content = getContent();

		if (isExitLine()) {
			g2d.setStroke(new BasicStroke(isSelected() ? 1.2f : 1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 2f, new float[] { 5, 4 }, 0f));
		} else {
			g2d.setStroke(new BasicStroke(isSelected() ? 2f : 1f));
		}

		g2d.setColor(getLineColor());

		textWidth = getStrWidth(g2d, content);
		int textY = g2d.getFontMetrics().getDescent();

		if (isL2R()) {
			g2d.drawLine(sx, sy, tx, sy);
			g2d.drawLine(tx, sy, tx - angleWidth, sy - angleWidth2);
			g2d.drawLine(tx, sy, tx - angleWidth, sy + angleWidth2);

			paintText(g2d, content, sx + 10, sy - textY);
			if (tx - sx > 600 && tx - sx > 2 * textWidth)
				paintText(g2d, content, tx - textWidth - 10, sy - textY);
		} else if (isR2L()) {
			g2d.drawLine(sx, sy, tx, sy);
			g2d.drawLine(tx, sy, tx + angleWidth, sy - angleWidth2);
			g2d.drawLine(tx, sy, tx + angleWidth, sy + angleWidth2);

			int leftX = tx;
			if (sx - leftX < textWidth + 20) // 短线，文字左对齐
				leftX = leftX + 10;
			else
				leftX = sx - textWidth - 10; // 长线，文字右对齐

			paintText(g2d, content, leftX, sy - textY);

			if (sx - tx > 600 && sx - tx > 2 * textWidth)
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

		// paintSideNode(g2d);

		paintExtraText(g2d);

		paintArgs(g2d);
	}

	// private void paintSideNode(Graphics2D g2d) {
	// boolean fullShow = from.isFullShowing() && to.isFullShowing();
	// if (isSelfInvoke() || fullShow)
	// return;
	//
	// Rectangle viewRect = getViewRect();
	// int leftX = viewRect.x + parent.getGap() - from.getX();
	// int rightX = viewRect.x + viewRect.width - parent.getGap() - from.getWidth() - to.getX();
	// int y = getY() - g2d.getFontMetrics().getAscent();
	//
	// if (!from.isFullShowing()) {
	// int x = isL2R() ? leftX : rightX;
	// g2d.translate(x, y);
	// from.paint(g2d);
	// g2d.translate(-x, -y);
	// }
	//
	// if (!to.isFullShowing()) {
	// int x = isL2R() ? rightX : leftX;
	// g2d.translate(x, y);
	// to.paint(g2d);
	// g2d.translate(-x, -y);
	// }
	// }

	private void paintArgs(Graphics2D g2d) {
		if (!isSelected())
			return;

		Invocation invoke = isEntryLine() ? getInvoke() : entryLine.getInvoke();
		int x = isEntryLine() ? from.getCenterX() : to.getCenterX();
		int y = getY() - g2d.getFontMetrics().getAscent() - g2d.getFontMetrics().getDescent();

		g2d.setColor(new Color(150, 150, 150));
		g2d.setFont(new Font("Verdana", Font.BOLD, 13));

		g2d.drawString(invoke.getLine(), x + 10, y);
	}

	private void paintExtraText(Graphics2D g2d) {
		int vx = getViewRect().x;
		int vw = getViewRect().width;
		int sy = getY();
		int textY = g2d.getFontMetrics().getDescent();

		if (!from.isFullShowing() && !to.isFullShowing()) {
			Color color = isSelected() ? Color.red : isEntryLine() ? new Color(50, 70, 80) : new Color(120, 67, 200);
			g2d.setColor(color);
			g2d.setFont(new Font("dialog", Font.BOLD, 13));
			String text = getContent();
			int strWidth = getStrWidth(g2d, text);
			String arrow = isEntryLine() ? downArrow : upArrow;

			g2d.drawString(text + arrow, vx + (vw - strWidth) / 2 - 10, sy - textY);
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
