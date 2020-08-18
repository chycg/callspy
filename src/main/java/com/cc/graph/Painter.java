
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
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

	private Map<String, Node> nodes = new HashMap<>();

	private List<Node> tmpNodes = new ArrayList<>();

	private TreeMap<Integer, Line> links = new TreeMap<>();

	private float ratio = 1;

	private List<Element> selection = new ArrayList<>();

	private List<SelectionListener> selectionListeners = new ArrayList<>();

	private List<DataChangeListener> dataChangeListeners = new ArrayList<>();

	private DefaultHandler handler = new DefaultHandler(this);

	private boolean popupEvent = true;

	private int gap = 10;

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

	public void fireDataChangeEvent(int eventType, Collection<? extends Element> target) {
		if (!popupEvent)
			return;

		DataChangeEvent e = new DataChangeEvent(this, eventType, target);
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

	private void fireSelectionChangeEvent(int eventType, Collection<? extends Element> target) {
		if (!popupEvent)
			return;

		SelectionEvent e = new SelectionEvent(this, eventType, target);
		for (SelectionListener listener : selectionListeners) {
			listener.selectionChanged(e);
		}
	}

	public Element getElementByLocation(Point p) {
		Point2D p2 = new Point2D.Float(p.x / ratio, p.y / ratio);
		List<Node> allNodes = getAllNodes();

		Element target = allNodes.stream().filter(e -> e.isNodeRange(p2)).findFirst().orElse(null);
		if (target != null)
			return target;

		target = getAllLinks().stream().filter(e -> e.isContain(p2)).findFirst().orElse(null);
		if (target != null)
			return target;

		target = allNodes.stream().filter(e -> e.isContain(p2)).findFirst().orElse(null);
		return target;
	}

	public void addSelection(Element e) {
		if (e == null || e.isSelected())
			return;

		e.setSelected(true);
		selection.add(e);

		repaint();
		fireSelectionChangeEvent(SelectionEvent.ADD_SELECTION, Arrays.asList(e));
	}

	public void removeSelection(Element e) {
		if (e == null || !e.isSelected())
			return;

		e.setSelected(false);
		selection.remove(e);
		repaint();

		fireSelectionChangeEvent(SelectionEvent.REMOVE_SELECTION, Arrays.asList(e));
	}

	public void clearSelection() {
		selection.forEach(e -> e.setSelected(false));
		selection.clear();
		fireSelectionChangeEvent(SelectionEvent.CLEAR_SELETION, new ArrayList<>());

		repaint();
	}

	public void setSelection(Element e) {
		clearSelection();
		if (e != null) {
			e.setSelected(true);
			selection.add(e);
		}

		List<Element> list = new ArrayList<>();
		if (e != null)
			list.add(e);

		fireSelectionChangeEvent(SelectionEvent.SET_SELETION, list);
		repaint();
	}

	protected Rectangle getViewRect() {
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

	public void ensureLineVisible(Element target) {
		if (target == null)
			return;

		if (target.isLine()) {
			ensureVisible(target);
			return;
		}

		Node node = (Node) target;
		if (node.getFromCount() == 0 && node.getToCount() == 0)
			return;

		List<Line> relatedList = getAllLinks().stream().filter(e -> e.getFrom() == node || e.getTo() == node).collect(Collectors.toList());
		Rectangle viewRect = getViewRect();
		float minY = viewRect.y / ratio;
		float maxY = (viewRect.y + viewRect.height) / ratio;
		for (Line e : relatedList) {
			Rectangle bounds = e.getBounds();
			if (minY < bounds.y && bounds.y < maxY)
				return;
		}

		Line line = relatedList.get(0);
		Rectangle rect = line.getBounds();
		int offsetY = (int) ((viewRect.y + viewRect.height - 40) / ratio);
		if (rect.y >= offsetY - 10)
			rect.y = rect.y + 50;

		rect.x = node.getCenterX() - node.getWidth();
		rect.width = node.getWidth() * 2;

		scrollRectToVisible(rect);
	}

	public void removeElements(Collection<? extends Element> c) {
		if (Utils.isEmpty(c))
			return;

		c.forEach(e -> remove0(e));

		repaint();
	}

	public void removeElements(Object source, Collection<? extends Element> c) {
		if (Utils.isEmpty(c))
			return;

		c.forEach(e -> remove0(e));

		fireDataChangeEvent(DataChangeEvent.REMOVE, c);
		repaint();
	}

	private void remove0(Element element) {
		if (element == null)
			return;

		selection.remove(element);
		if (element.isNode()) {
			String className = element.getText();
			nodes.remove(className);

			Set<Line> relatedLines = links.values().stream()
					.filter(e -> e.getFrom().getText().equals(className) || e.getTo().getText().equals(className)).collect(Collectors.toSet());

			links.values().removeAll(relatedLines);
			selection.removeAll(relatedLines);

			fireDataChangeEvent(DataChangeEvent.REMOVE, relatedLines);
		} else if (element.isLine()) {
			Line line = (Line) element;
			links.remove(line.getId());

			String text = line.getText();
			Node from = line.getFrom();
			Set<Line> sameInvocations = new HashSet<>();

			// 删除同类下的同名方法
			links.values().removeIf(e -> {
				if (e.getFrom() == from && e.getText().equals(text)) {
					selection.remove(e);
					sameInvocations.add(e);
					return true;
				}
				return false;
			});

			fireDataChangeEvent(DataChangeEvent.REMOVE, sameInvocations);
		}
	}

	/**
	 * init from tree
	 * 
	 * @param parent
	 */
	void init(DefaultMutableTreeNode parent) {
		if (parent.getUserObject() instanceof Invocation) {
			Invocation source = (Invocation) parent.getUserObject();
			String className = source.getClassName();

			int count = parent.getChildCount();
			for (int i = 0; i < count; i++) {
				DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(i);
				Invocation invoke = (Invocation) child.getUserObject();
				addLine(className, invoke.getClassName(), invoke.getMethodName(), invoke);
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

	public void removeLine(String className, String methodName, Invocation invocation) {
		popupEvent = false;
		List<Line> allLinks = getAllLinks();
		Set<Line> lines = allLinks.stream()
				.filter(e -> e.getTo().getText().endsWith(className) && e.getText().equals(methodName) && invocation == e.getUserObject())
				.collect(Collectors.toSet());

		if (lines.size() > 0)
			removeElements(lines);

		popupEvent = true;
	}

	public Line addLine(String from, String to, String method, Invocation invocation) {
		List<Element> targets = new ArrayList<>();
		if (!nodes.containsKey(from)) {
			Node fromNode = new Node(from, nodes.size(), this);
			nodes.put(from, fromNode);

			targets.add(fromNode);
		}

		if (!nodes.containsKey(to)) {
			Node toNode = new Node(to, nodes.size(), this);
			nodes.put(to, toNode);

			targets.add(toNode);
		}

		Node fromNode = nodes.get(from);
		Node toNode = nodes.get(to);

		if (links.size() > 1) {
			Line last = links.lastEntry().getValue();
			if (last.getFrom() == fromNode && last.getTo() == toNode && last.getText().equals(method)) {
				last.addCount();
				return last;
			}
		}

		Line line = new Line(fromNode, toNode, method, links.size());
		line.setUserObject(invocation);
		line.setMod(invocation.getMod());

		links.put(line.getId(), line);
		targets.add(line);

		fireDataChangeEvent(DataChangeEvent.ADD, targets);

		return line;
	}

	private int computeSize() {
		tmpNodes.clear();
		tmpNodes = nodes.values().stream().sorted((a, b) -> a.getOrder() - b.getOrder()).collect(Collectors.toList());

		Graphics2D g2d = (Graphics2D) getGraphics();
		int offsetX = 20;
		for (Node node : tmpNodes) {
			node.setX(offsetX);
			node.resetCounts();
			offsetX += node.getWidth(g2d) + 20;
		}

		int i = 1;
		List<Line> allLinks = getAllLinks();
		for (Line line : allLinks) {
			line.setOrder(i++);
			line.getFrom().addFromCount();
			line.getTo().addToCount();
		}

		tmpNodes.removeIf(e -> e.getFromCount() == 0 && e.getToCount() == 0);

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

		for (Node node : tmpNodes) {
			node.paint(g2d);
			g2d.setStroke(stroke);
			g2d.setColor(node.isSelected() ? Color.black : Color.lightGray);
			g2d.drawLine(node.getCenterX(), 10 + node.getHeight(), node.getCenterX(), getHeight());

			g2d.setColor(Color.cyan.darker());
			int counterY = (int) ((rect.y + rect.height / 2) / ratio) / 50 * 50;
			g2d.drawString(node.getCounter(), node.getCenterX() - String.valueOf(node.getFromCount()).length() * 10 - 1, counterY - 18);

			// bottom class
			g2d.translate(0, offsetY / ratio);
			node.paint(g2d);
			g2d.translate(0, -offsetY / ratio);
		}

		g2d.setFont(new Font("Verdana", Font.BOLD, 13));
		List<Line> allLinks = getAllLinks();
		for (Line line : allLinks) {
			line.paint(g2d);
		}
	}

	public void highLights(String text) {
		nodes.values().forEach(e -> {
			e.highLighted(text);
		});

		getAllLinks().forEach(e -> {
			e.highLighted(text);
		});

		repaint();
	}

	public List<Node> getAllNodes() {
		return nodes.values().stream().sorted().collect(Collectors.toList());
	}

	public List<Line> getAllLinks() {
		return new ArrayList<>(links.values());
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

	public int getGap() {
		return gap;
	}

	public void setGap(int gap) {
		this.gap = gap;
	}

	/**
	 * @return
	 */
	@Override
	public Dimension getPreferredSize() {
		List<Node> tmpNodes = nodes.values().stream().sorted((a, b) -> a.getOrder() - b.getOrder()).collect(Collectors.toList());

		int offsetX = 20;
		for (Node node : tmpNodes) {
			offsetX += node.getWidth() + 20;
		}

		int width = offsetX + 50;
		int height = links.isEmpty() ? 100 : links.size() * 50 + 40;

		return new Dimension(width, height);
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return getHeight() / 60;
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		return getHeight() / 120;
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
