package com.zeroturnaround.callspy.tree;

import java.awt.Component;
import java.awt.Font;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

public class TreeNodeRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = -2407874697670564555L;

	private static final String plain = "<span style='color:rgb(0,70,0)'>";
	private static final String selection = "<span style='color:white'>";

	private final Pattern p = Pattern.compile("\".*\"|[a-zA-Z_]+[a-zA-Z0-9_]*");

	private static Font font = new Font("Courier New", Font.PLAIN, 14);

	private Supplier<String> filterTextSupplier;

	private Set<String> keyWords = new HashSet<>(Arrays.asList("public", "void", "null", "class", "enum", "double", "extends", "implements", "final",
			"int", "long", "byte", "interface", "abstract", "true", "false"));

	public TreeNodeRenderer(Supplier<String> filterTextSupplier) {
		this.filterTextSupplier = filterTextSupplier;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		Object userObject = node.getUserObject();
		if (userObject instanceof Node) {
			Node data = (Node) userObject;
			String line = renderFilterMatch(node, data.getLine());

			String method = data.getMethod();
			int index = line.indexOf(method);
			String prefix = line.substring(0, index);

			String suffix = line.substring(line.indexOf('('), line.length());

			line = prefix + "<b>" + method + "</b>" + updateColor(suffix);
			String text = (sel ? selection : plain) + line + "</span>";

			this.setText("<html>" + text + "</html>");
			this.setFont(font);
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

	private String updateColor(String text) {
		StringBuilder sb = new StringBuilder();
		Matcher m = p.matcher(text);
		int pos = 0;

		while (m.find()) {
			String group = m.group();
			int start = m.start();
			int end = m.end();
			sb.append(text.substring(pos, start));

			if (text.charAt(start) == '"') {
				sb.append("<span style='color:rgb(0,0,255)'>").append(group).append("</span>");
			} else if (keyWords.contains(group)) {
				sb.append("<b><span style='color:rgb(127,0,85)'>").append(group).append("</span></b>");
			} else {
				sb.append(group);
			}

			pos = end;
		}
		return sb.toString();
	}

}