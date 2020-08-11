package com.cc.tree;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = -1266662931999876034L;

	private String path = "";
	private final String spaceChar = "~";

	private DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
	private DefaultTreeModel model = new DefaultTreeModel(root);

	private JTextField tfFilter = new JTextField();
	private JButton btnFile = new JButton("New");

	private JTextPane taDetail = new JTextPane();

	private JTextField tfSelection = new JTextField();

	private JTree tree = new JTree(model);
	private DefaultMutableTreeNode parentNode = root;

	private int lastCount = -1;

	private Font font = new Font("微软雅黑", Font.PLAIN, 14);

	private TreeNodeRenderer renderer;

	private String lastPath = "d:/";

	private int nodeCount = 0;

	private String title;

	private AbstractAction copyAction = new AbstractAction("copy") {

		private static final long serialVersionUID = 9024135081208422380L;

		@Override
		public void actionPerformed(ActionEvent e) {
			JMenuItem mi = (JMenuItem) e.getSource();
			JPopupMenu p = (JPopupMenu) mi.getParent();
			Component c = p.getInvoker();
			String text = null;
			if (c == tree) {
				TreePath path = tree.getSelectionPath();
				if (path == null)
					return;

				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
				Node data = (Node) node.getUserObject();
				if (data == null)
					return;

				text = data.getLine();
			} else if (c == tfSelection) {
				if (tfSelection.getSelectedText().length() > 0) {
					text = tfSelection.getSelectedText();
				} else {
					text = tfSelection.getText();
				}
			} else if (c == taDetail) {
				if (taDetail.getSelectedText() != null && taDetail.getSelectedText().length() > 0) {
					text = taDetail.getSelectedText();
				} else {
					Document document = taDetail.getDocument();
					try {
						text = document.getText(0, document.getLength());
						text = text.substring(0, text.indexOf('('));
					} catch (BadLocationException e1) {
						e1.printStackTrace();
					}
				}
			}

			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			Transferable trans = new StringSelection(text);
			clipboard.setContents(trans, null);
		}
	};

	private AbstractAction copyMethodAction = new AbstractAction("copyMethod") {

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
			Transferable trans = new StringSelection(data.getMethodName());
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

			DefaultMutableTreeNode target = findTarget(node, null);
			removeTreeNode(node, null);
			tree.setSelectionPath(new TreePath(target.getPath()));
		}
	};

	private AbstractAction removeMethodAction = new AbstractAction("removeMethod") {

		private static final long serialVersionUID = -5615934028594122494L;

		@Override
		public void actionPerformed(ActionEvent e) {
			TreePath path = tree.getSelectionPath();
			if (path == null)
				return;

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
			if (node == null)
				return;

			Node data = (Node) node.getUserObject();
			if (data != null) {
				DefaultMutableTreeNode target = findTarget(node, data.getMethod());
				removeTreeNode(root, data.getMethod());
				tree.setSelectionPath(new TreePath(target.getPath()));
			}
		}
	};

	private AbstractAction removeClassAction = new AbstractAction("removeClass") {

		private static final long serialVersionUID = -3540063801864849754L;

		@Override
		public void actionPerformed(ActionEvent e) {
			TreePath path = tree.getSelectionPath();
			if (path == null)
				return;

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
			if (node == null)
				return;

			Node data = (Node) node.getUserObject();
			if (data != null) {
				DefaultMutableTreeNode target = findTarget(node, data.getClassName());
				removeTreeNode(root, data.getClassName());
				tree.setSelectionPath(new TreePath(target.getPath()));
			}
		}
	};

	private AbstractAction removePackageAction = new AbstractAction("removePackage") {

		private static final long serialVersionUID = -3540063801864849754L;

		@Override
		public void actionPerformed(ActionEvent e) {
			TreePath path = tree.getSelectionPath();
			if (path == null)
				return;

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
			if (node == null)
				return;

			Node data = (Node) node.getUserObject();
			if (data != null) {
				DefaultMutableTreeNode target = findTarget(node, data.getPackageName());
				removeTreeNode(root, data.getPackageName());
				tree.setSelectionPath(new TreePath(target.getPath()));
			}
		}
	};

	private AbstractAction topWindowAction = new AbstractAction("topWindow") {

		private static final long serialVersionUID = 7398038743300735100L;

		@Override
		public void actionPerformed(ActionEvent e) {
			JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
			MainFrame.this.setAlwaysOnTop(item.isSelected());
		}
	};

	public MainFrame() {
		this(null);
	}

	public MainFrame(String path) {
		if (path != null)
			this.path = path;

		initLayout();
		initListener();

		tfFilter.setFont(font);
		tfSelection.setFont(font);

		taDetail.setContentType("text/html");
		taDetail.setEditable(false);

		setSize(800, 600);
		setVisible(true);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void initLayout() {
		renderer = new TreeNodeRenderer(() -> tfFilter.getText());
		tree.setCellRenderer(renderer);
		tree.putClientProperty("JTree.lineStyle", "Horizontal");
		tree.setRowHeight(24);

		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JPanel topBar = new JPanel(new BorderLayout());
		topBar.add(tfFilter);
		topBar.add(btnFile, BorderLayout.EAST);

		panel.add(topBar, BorderLayout.NORTH);

		JSplitPane rootSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(tree), new JScrollPane(taDetail));
		rootSplit.setDividerLocation(1500);
		rootSplit.setOneTouchExpandable(true);
		rootSplit.setDividerSize(10);

		panel.add(rootSplit);
		panel.add(tfSelection, BorderLayout.SOUTH);

		this.add(panel);

		parseFile();
	}

	private void initListener() {
		tfFilter.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				tree.repaint();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				tree.repaint();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				tree.repaint();
			}
		});

		tfFilter.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				int code = e.getKeyCode();
				if (code == KeyEvent.VK_UP || code == KeyEvent.VK_PAGE_UP) {
					JTreeUtil.selectNext(tree, tfFilter.getText(), false);
				} else if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_PAGE_DOWN || code == KeyEvent.VK_ENTER) {
					JTreeUtil.selectNext(tree, tfFilter.getText(), true);
				}
			}
		});

		btnFile.addActionListener(e -> {
			JFileChooser dialog = new JFileChooser(lastPath);
			dialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
			dialog.showOpenDialog(null);
			File file = dialog.getSelectedFile();
			if (file != null) {
				root.removeAllChildren();
				model.reload();
				lastCount = -1;
				parentNode = root;
				this.path = file.getAbsolutePath();
				this.lastPath = path;

				parseFile();
				tree.expandRow(1);
			}
		});

		JPopupMenu popup = new JPopupMenu();
		popup.add(new JMenuItem(removeAction));
		popup.add(new JMenuItem(removeMethodAction));
		popup.add(new JMenuItem(removeClassAction));
		popup.add(new JMenuItem(removePackageAction));
		popup.addSeparator();
		popup.add(new JMenuItem(copyAction));
		popup.add(new JMenuItem(copyMethodAction));
		popup.addSeparator();
		popup.add(new JCheckBoxMenuItem(topWindowAction));

		tree.setComponentPopupMenu(popup);

		tree.addTreeSelectionListener(e -> {
			TreePath path = e.getNewLeadSelectionPath();
			if (path == null)
				return;

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
			if (node == null || node == root)
				return;

			Node data = (Node) node.getUserObject();
			tfSelection.setText(data.getMethod());

			String tagLine = renderer.getTagLine(data);
			taDetail.setText("<font size='5' face='Courier New'>" + tagLine + "</font>");
		});

		tree.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					removeMethodAction.actionPerformed(new ActionEvent(e.getSource(), e.getID(), "deleteMethod"));
				} else if (e.getKeyCode() == KeyEvent.VK_0 && e.getModifiers() == InputEvent.CTRL_MASK) {
					renderer.resetFontSize();
				}

				tree.repaint();
			}
		});

		tree.addMouseWheelListener(new MouseAdapter() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if (e.getModifiers() == InputEvent.CTRL_MASK) {
					int rotation = e.getWheelRotation();
					renderer.updateFontSize(rotation);
				} else {
					tree.getParent().dispatchEvent(e);
				}
			}
		});

		JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.add(new JMenuItem(copyAction));
		tfSelection.setComponentPopupMenu(popupMenu);
		taDetail.setComponentPopupMenu(popupMenu);
	}

	/**
	 * 
	 * @param node
	 * @param text
	 *            为空删除当前节点，否则删除同名方法节点
	 */
	private void removeTreeNode(DefaultMutableTreeNode node, String text) {
		int count = node.getChildCount();
		for (int i = count - 1; i >= 0; i--) {
			removeTreeNode((DefaultMutableTreeNode) node.getChildAt(i), text);
		}

		if (node != root) {
			Node data = (Node) node.getUserObject();
			if (node.getChildCount() == 0 && (text == null || data.getLine().contains(text)))
				deleteNode(node);
		}
	}

	private DefaultMutableTreeNode findTarget(DefaultMutableTreeNode node, String text) {
		DefaultMutableTreeNode target = (DefaultMutableTreeNode) node.getParent();
		if (text != null) {
			DefaultMutableTreeNode next = node.getNextSibling();
			while (next != null) {
				Node data = (Node) next.getUserObject();
				if (!data.getLine().contains(text)) {
					return next;
				}

				next = next.getNextSibling();
			}
		}

		return target;
	}

	private void addNode(MutableTreeNode node, MutableTreeNode parentNode, int index) {
		model.insertNodeInto(node, parentNode, index);
		nodeCount++;
	}

	private void deleteNode(DefaultMutableTreeNode node) {
		model.removeNodeFromParent(node);
		nodeCount--;

		setTitle(title + " - rows: " + nodeCount);
	}

	private void parseFile() {
		new SwingWorker<Object, Object>() {

			@Override
			protected Object doInBackground() throws Exception {
				File file = new File(path);
				if (!file.exists()) {
					System.out.println(path + " not exists");
					return null;
				}

				try {
					List<String> list = Files.readAllLines(Paths.get(file.toURI()));
					title = file.getName() + " - " + list.get(0);

					String line = "";
					for (int i = 2; i < list.size(); i++) {
						String e = list.get(i);
						if (!e.startsWith(spaceChar))
							continue;

						line += e;

						int k = i + 1;
						while (k < list.size()) {
							String nextLine = list.get(k);
							if (nextLine.startsWith(spaceChar))
								break;

							line += nextLine;
							k++;
						}

						addNode(line);
						line = "";
					}
					setTitle(title + " - rows: " + nodeCount);
				} catch (IOException e) {
					e.printStackTrace();
				}

				return null;
			}
		}.execute();
	}

	private void addNode(String line) {
		int count = countSpace(line);
		int index = line.indexOf("->");

		String result = null;
		if (index > 0) { // 结果行，取结果
			result = line.substring(index, line.length());
		} else {
			index = line.length();
		}

		line = line.substring(count, index).replaceAll("> +", ">");
		if (result != null)
			line += result;

		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new Node(line, count));

		if (count < lastCount) {
			parentNode = (DefaultMutableTreeNode) parentNode.getParent();

			if (parentNode.getUserObject() instanceof Node) {
				Node parent = (Node) parentNode.getUserObject();
				lastCount = parent.getCount();
			} else {
				lastCount = 0;
			}
		}

		if (count > lastCount) {
			addNode(node, parentNode, parentNode.getChildCount());
			parentNode = node;
		} else {
			Node data = (Node) parentNode.getUserObject();
			if (count == lastCount && data.isResult(line)) {
				data.setResultLine(line);
			} else {
				TreeNode pp = parentNode.getParent();
				addNode(node, (MutableTreeNode) pp, pp.getChildCount());
				parentNode = node;
			}
		}

		lastCount = count;
	}

	private int countSpace(String line) {
		if (!line.startsWith(spaceChar))
			return -1;

		char char0 = spaceChar.charAt(0);
		for (int i = 0; i < line.length(); i++) {
			if (line.charAt(i) == char0)
				continue;

			return i;
		}

		return 0;
	}
}
