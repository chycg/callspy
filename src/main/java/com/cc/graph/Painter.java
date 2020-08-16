
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
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.tree.DefaultMutableTreeNode;

import com.cc.Utils;
import com.cc.graph.event.DataChangeEvent;
import com.cc.graph.event.DataChangeListener;
import com.cc.graph.event.DefaultHandler;
import com.cc.graph.event.SelectionEvent;
import com.cc.graph.event.SelectionListener;
import com.cc.tree.Invocation;

public class Painter extends JComponent implements Scrollable {

	private static final long serialVersionUID = -3629016612452078796L;

	private Map<String, Node> map = new HashMap<>();

	private List<Node> tmpNodes = new ArrayList<>();

	private List<Line> links = new ArrayList<>();

	private float ratio = 1;

	private List<Element> selection = new ArrayList<>();

	private List<SelectionListener> selectionListeners = new ArrayList<>();

	private List<DataChangeListener> dataChangeListeners = new ArrayList<>();

	private DefaultHandler handler = new DefaultHandler(this);

	public Painter() {
		setFocusable(true);
		setFont(new Font("微软雅黑", Font.BOLD, 15));

		addKeyListener(handler);
		addMouseListener(handler);
		addMouseWheelListener(handler);
	}

	public void addSelectionListener(SelectionListener listener) {
		if (listener == null || selectionListeners.contains(listener))
			return;

		selectionListeners.add(listener);
	}

	public void removeSelectionListener(SelectionListener listener) {
		if (listener != null)
			selectionListeners.remove(listener);
	}

	public void fireDataChangeEvent(Collection<? extends Element> target) {
		DataChangeEvent e = new DataChangeEvent(this);
		for (DataChangeListener listener : dataChangeListeners) {
			listener.dataChanged(e);
		}
	}

	public void addDataChangeListener(DataChangeListener listener) {
		if (listener == null || dataChangeListeners.contains(listener))
			return;

		dataChangeListeners.add(listener);
	}

	public void removeDataChangeListener(DataChangeListener listener) {
		if (listener != null)
			dataChangeListeners.remove(listener);
	}

	private void fireSelectionChangeEvent() {
		SelectionEvent e = new SelectionEvent(this);
		for (SelectionListener listener : selectionListeners) {
			listener.selectionChanged(e);
		}
	}

	public Element getElementByLocation(Point p) {
		Point2D p2 = new Point2D.Float(p.x / ratio, p.y / ratio);
		Element target = getAllLinks().stream().filter(e -> e.isContain(p2)).findFirst().orElse(null);
		if (target != null)
			return target;

		target = getAllNodes().stream().filter(e -> e.isContain(p2)).findFirst().orElse(null);
		return target;
	}

	public void addSelection(Element e) {
		if (e == null || e.isSelected())
			return;

		e.setSelected(true);
		selection.add(e);

		repaint();
		fireSelectionChangeEvent();
	}

	public void removeSelection(Element e) {
		if (e == null || !e.isSelected())
			return;

		e.setSelected(false);
		selection.remove(e);
		repaint();

		fireSelectionChangeEvent();
	}

	public void clearSelection() {
		selection.forEach(e -> e.setSelected(false));
		selection.clear();
		fireSelectionChangeEvent();

		repaint();
	}

	public void setSelection(Element e) {
		clearSelection();
		if (e != null) {
			e.setSelected(true);
			selection.add(e);
		}

		fireSelectionChangeEvent();
		repaint();
	}

	private Rectangle getViewRect() {
		return ((JViewport) getParent()).getViewRect();
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

		c.forEach(e -> remove0(e));

		fireDataChangeEvent(c);
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
			Invocation source = (Invocation) parent.getUserObject();
			String className = source.getClassName();

			int count = parent.getChildCount();
			for (int i = 0; i < count; i++) {
				DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(i);
				Invocation target = (Invocation) child.getUserObject();
				addLine(className, target.getClassName(), target.getMethodName());
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

	public void removeClassNode(String className) {
		Node node0 = map.get(className);
		if (node0 != null)
			removeElements(Arrays.asList(node0));
	}

	public void addLine(String from, String to, String method) {
		List<Element> targets = new ArrayList<>();
		if (!map.containsKey(from)) {
			Node fromNode = new Node(from, map.size());
			map.put(from, fromNode);

			targets.add(fromNode);
		}

		if (!map.containsKey(to)) {
			Node toNode = new Node(to, map.size());
			map.put(to, toNode);

			targets.add(toNode);
		}

		Node fromNode = map.get(from);
		Node toNode = map.get(to);

		if (links.size() > 1) {
			Line last = links.get(links.size() - 1);
			if (last.getFrom() == fromNode && last.getTo() == toNode && last.getText().equals(method)) {
				last.addCount();
				return;
			}
		}

		Line line = new Line(fromNode, toNode, method, links.size());
		links.add(line);
		targets.add(line);

		fireDataChangeEvent(targets);
	}

	private int computeSize() {
		tmpNodes.clear();
		tmpNodes = map.values().stream().sorted((a, b) -> a.getOrder() - b.getOrder()).collect(Collectors.toList());

		Graphics2D g2d = (Graphics2D) getGraphics();
		int offsetX = 20;
		for (Node node : tmpNodes) {
			node.setX(offsetX);
			offsetX += node.getWidth(g2d) + 20;
		}

		int i = 1;
		for (Line line : links) {
			line.setOrder(i++);
		}

		return offsetX;
	}

	@Override
	protected void paintComponent(Graphics g) {
		computeSize();
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.clearRect(0, 0, getWidth(), getHeight());
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.transform(AffineTransform.getScaleInstance(ratio, ratio));

		g2d.setColor(Color.black);
		Rectangle rect = getViewRect();
		int offsetY = rect.y + rect.height - 40;
		BasicStroke stroke = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 3.5f, new float[] { 10, 5 }, 0f);
		BasicStroke selectedStroke = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1f, new float[] { 4, 4 }, 0f);

		for (Node node : tmpNodes) {
			node.paint(g2d);

			g2d.setStroke(stroke);
			g2d.setColor(Color.lightGray);
			g2d.drawLine(node.getCenterX(), 10 + node.getHeight(), node.getCenterX(), getHeight());

			// bottom class
			g2d.translate(0, offsetY / ratio);
			node.paint(g2d);
			g2d.translate(0, -offsetY / ratio);

			if (node.isSelected()) {
				g2d.setColor(Color.blue);
				g2d.setStroke(selectedStroke);
				g2d.drawRect(node.getX() - 2, 8, node.getWidth() + 4, getHeight() - 10);
			}
		}

		g2d.setFont(new Font("Verdana", Font.BOLD, 13));
		for (Line line : links) {
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

	public float getRatio() {
		return ratio;
	}

	public void setRatio(float ratio) {
		this.ratio = ratio;
	}

	/**
	 * @return
	 */
	@Override
	public Dimension getPreferredSize() {
		int width = computeSize() + 100;
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
