package com.cc.graph.event;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.List;
import java.util.stream.Collectors;

import com.cc.graph.Element;
import com.cc.graph.Line;
import com.cc.graph.Node;
import com.cc.graph.Painter;

public class DefaultHandler extends MouseAdapter implements KeyListener {

	private Painter painter;

	private Point startPoint;

	private Point endPoint;

	private Rectangle dragRange = new Rectangle();

	public DefaultHandler(Painter painter) {
		this.painter = painter;
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();
		List<Element> selection = painter.getSelectedElements();
		if (code == KeyEvent.VK_0 && e.getModifiers() == InputEvent.CTRL_MASK) {
			painter.setRatio(1f);
			painter.repaint();
		} else if (code == KeyEvent.VK_DELETE) {
			painter.removeElements(selection);
		} else if (code == KeyEvent.VK_UP) {
			if (selection.size() == 1 || selection.get(0).isLine()) {
				Line line = (Line) selection.get(0);
				Line target = painter.getAllLinks().stream().filter(o -> o.getOrder() < line.getOrder()).sorted((a, b) -> b.getOrder() - a.getOrder())
						.findFirst().orElse(null);
				if (target != null) {
					painter.setSelection(target);
					painter.ensureVisible(target);
				}

				e.consume();
			}
		} else if (code == KeyEvent.VK_DOWN) {
			if (selection.size() == 1 || selection.get(0).isLine()) {
				Line line = (Line) selection.get(0);
				Line target = painter.getAllLinks().stream().filter(o -> o.getOrder() > line.getOrder()).findFirst().orElse(null);
				if (target != null) {
					painter.setSelection(target);
					painter.ensureVisible(target);
				}

				e.consume();
			}
		} else if (code == KeyEvent.VK_LEFT) {
			if (selection.size() == 1 || selection.get(0).isNode()) {
				List<Node> nodes = painter.getAllNodes().stream().sorted((a, b) -> b.getOrder() - a.getOrder()).collect(Collectors.toList());

				Node node = (Node) selection.get(0);
				Node target = nodes.stream().filter(o -> o.getOrder() < node.getOrder()).findFirst().orElse(null);
				if (target != null) {
					painter.setSelection(target);
					painter.scrollRectToVisible(target.getBounds());
				}

				e.consume();
			}
		} else if (code == KeyEvent.VK_RIGHT) {
			if (selection.size() == 1 || selection.get(0).isNode()) {
				List<Node> nodes = painter.getAllNodes().stream().sorted((a, b) -> a.getOrder() - b.getOrder()).collect(Collectors.toList());

				Node node = (Node) selection.get(0);
				Node target = nodes.stream().filter(o -> o.getOrder() > node.getOrder()).findFirst().orElse(null);
				if (target != null) {
					painter.setSelection(target);
					painter.scrollRectToVisible(target.getBounds());
				}

				e.consume();
			}
		} else if (code == KeyEvent.VK_F5) {
			painter.clean();
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.isControlDown()) {
			int rotation = e.getWheelRotation();
			painter.setRatio(painter.getRatio() - rotation * 0.1f);
			painter.repaint();
		} else {
			painter.getParent().dispatchEvent(e);
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		startPoint = e.getPoint();
		painter.requestFocus();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		startPoint = null;
		endPoint = null;
		painter.repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() != MouseEvent.BUTTON1)
			return;

		Element element = painter.getElementByLocation(getScalePoint(e.getPoint()));
		if (e.isControlDown()) {
			if (element == null)
				return;

			if (element.isSelected())
				painter.removeSelection(element);
			else
				painter.addSelection(element);
		} else {
			if (element == null || element.isSelected()) {
				painter.clearSelection();
			} else {
				boolean onlyNode = element.isNode() && ((Node) element).isNodeRange(getScalePoint(e.getPoint()));
				if (onlyNode) {
					painter.ensureVisible(element);
				} else {
					painter.ensureLineVisible(element);
				}

				painter.setSelection(element);
			}
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		endPoint = e.getPoint();
		painter.setRangeSelection(getDragRange());
		painter.repaint();
	}

	private Point getScalePoint(Point p) {
		return new Point((int) (p.x / painter.getRatio()), (int) (p.y / painter.getRatio()));
	}

	public Rectangle getDragRange() {
		if (startPoint == null || endPoint == null)
			return null;

		int x = Math.min(startPoint.x, endPoint.x);
		int y = Math.min(startPoint.y, endPoint.y);
		int width = Math.abs(startPoint.x - endPoint.x);
		int height = Math.abs(startPoint.y - endPoint.y);

		float ratio = painter.getRatio();
		dragRange.x = (int) (x / ratio);
		dragRange.y = (int) (y / ratio);
		dragRange.width = (int) (width / ratio);
		dragRange.height = (int) (height / ratio);

		return dragRange;

	}
}
