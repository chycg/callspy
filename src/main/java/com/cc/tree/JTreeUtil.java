package com.cc.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

public class JTreeUtil {

	private static List<DefaultMutableTreeNode> matchedNode = new ArrayList<>();

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
		if (treeNode.getUserObject() instanceof Node) {
			Node node = (Node) treeNode.getUserObject();
			return node.getLine().toLowerCase().contains(text.toLowerCase());
		}

		return false;
	}

	public static void selectNext(JTree tree, String text, boolean next) {
		DefaultMutableTreeNode selection = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

		if (!Objects.equals(lastText, text)) {
			matchedNode.clear();
			findNodes((DefaultMutableTreeNode) tree.getModel().getRoot(), text);
			lastText = text;
		}

		if (matchedNode.isEmpty())
			return;

		int index = matchedNode.indexOf(selection);
		index = next ? index + 1 : index - 1;

		if (index >= matchedNode.size())
			index = index % matchedNode.size();

		if (index < 0)
			index += matchedNode.size();

		TreePath path = new TreePath(matchedNode.get(index).getPath());
		tree.scrollPathToVisible(path);
		tree.expandPath(path);
		tree.setSelectionPath(path);
	}
}