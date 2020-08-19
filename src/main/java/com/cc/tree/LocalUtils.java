package com.cc.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.cc.graph.Element;
import com.cc.graph.Painter;

public class LocalUtils {

	private static List<DefaultMutableTreeNode> matchedNode = new ArrayList<>();

	private static List<Element> matchedElements = new ArrayList<>();

	private static String lastText;

	private static void findNodes(DefaultMutableTreeNode parent, String text) {
		if (isMatched(parent, text))
			matchedNode.add(parent);

		for (int i = 0; i < parent.getChildCount(); i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(i);
			findNodes(child, text);
		}
	}

	private static boolean isMatched(DefaultMutableTreeNode treeNode, String text) {
		if (treeNode.getUserObject() instanceof Invocation) {
			Invocation node = (Invocation) treeNode.getUserObject();
			String line = node.getLine().toLowerCase();
			return line.contains(text.toLowerCase());
		}

		return false;
	}

	public static void selectNextTreeNode(JTree tree, String text, boolean next) {
		if (!Objects.equals(lastText, text)) {
			matchedNode.clear();
			findNodes((DefaultMutableTreeNode) tree.getModel().getRoot(), text);
			lastText = text;
		}

		if (matchedNode.isEmpty())
			return;

		DefaultMutableTreeNode selection = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		int index = selection == null ? -1 : matchedNode.indexOf(selection);
		index = next ? index + 1 : index - 1;

		if (index >= matchedNode.size())
			index = index % matchedNode.size();

		if (index < 0)
			index += matchedNode.size();

		TreePath path = new TreePath(matchedNode.get(index).getPath());
		tree.scrollPathToVisible(path);
		tree.expandPath(path);
		tree.clearSelection();
		tree.setSelectionPath(path);
	}

	public static void selectNextElement(Painter painter, String text, boolean next) {
		if (!Objects.equals(lastText, text)) {
			matchedElements.clear();

			String text2 = text.trim();
			matchedElements.addAll(painter.getAllNodes().stream().filter(e -> e.getName().contains(text2)).collect(Collectors.toList()));
			matchedElements.addAll(painter.getAllLinks().stream().filter(e -> e.getName().contains(text2)).collect(Collectors.toList()));

			lastText = text2;
		}

		if (matchedElements.isEmpty())
			return;

		Element selection = painter.getSelectedElements().isEmpty() ? null : painter.getSelectedElements().get(0);
		int index = selection == null ? -1 : matchedElements.indexOf(selection);
		index = next ? index + 1 : index - 1;

		if (index >= matchedElements.size())
			index = index % matchedElements.size();

		if (index < 0)
			index += matchedElements.size();

		Element nextOne = matchedElements.get(index);
		painter.setSelection(nextOne);
		painter.ensureVisible(nextOne);
	}
}