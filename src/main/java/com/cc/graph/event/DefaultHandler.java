package com.cc.graph.event;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.cc.graph.Element;
import com.cc.graph.Line;
import com.cc.graph.Node;
import com.cc.graph.Painter;

public class DefaultHandler extends MouseAdapter implements KeyListener {

	private Painter painter;

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
			Set<Node> linkedNodes = new HashSet<>();
			painter.getAllLinks().forEach(line -> {
				linkedNodes.add(line.getFrom());
				linkedNodes.add(line.getTo());
			});

			List<Node> allNodes = painter.getAllNodes();
			allNodes.removeAll(linkedNodes);
			painter.removeElements(allNodes);
			painter.fireDataChangeEvent(DataChangeEvent.REMOVE, allNodes);
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
		painter.requestFocus();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() != MouseEvent.BUTTON1)
			return;

		Element element = painter.getElementByLocation(e.getPoint());
		if (e.isControlDown()) {
			if (element == null)
				return;

			if (element.isSelected())
				painter.removeSelection(element);
			else
				painter.addSelection(element);
		} else {
			painter.setSelection(element);
		}
	}

}
