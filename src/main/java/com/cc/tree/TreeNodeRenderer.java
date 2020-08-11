package com.cc.tree;

import java.awt.Color;
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

	private static final String HighLightTemplate = "<span style='background:rgb(230,230,0);'>$1</span>";

	private static int fontSize = 14;

	static Font font = new Font("微软雅黑", Font.PLAIN, fontSize);

	private final Pattern p = Pattern.compile("\".*?\"|[a-zA-Z_]+[a-zA-Z0-9_]*");

	private final Color background = new Color(232, 242, 254);

	private Supplier<String> filterTextSupplier;

	private Set<String> keyWords = new HashSet<>(Arrays.asList("public", "void", "null", "class", "enum", "double", "extends", "implements", "final",
			"int", "long", "byte", "interface", "abstract", "true", "false"));

	public TreeNodeRenderer(Supplier<String> filterTextSupplier) {
		this.filterTextSupplier = filterTextSupplier;
	}

	public void updateFontSize(int rotation) {
		fontSize -= rotation;
	}

	public void resetFontSize() {
		fontSize = 14;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		Object userObject = node.getUserObject();
		if (userObject instanceof Node) {
			Node data = (Node) userObject;

			if (data.getLine().contains(data.getMethod())) {
				String tagLine = getTagLine(data);
				setText("<html><span style='color:rgb(0,30,80)'>" + renderFilterMatch(node, tagLine) + "</span></html>");
			}
		}

		this.setFont(font.deriveFont(font.getStyle(), fontSize));

		return this;
	}

	String getTagLine(Node data) {
		String method = data.getMethod();
		String line = data.getLine();

		int index = line.indexOf(method);
		if (index >= 0) {
			String className = data.getClassName();
			int dotIndex = line.indexOf(className);
			String prefix = line.substring(0, dotIndex);
			String suffix = line.substring(dotIndex + className.length());
			line = prefix + "<b>" + className + "</b>" + updateColor(suffix);
		}

		return line;
	}

	@Override
	public Color getBackgroundSelectionColor() {
		return background;
	}

	private String renderFilterMatch(DefaultMutableTreeNode node, String text) {
		if (node.isRoot())
			return text;

		String textToFilter = filterTextSupplier.get().trim();
		if (textToFilter.isEmpty())
			return text;

		try {
			text = text.replaceAll("(?i)(" + Pattern.quote(textToFilter) + ")", HighLightTemplate);
		} catch (Exception e) {
			return text;
		}

		return text;
	}

	private String updateColor(String text) {
		StringBuilder sb = new StringBuilder();
		Matcher m = p.matcher(text);
		int pos = 0;

		while (m.find()) {
			String group = m.group();
			int start = m.start();
			int end = m.end();
			sb.append(text, pos, start);

			if (text.charAt(start) == '"') {
				sb.append("<span style='color:rgb(0,0,255)'>").append(group).append("</span>");
			} else if (keyWords.contains(group)) {
				sb.append("<b><span style='color:rgb(127,0,85)'>").append(group).append("</span></b>");
			} else {
				sb.append(group);
			}

			pos = end;
		}

		sb.append(text.substring(pos));
		return sb.toString();
	}

}