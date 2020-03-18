package com.zeroturnaround.callspy;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = -1266662931999876034L;

	private DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
	private DefaultTreeModel model = new DefaultTreeModel(root);

	private JTextField tfFilter = new JTextField();
	private JTextField tfExclude = new JTextField();

	private JTree tree = new JTree(model);
	private DefaultMutableTreeNode parentNode = root;

	private int lastCount = -1;

	private AbstractAction copyAction = new AbstractAction("copy") {

		private static final long serialVersionUID = 9024135081208422380L;

		@Override
		public void actionPerformed(ActionEvent e) {
			TreePath path = tree.getSelectionPath();
			if (path == null)
				return;

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
			Node data = (Node) node.getUserObject();
			if (data == null)
				return;

			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			Transferable trans = new StringSelection(data.getLine());
			clipboard.setContents(trans, null);
		}
	};

	private AbstractAction removeAction = new AbstractAction("remove") {

		private static final long serialVersionUID = -5615934028594122494L;

		@Override
		public void actionPerformed(ActionEvent e) {
			TreePath path = tree.getSelectionPath();
			if (path == null)
				return;

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
			if (node == null)
				return;

			model.removeNodeFromParent(node);
		}
	};

	public static void main(String[] args) {
		new MainFrame();
	}

	public MainFrame() {
		initLayout();
		initListener();

		setTitle("trace");
		setSize(800, 600);
		setVisible(true);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void initLayout() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.add(tfFilter, BorderLayout.NORTH);
		panel.add(tfExclude, BorderLayout.SOUTH);
		tfExclude.setEditable(false);

		panel.add(new JScrollPane(tree));
		this.add(panel);

		parseFile(null);

		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}
	}

	private void initListener() {
		tfFilter.addActionListener(e -> {
			String text = tfFilter.getText().trim();
			if (text.isEmpty())
				return;

			tfFilter.setText(null);
			if (!tfExclude.getText().contains(text)) {
				tfExclude.setText(tfExclude.getText() + "," + text);
			}
			removeTreeNode(root, text);
		});

		tfExclude.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() > 1)
					tfExclude.setText(null);
			}
		});

		JPopupMenu popup = new JPopupMenu();
		popup.add(new JMenuItem(removeAction));
		popup.add(new JMenuItem(copyAction));

		tree.setComponentPopupMenu(popup);
	}

	private void removeTreeNode(DefaultMutableTreeNode node, String text) {
		if (node.getUserObject().getClass() == Node.class) {
			Node data = (Node) node.getUserObject();
			if (data != null && data.getLine().contains(text)) {
				model.removeNodeFromParent(node);
			}
		}

		int count = node.getChildCount();
		for (int i = count - 1; i >= 0; i--) {
			removeTreeNode((DefaultMutableTreeNode) node.getChildAt(i), text);
		}
	}

	private void parseFile(String path) {
		path = "D:\\git\\callspy\\src\\main\\java\\com\\zeroturnaround\\callspy\\user.log.1";

		try {
			List<String> list = Files.readAllLines(Paths.get(new File(path).toURI()));
			setTitle("Trace-" + list.get(0));

			String line = "";
			for (int i = 2; i < list.size(); i++) {
				String e = list.get(i);
				if (!e.startsWith("~"))
					continue;

				line += e;

				int k = i + 1;
				while (k < list.size()) {
					String nextLine = list.get(k);
					if (nextLine.startsWith("~"))
						break;

					line += nextLine;
					k++;
				}

				addNode(line);
				line = "";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void addNode(String line) {
		int count = countSpace(line);
		line = line.substring(count);
		line = line.replaceAll("> +", ">");

		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new Node(line));
		if (count > lastCount) {
			model.insertNodeInto(node, parentNode, parentNode.getChildCount());
			parentNode = node;
		} else {
			Node data = (Node) parentNode.getUserObject();
			if (count == lastCount && data.isResult(line)) {
				data.setResultLine(line);
			} else {
				TreeNode pp = parentNode.getParent();
				model.insertNodeInto(node, (MutableTreeNode) pp, pp.getChildCount());
				parentNode = node;
			}
		}

		lastCount = count;
	}

	private int countSpace(String line) {
		if (!line.startsWith("~"))
			return -1;

		for (int i = 0; i < line.length(); i++) {
			if (line.charAt(i) == '~')
				continue;

			return i;
		}

		return 0;
	}
}
