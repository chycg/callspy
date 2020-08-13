package com.cc.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.tree.DefaultMutableTreeNode;

import com.cc.tree.Invocation;

public class Painter extends JComponent implements Scrollable {

	private static final long serialVersionUID = -3629016612452078796L;

	private Map<String, Node> map = new HashMap<>();

	private List<Line> list = new ArrayList<>();

	private int mouseX;

	private int mouseY;

	private String targetName;

	private double ratio = 1;

	public Painter() {
		setFocusable(true);
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_0 && e.getModifiers() == InputEvent.CTRL_MASK) {
					ratio = 1;
					repaint();
				}
			}
		});

		MouseAdapter ma = new MouseAdapter() {

			@Override
			public void mouseMoved(MouseEvent e) {
				targetName = null;
				mouseX = e.getX();
				mouseY = e.getY();
				repaint();
			}

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if (e.getModifiers() == InputEvent.CTRL_MASK) {
					int rotation = e.getWheelRotation();
					ratio -= rotation * 0.1;
					repaint();
				} else {
					getParent().dispatchEvent(e);
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {
				Painter.this.requestFocus();
			}
		};

		addMouseListener(ma);
		addMouseMotionListener(ma);
		addMouseWheelListener(ma);
	}

	public void init(DefaultMutableTreeNode parent) {
		if (parent.getUserObject() instanceof Invocation) {
			Invocation o = (Invocation) parent.getUserObject();
			String className = o.getClassName();

			int count = parent.getChildCount();
			for (int i = 0; i < count; i++) {
				DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(i);
				Invocation o2 = (Invocation) child.getUserObject();
				addLine(className, o2.getClassName(), o.getMethodName());
				init(child);
			}
		} else {
			int count = parent.getChildCount();
			for (int i = 0; i < count; i++) {
				init((DefaultMutableTreeNode) parent.getChildAt(i));
			}
		}
	}

	public void treeChanged(DefaultMutableTreeNode node) {
		if (node.getUserObject() instanceof Invocation) {
			Invocation o = (Invocation) node.getUserObject();
			map.remove(o.getClassName());
			list.removeIf(e -> e.getFrom().getName().equals(o.getClassName()) || e.getTo().getName().equals(o.getClassName()));

			repaint();
		}
	}

	public void addLine(String from, String to, String method) {
		if (!map.containsKey(from))
			map.put(from, new Node(from, map.size()));

		if (!map.containsKey(to))
			map.put(to, new Node(to, map.size()));

		list.add(new Line(map.get(from), map.get(to), method));
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.clearRect(0, 0, getWidth(), getHeight());
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.transform(AffineTransform.getScaleInstance(ratio, ratio));
		int y = 10;
		int hGap = Node.height + 30;

		List<Node> nodes = map.values().stream().sorted((a, b) -> a.getOrder() - b.getOrder()).collect(Collectors.toList());
		g2d.setColor(Color.black);
		int offset = 5;
		int x = 5;
		int offsetY = ((JViewport) getParent()).getViewPosition().y;
		System.out.println(offsetY);
		for (Node node : nodes) {
			int w = node.getWidth(g2d);
			node.setX(x);

			BasicStroke stroke = new BasicStroke(1.1f);
			g2d.setStroke(stroke);
			g2d.drawRect(x, y, w, node.getHeight());
			g2d.drawString(node.getName(), x + Node.gap, y + Node.gap + g2d.getFontMetrics().getAscent());

			g2d.drawRect(x, y + offsetY, w, node.getHeight());
			g2d.drawString(node.getName(), x + Node.gap, y + offsetY + Node.gap + g2d.getFontMetrics().getAscent());

			stroke = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 3.5f, new float[] { 10, offset }, 0f);
			g2d.setStroke(stroke);
			g2d.drawLine(x + w / 2, y + node.getHeight(), x + w / 2, getHeight());

			if (mouseX >= x && mouseX < x + w)
				targetName = node.getName();

			x += w + 20;
		}

		for (int i = 0; i < list.size(); i++) {
			Line line = list.get(i);
			Node from = line.getFrom();
			Node to = line.getTo();

			int sx = from.getCenterX();
			int tx = to.getCenterX();
			int sy = i * 50 + hGap;

			g2d.setStroke(new BasicStroke(1f));

			if (from.getOrder() < to.getOrder()) {
				g2d.drawLine(sx, sy, tx, sy);
				g2d.drawLine(tx, sy, tx - offset, sy - offset);
				g2d.drawLine(tx, sy, tx - offset, sy + offset);

				g2d.drawString(line.getMethod(), sx + 10, sy - offset);
			} else if (from.getOrder() == to.getOrder()) {
				int h = 25;
				int w = 50;
				g2d.drawLine(sx, sy, sx + w, sy);

				g2d.drawLine(sx + w, sy, sx + w, sy + h);
				g2d.drawLine(sx + w, sy + h, sx, sy + h);

				g2d.drawLine(sx, sy + h, sx + offset, sy + h - offset);
				g2d.drawLine(sx, sy + h, sx + offset, sy + h + offset);

				g2d.drawString(line.getMethod(), sx + w + offset, sy + h / 2);
			} else if (from.getOrder() > to.getOrder()) {
				g2d.drawLine(sx, sy, tx, sy);
				g2d.drawLine(tx, sy, tx + offset, sy - offset);
				g2d.drawLine(tx, sy, tx + offset, sy + offset);

				g2d.drawString(line.getMethod(), sx - getStrWidth(g2d, line.getMethod()) - 10, sy - offset);
			}
		}

		if (targetName != null) {
			g2d.setColor(Color.red);
			g2d.drawString(targetName, mouseX, mouseY);
		}
	}

	private int getStrWidth(Graphics2D g, String text) {
		return g.getFontMetrics().stringWidth(text);
	}

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	/**
	 * @return
	 */
	@Override
	public Dimension getPreferredSize() {
		int width = Math.max(map.values().stream().mapToInt(e -> e.getWidth()).sum() + map.size() * 20 + 100, 400);
		int height = list.isEmpty() ? 100 : list.size() * 50 + 40;

		return new Dimension(width, height);
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 40;
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 100;
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}
}
