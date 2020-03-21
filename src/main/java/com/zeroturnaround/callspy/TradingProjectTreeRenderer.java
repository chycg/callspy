package com.zeroturnaround.callspy;

import java.awt.Component;
import java.util.function.Supplier;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

public class TradingProjectTreeRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = -2407874697670564555L;

	private static final String SPAN_FORMAT = "<span style='color:%s;'><font size='4'>%s</font></span>";
	private Supplier<String> filterTextSupplier;

	public TradingProjectTreeRenderer(Supplier<String> filterTextSupplier) {
		this.filterTextSupplier = filterTextSupplier;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		Object userObject = node.getUserObject();
		if (userObject instanceof Node) {
			Node project = (Node) userObject;
			String text = String.format(SPAN_FORMAT, sel ? "rgb(255,255,255)" : "rgb(0,70,0)", renderFilterMatch(node, project.getLine()));
			this.setText("<html>" + text + "</html>");
		}

		return this;
	}

	private String renderFilterMatch(DefaultMutableTreeNode node, String text) {
		if (node.isRoot()) {
			return text;
		}
		String textToFilter = filterTextSupplier.get();
		return HTMLHighlighter.highlightText(text, textToFilter);
	}
}