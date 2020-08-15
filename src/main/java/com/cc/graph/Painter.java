
package com.cc.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.tree.DefaultMutableTreeNode;

import com.cc.Utils;
import com.cc.tree.Invocation;

public class Painter extends JComponent implements Scrollable {

	private static final long serialVersionUID = -3629016612452078796L;

	private Map<String, Node> map = new HashMap<>();

	private List<Line> links = new ArrayList<>();

	private float ratio = 1;

	private List<Element> selection = new ArrayList<>();

	public Painter() {
		setFocusable(true);
		setFont(new Font("微软雅黑", Font.PLAIN, 14));
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				int code = e.getKeyCode();
				if (code == KeyEvent.VK_0 && e.getModifiers() == InputEvent.CTRL_MASK) {
					ratio = 1;
					repaint();
				} else if (code == KeyEvent.VK_DELETE) {
					removeElements(getSelectedElements());
				} else if (code == KeyEvent.VK_UP) {
					if (selection.size() == 1 || selection.get(0).isLine()) {
						Line line = (Line) selection.get(0);
						Line target = links.stream().filter(o -> o.getOrder() < line.getOrder()).sorted((a, b) -> b.getOrder() - a.getOrder())
								.findFirst().orElse(null);
						if (target != null) {
							setSelection(target);
							ensureVisible(target);
						}

						e.consume();
					}
				} else if (code == KeyEvent.VK_DOWN) {
					if (selection.size() == 1 || selection.get(0).isLine()) {
						Line line = (Line) selection.get(0);
						Line target = links.stream().filter(o -> o.getOrder() > line.getOrder()).findFirst().orElse(null);
						if (target != null) {
							setSelection(target);
							ensureVisible(target);
						}

						e.consume();
					}
				} else if (code == KeyEvent.VK_LEFT) {
					if (selection.size() == 1 || selection.get(0).isNode()) {
						List<Node> nodes = map.values().stream().sorted((a, b) -> b.getOrder() - a.getOrder()).collect(Collectors.toList());

						Node node = (Node) selection.get(0);
						Node target = nodes.stream().filter(o -> o.getOrder() < node.getOrder()).findFirst().orElse(null);
						if (target != null) {
							setSelection(target);
							scrollRectToVisible(target.getBounds());
						}

						e.consume();
					}
				} else if (code == KeyEvent.VK_RIGHT) {
					if (selection.size() == 1 || selection.get(0).isNode()) {
						List<Node> nodes = map.values().stream().sorted((a, b) -> a.getOrder() - b.getOrder()).collect(Collectors.toList());

						Node node = (Node) selection.get(0);
						Node target = nodes.stream().filter(o -> o.getOrder() > node.getOrder()).findFirst().orElse(null);
						if (target != null) {
							setSelection(target);
							scrollRectToVisible(target.getBounds());
						}

						e.consume();
					}
				} else if (code == KeyEvent.VK_F5) {
					Set<Node> linkedNodes = new HashSet<>();
					links.forEach(line -> {
						linkedNodes.add(line.getFrom());
						linkedNodes.add(line.getTo());
					});

					Set<Node> allNodes = new HashSet<>(map.values());
					allNodes.removeAll(linkedNodes);
					removeElements(allNodes);
				}
			}
		});

		MouseAdapter ma = new MouseAdapter() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if (e.isControlDown()) {
					int rotation = e.getWheelRotation();
					ratio -= rotation * 0.1f;
					repaint();
				} else {
					getParent().dispatchEvent(e);
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {
				Painter.this.requestFocus();
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() != MouseEvent.BUTTON1)
					return;

				Element element = getElementByLocation(e.getPoint());
				if (e.isControlDown()) {
					if (element == null)
						return;

					if (element.isSelected())
						removeSelection(element);
					else
						addSelection(element);
				} else {
					setSelection(element);
				}
			}
		};

		addMouseListener(ma);
		addMouseWheelListener(ma);
	}

	private Element getElementByLocation(Point p) {
		Point2D p2 = new Point2D.Float(p.x / ratio, p.y / ratio);
		for (Line e : links) {
			if (e.isContain(p2))
				return e;
		}

		for (Node e : map.values()) {
			if (e.isContain(p2)) {
				return e;
			}
		}

		return null;
	}

	public void addSelection(Element e) {
		if (e == null || e.isSelected())
			return;

		e.setSelected(true);
		selection.add(e);

		repaint();
	}

	public void removeSelection(Element e) {
		if (e == null || !e.isSelected())
			return;

		e.setSelected(false);
		selection.remove(e);
		repaint();
	}

	public void clearSelection() {
		selection.forEach(e -> e.setSelected(false));
		selection.clear();

		repaint();
	}

	public void setSelection(Element e) {
		clearSelection();
		if (e != null) {
			e.setSelected(true);
			selection.add(e);
		}

		repaint();
	}

	private Rectangle getViewRect() {
		JViewport port = (JViewport) getParent();
		Rectangle rect = port.getViewRect();
		return rect;
	}

	/**
	 * @param target
	 */
	public void ensureVisible(Element target) {
		Rectangle rect = target.getBounds();
		Rectangle viewRect = getViewRect();
		if (viewRect.getHeight() - rect.y < 100)
			rect.height += 100;

		scrollRectToVisible(rect);
	}

	public void removeElements(Collection<? extends Element> c) {
		if (Utils.isEmpty(c))
			return;

		for (Element e : c) {
			remove0(e);
		}

		repaint();
	}

	private void remove0(Element element) {
		if (element == null)
			return;

		selection.remove(element);
		if (element.isNode()) {
			String className = element.getText();
			map.remove(className);

			Set<Line> relatedLines = links.stream().filter(e -> e.getFrom().getText().equals(className) || e.getTo().getText().equals(className))
					.collect(Collectors.toSet());

			links.removeAll(relatedLines);
			selection.removeAll(relatedLines);
		} else if (element.isLine()) {
			Line line = (Line) element;
			String text = line.getText();

			Node from = line.getFrom();
			links.remove(element);
			links.removeIf(e -> e.getFrom() == from && e.getText().equals(text)); // 删除同类下的同名方法
		}
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

	public List<Element> getSelectedElements() {
		return new ArrayList<>(selection);
	}

	public void treeChanged(DefaultMutableTreeNode node) {
		if (node.getUserObject() instanceof Invocation) {
			Invocation o = (Invocation) node.getUserObject();

			Node node0 = map.get(o.getClassName());
			removeElements(Arrays.asList(node0));
		}
	}

	public void addLine(String from, String to, String method) {
		if (!map.containsKey(from))
			map.put(from, new Node(from, map.size()));

		if (!map.containsKey(to))
			map.put(to, new Node(to, map.size()));

		Node fromNode = map.get(from);
		Node toNode = map.get(to);

		if (links.size() > 1) {
			Line last = links.get(links.size() - 1);
			if (last.getFrom() == fromNode && last.getTo() == toNode) {
				last.addCount();
				return;
			}
		}

		links.add(new Line(fromNode, toNode, method, links.size()));
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.clearRect(0, 0, getWidth(), getHeight());
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.transform(AffineTransform.getScaleInstance(ratio, ratio));

		List<Node> nodes = map.values().stream().sorted((a, b) -> a.getOrder() - b.getOrder()).collect(Collectors.toList());
		g2d.setColor(Color.black);
		int offsetX = 20;
		Rectangle rect = getViewRect();
		int offsetY = rect.y + rect.height - 40;
		for (Node node : nodes) {
			node.setX(offsetX);
			node.paint(g2d);

			BasicStroke stroke = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 3.5f, new float[] { 10, 5 }, 0f);
			g2d.setStroke(stroke);
			g2d.setColor(Color.gray);
			g2d.drawLine(node.getCenterX(), 10 + node.getHeight(), node.getCenterX(), getHeight());

			// bottom class
			g2d.translate(0, offsetY / ratio);
			node.paint(g2d);
			g2d.translate(0, -offsetY / ratio);

			if (node.isSelected()) {
				g2d.setColor(Color.blue);
				stroke = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1f, new float[] { 4, 4 }, 0f);
				g2d.setStroke(stroke);
				g2d.drawRect(node.getX() - 2, 8, node.getWidth() + 4, getHeight() - 10);
			}

			offsetX += node.getWidth() + 20;
		}

		int i = 1;
		for (Line line : links) {
			line.setOrder(i++);
			line.paint(g2d);
		}
	}

	public void highLights(String text) {
		map.values().forEach(e -> {
			e.highLighted(text);
		});

		links.forEach(e -> {
			e.highLighted(text);
		});

		repaint();
	}

	public List<Node> getAllNodes() {
		return map.values().stream().sorted().collect(Collectors.toList());
	}

	public List<Line> getAllLinks() {
		return new ArrayList<>(links);
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
		int width = map.values().stream().mapToInt(e -> e.getWidth()).sum() + map.size() * 20;
		int height = links.isEmpty() ? 100 : links.size() * 50 + 40;

		return new Dimension(width, height);
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return getHeight() / 40;
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		return getHeight() / 40;
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
