
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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

	private Map<Integer, Line> links = new ConcurrentHashMap<>();

	private float ratio = 1;

	private List<Element> selection = new ArrayList<>();

	private List<SelectionListener> selectionListeners = new ArrayList<>();

	private List<DataChangeListener> dataChangeListeners = new ArrayList<>();

	private DefaultHandler handler = new DefaultHandler(this);

	private boolean popupEvent = true;

	private int gap = 15;

	private UIData uiData;

	private Line lastLine;

	public Painter() {
		setFocusable(true);
		setFont(new Font("微软雅黑", Font.BOLD, 15));

		addKeyListener(handler);
		addMouseListener(handler);
		addMouseMotionListener(handler);
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

	public void fireDataChangeEvent(int eventType, Integer targetId, Collection<? extends Element> target) {
		if (!popupEvent)
			return;

		DataChangeEvent e = new DataChangeEvent(this, eventType, targetId, target);
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
		List<Node> allNodes = getAllNodes();

		Element target = allNodes.stream().filter(e -> e.isNodeRange(p)).findFirst().orElse(null);
		if (target != null)
			return target;

		target = getAllLinks().stream().filter(e -> e.isContain(p)).findFirst().orElse(null);
		if (target != null)
			return target;

		target = allNodes.stream().filter(e -> e.isContain(p)).findFirst().orElse(null);
		return target;
	}

	public void addSelection(Element e) {
		addSelection(Arrays.asList(e));
	}

	public void addSelection(Collection<? extends Element> c) {
		if (c == null || c.isEmpty())
			return;

		c.removeIf(e -> selection.contains(e));
		if (c.isEmpty())
			return;

		for (Element e : c) {
			if (e == null)
				continue;

			e.setSelected(true);
			selection.add(e);
		}

		repaint();
		fireSelectionChangeEvent(SelectionEvent.ADD_SELECTION, c);
	}

	public void setRangeSelection(Rectangle rect) {
		List<Element> unselected = selection.stream().filter(e -> !rect.contains(e.getBounds())).collect(Collectors.toList());
		removeSelection(unselected);

		List<Node> nodes = getAllNodes().stream().filter(e -> rect.contains(e.getBounds())).collect(Collectors.toList());
		nodes.removeAll(selection);

		List<Line> links = getAllLinks().stream().filter(e -> rect.contains(e.getBounds())).collect(Collectors.toList());
		links.removeAll(selection);

		Collection<Element> c = new HashSet<>();
		c.addAll(nodes);
		c.addAll(links);

		addSelection(c);
	}

	public void removeSelection(Element e) {
		removeSelection(Arrays.asList(e));
	}

	public void removeSelection(Collection<? extends Element> c) {
		if (c == null || c.isEmpty())
			return;

		for (Element e : c) {
			if (e == null || !e.isSelected())
				continue;

			e.setSelected(false);
			selection.remove(e);
		}

		repaint();
		fireSelectionChangeEvent(SelectionEvent.REMOVE_SELECTION, c);
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

	public void clean() {
		Set<Node> isolatedSet = getAllNodes().stream().filter(e -> e.isIsolated()).collect(Collectors.toSet());
		if (isolatedSet.isEmpty())
			return;

		fireDataChangeEvent(DataChangeEvent.REMOVE, 0, isolatedSet);
		removeElements(isolatedSet);
	}

	protected Rectangle getViewRect() {
		return ((JViewport) getParent()).getViewRect();
	}

	/**
	 * @param target
	 */
	public void ensureVisible(Element target) {
		if (target.isLine()) {
			ensureLineVisible((Line) target);
		} else if (target.isNode()) {
			ensureNodeVisible((Node) target);
		}
	}

	private void ensureNodeVisible(Node node) {
		Rectangle rect = node.getTextBounds();
		scrollRectToVisible(getScaleRect(rect));
	}

	/**
	 * 出入线已显示、无法完整显示、不做移动
	 * 
	 * @param line
	 */
	private void ensureLineVisible(Line line) {
		Rectangle viewRect = getViewRect();
		Rectangle rect0 = line.getBounds();
		Rectangle rect1 = line.getExitLine() != null ? line.getExitLine().getTextBounds() : line.getEntryLine().getTextBounds();

		Rectangle rect = new Rectangle(rect0);
		rect.add(rect1);
		if (viewRect.contains(rect) || rect.width > viewRect.width || rect.height > viewRect.height)
			return;

		rect.y = Math.min(rect0.y, rect1.y) - 50;
		rect.height = Math.abs(rect0.y - rect1.y) + 120;

		scrollRectToVisible(getScaleRect(rect));
	}

	/**
	 * 点击单个线，显示当前线条范围；否则显示当前类下的首个调用线
	 * 
	 * @param target
	 */
	public void ensureFirstLineVisible(Element target) {
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
		Line line = relatedList.get(0);
		ensureLineVisible(line);
	}

	public Rectangle getScaleRect(Rectangle rect) {
		int x = (int) (rect.x / ratio);
		int y = (int) (rect.y / ratio);
		int w = (int) (rect.width / ratio);
		int h = (int) (rect.height / ratio);

		return new Rectangle(x, y, w, h);
	}

	public void removeElements(Collection<? extends Element> c) {
		if (Utils.isEmpty(c))
			return;

		c.forEach(e -> doRemove(e));
		repaint();
	}

	private void doRemove(Element element) {
		if (element == null || element.getName() == null)
			return;

		Set<Element> targets = new HashSet<>();
		targets.add(element);

		Integer targetId = 0;
		if (element.isNode()) {
			String className = element.getName();
			if (!nodes.containsKey(className))
				return;

			remove0(element);
			Set<Line> fromLines = links.values().stream().filter(e -> e.getFrom() == element).collect(Collectors.toSet());
			Set<Line> toLines = links.values().stream().filter(e -> e.getTo() == element).collect(Collectors.toSet());
			remove0(toLines); // to lines 直接删除不递归

			removeLines(fromLines); // from lines 递归删除
		} else if (element.isLine()) {
			Line line = (Line) element;
			targetId = line.getId();
			Set<Element> set = new HashSet<>();
			doRemoveLine(line, set);

			Node from = line.getFrom();
			String text = line.getName();
			Set<Line> sameMethods = links.values().stream()
					.filter(e -> e.getFrom() == from && e.getName().equals(text) && e.getOrder() - e.getExitLine().getOrder() == 1)
					.collect(Collectors.toSet());
			set.addAll(removeLines(sameMethods));

			targets.addAll(set);
		}

		fireDataChangeEvent(DataChangeEvent.REMOVE, targetId, targets);
	}

	private Set<Element> removeLines(Collection<Line> lines) {
		if (Utils.isEmpty(lines))
			return new HashSet<>();

		Set<Element> set = new HashSet<>();
		for (Line e : lines) {
			doRemoveLine(e, set);
		}

		return set;
	}

	private void doRemoveLine(Line line, Set<Element> set) {
		remove0(line);
		Set<Line> betweenLines = getBetweenLines(line);
		for (Line e : betweenLines) {
			set.add(e);
			remove0(e);
		}
	}

	private void remove0(Element e) {
		remove0(Arrays.asList(e));
	}

	private void remove0(Collection<? extends Element> c) {
		if (c == null || c.isEmpty())
			return;

		for (Element e : c) {
			if (e == null)
				continue;

			selection.remove(e);
			if (e.isLine()) {
				Line line = (Line) e;
				links.remove(e.getId());

				Node from = line.getFrom();
				Node to = line.getTo();

				from.decreaseFromCount();
				to.decreaseToCount();

				if (hasNode(from) && from.isIsolated()) {
					remove0(from);
					fireDataChangeEvent(DataChangeEvent.REMOVE, null, Arrays.asList(from));
				}

				if (hasNode(to) && to.isIsolated()) {
					remove0(to);
					fireDataChangeEvent(DataChangeEvent.REMOVE, null, Arrays.asList(to));
				}
			} else {
				nodes.remove(e.getName());
			}
		}
	}

	private boolean hasNode(Node node) {
		if (node == null)
			return false;

		return nodes.containsKey(node.getName());
	}

	private Set<Line> getBetweenLines(Line target) {
		Set<Line> set = new HashSet<>();
		if (target != null) {
			Line exitLine = target.getExitLine();
			if (exitLine != null) {
				int index = target.getOrder();
				int end = exitLine.getOrder();

				set = uiData.getLinks().stream().filter(e -> !e.isExitLine() && e.getOrder() > index && e.getOrder() < end)
						.collect(Collectors.toSet());
			}
		}

		return set;
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
				addLine(className, invoke);
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

	public void removeLine(Invocation invocation) {
		popupEvent = false;
		List<Line> allLinks = getAllLinks();
		Set<Line> lines = allLinks.stream().filter(e -> e.getTo().getName().endsWith(invocation.getClassName())
				&& e.getName().equals(invocation.getMethodName()) && invocation == e.getInvoke()).collect(Collectors.toSet());

		if (lines.size() > 0)
			removeElements(lines);

		popupEvent = true;
	}

	public Line addLine(String from, Invocation invocation) {
		String to = invocation.getClassName();
		String method = invocation.getMethodName();
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
			if (lastLine.getFrom() == fromNode && lastLine.getTo() == toNode && lastLine.getName().equals(method)) {
				lastLine.addRepeatCount();
				return lastLine;
			}
		}

		lastLine = new Line(invocation.getId(), fromNode, toNode, method, links.size());
		lastLine.setInvoke(invocation);
		lastLine.setMod(invocation.getMod());

		links.put(lastLine.getId(), lastLine);
		targets.add(lastLine);

		return lastLine;
	}

	private UIData computeSize() {
		if (uiData != null) {
			uiData.compute();
			return uiData;
		}

		uiData = new UIData(this, (Graphics2D) getGraphics());
		return uiData;
	}

	BasicStroke stroke = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 3.5f, new float[] { 10, 5 }, 0f);

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.clearRect(0, 0, getWidth(), getHeight());

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.transform(AffineTransform.getScaleInstance(ratio, ratio));
		g2d.setColor(Color.black);

		UIData data = computeSize();
		Rectangle rect = getViewRect();

		int top = (int) (rect.y / ratio) + gap;
		int rangeH = Math.min(rect.y + rect.height, getHeight());
		int bottom = (int) (rangeH / ratio - Node.height - gap);
		int left = (int) (rect.x / ratio);
		int right = (int) ((rect.x + rect.width) / ratio) - 40;

		int fontHeight = g2d.getFontMetrics().getAscent() + g2d.getFontMetrics().getDescent();

		for (Node node : data.getNodes()) {
			if (node.getX() + node.getWidth() < left)
				continue;

			if (node.getX() > right)
				break;

			g2d.translate(0, top);
			node.paint(g2d);
			g2d.translate(0, -top);

			g2d.setStroke(stroke);
			g2d.setColor(node.isSelected() ? Color.black : Color.lightGray);
			g2d.drawLine(node.getCenterX(), top + node.getHeight(), node.getCenterX(), bottom + gap);

			g2d.setColor(Color.cyan.darker());
			g2d.drawString(node.getCounter(), node.getCenterX() - String.valueOf(node.getFromCount()).length() * 10 - 1, top - 2);

			// bottom class
			g2d.translate(0, bottom);
			node.paint(g2d);
			g2d.translate(0, -bottom);
		}

		g2d.setFont(new Font("Dialog", Font.BOLD, 13));
		for (Line line : data.getLinks()) {
			if (line.getY() < top + Node.height + fontHeight) {
				continue;
			}

			if (line.getY() > bottom)
				break;

			line.paint(g2d);
		}

		Rectangle range = handler.getDragRange();
		if (range != null) {
			g2d.setColor(Color.blue);
			g2d.setStroke(stroke);
			g2d.drawRect(range.x, range.y, range.width, range.height);

			g2d.setColor(new Color(255, 200, 0, 90));
			g2d.fillRect(range.x + 1, range.y + 1, range.width - 2, range.height - 2);
		}

		// if (handler.getMovingPoint() != null) {
		// Point p = handler.getMovingPoint();
		// g2d.setColor(Color.red);
		// g2d.drawString(p.toString(), p.x + 2, p.y - 2);
		// g2d.drawLine(p.x, 0, p.x, getHeight());
		// g2d.drawLine(0, p.y, getWidth(), p.y);
		// }
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
		return links.values().stream().sorted((a, b) -> a.getId() - b.getId()).collect(Collectors.toList());
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

	public int getNodeSize() {
		return nodes.size();
	}

	public int getLinkSize() {
		return links.size();
	}

	/**
	 * @return
	 */
	@Override
	public Dimension getPreferredSize() {
		UIData data = computeSize();
		int width = data.getWidth();
		int height = data.getHeight();

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
