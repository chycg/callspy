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
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
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
import javax.swing.tree.TreePath;

import com.cc.Utils;
import com.cc.graph.Element;
import com.cc.graph.Line;
import com.cc.graph.Painter;
import com.cc.graph.event.DataChangeEvent;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = -1266662931999876034L;

	private String path = "";
	private DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
	private DefaultTreeModel model = new DefaultTreeModel(root);

	private JTextField tfFilter = new JTextField();
	private JButton btnFile = new JButton("New");

	private JTextPane taDetail = new JTextPane();

	private JTextField tfSelection = new JTextField();

	private JTree tree = new JTree(model);
	private DefaultMutableTreeNode parentNode = root;

	private JTabbedPane tabPane = new JTabbedPane();
	private Painter painter = new Painter();

	private int lastCount = -1;

	private Font font = new Font("微软雅黑", Font.PLAIN, 14);

	private TreeNodeRenderer renderer;

	private String lastPath = "d:/";

	private int nodeCount = 0;

	private String title;

	private Map<Integer, DefaultMutableTreeNode> treeMap = new HashMap<>();

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
				Invocation data = (Invocation) node.getUserObject();
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
			Invocation data = (Invocation) node.getUserObject();
			if (data == null)
				return;

			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			Transferable trans = new StringSelection(data.getMethodName());
			clipboard.setContents(trans, null);
		}
	};

	// private AbstractAction removeAction = new AbstractAction("remove") {
	//
	// private static final long serialVersionUID = -5615934028594122494L;
	//
	// @Override
	// public void actionPerformed(ActionEvent e) {
	// TreePath path = tree.getSelectionPath();
	// if (path == null)
	// return;
	//
	// DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
	// if (node == null)
	// return;
	//
	// Invocation data = (Invocation) node.getUserObject();
	// if (data != null) {
	// DefaultMutableTreeNode target = findTarget(node, data.getMethod());
	// removeTreeNode(node, null, MatchType.SINGLE);
	// tree.setSelectionPath(new TreePath(target.getPath()));
	// }
	// }
	// };

	private AbstractAction removeMethodAction = new AbstractAction("removeMethod") {

		private static final long serialVersionUID = -5615934028594122494L;

		@Override
		public void actionPerformed(ActionEvent e) {
			Integer targetId = e.getID();
			if (targetId > 0) {
				doRemove(targetId);
				return;
			}

			TreePath path = tree.getSelectionPath();
			if (path == null)
				return;

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
			if (node == null)
				return;

			Invocation data = (Invocation) node.getUserObject();
			if (node.getChildCount() > 0) { // 删除当前方法以及下级节点，同名方法不处理
				doRemove(data.getId());
				return;
			}

			// 同名简单方法按方法匹配批量删除
			DefaultMutableTreeNode target = findTarget(node, data.getMethod());
			removeTreeNode(root, data.getMethodName(), MatchType.METHOD);
			tree.setSelectionPath(new TreePath(target.getPath()));
		}

		private void doRemove(Integer targetId) {
			DefaultMutableTreeNode node = treeMap.get(targetId);
			if (node == null)
				return;

			AtomicInteger count = new AtomicInteger(1);
			computeNodeSize(node, count);

			model.removeNodeFromParent(node);
			treeMap.remove(targetId);
			nodeCount -= count.get();

			setTitle(title + " - rows: " + nodeCount);
		}

		private void computeNodeSize(DefaultMutableTreeNode parent, AtomicInteger count) {
			if (parent == null)
				return;

			count.addAndGet(parent.getChildCount());
			for (int i = 0; i < parent.getChildCount(); i++) {
				DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(i);
				computeNodeSize(child, count);
			}
		}
	};

	private AbstractAction removeClassAction = new AbstractAction("removeClass") {

		private static final long serialVersionUID = -3540063801864849754L;

		@Override
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			if (source instanceof String) {
				removeTreeNode(root, (String) source, MatchType.CLASS);
				return;
			}

			TreePath path = tree.getSelectionPath();
			if (path == null)
				return;

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
			if (node == null)
				return;

			Invocation data = (Invocation) node.getUserObject();
			if (data != null) {
				DefaultMutableTreeNode target = findTarget(node, data.getClassName());
				removeTreeNode(root, data.getClassName(), MatchType.CLASS);
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

			Invocation data = (Invocation) node.getUserObject();
			if (data != null) {
				DefaultMutableTreeNode target = findTarget(node, data.getPackageName());
				removeTreeNode(root, data.getPackageName(), MatchType.PACKAGE);
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

	private AbstractAction exportTreeAction = new AbstractAction("exportTreeData") {

		private static final long serialVersionUID = 7398038743300735101L;

		@Override
		public void actionPerformed(ActionEvent e) {
			exportTreeData();
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
		panel.add(getCenterPane());
		panel.add(tfSelection, BorderLayout.SOUTH);

		this.add(panel);

		parseFile();
	}

	private JComponent getCenterPane() {
		JSplitPane rootSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tabPane, new JScrollPane(taDetail));
		rootSplit.setDividerLocation(1500);
		rootSplit.setOneTouchExpandable(true);
		rootSplit.setDividerSize(10);

		tabPane.addTab("Tree", new JScrollPane(tree));
		tabPane.addTab("Graph", new JScrollPane(painter));

		return rootSplit;
	}

	public boolean isTreeView() {
		return tabPane.getSelectedIndex() == 0;
	}

	public boolean isGraphView() {
		return tabPane.getSelectedIndex() == 1;
	}

	private void initListener() {
		tfFilter.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				if (isTreeView()) {
					tree.repaint();
				} else {
					painter.highLights(tfFilter.getText());
				}
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				if (isTreeView()) {
					tree.repaint();
				} else {
					painter.highLights(tfFilter.getText());
				}
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				if (isTreeView()) {
					tree.repaint();
				} else {
					painter.highLights(tfFilter.getText());
				}
			}
		});

		tfFilter.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				int code = e.getKeyCode();
				if (code == KeyEvent.VK_UP || code == KeyEvent.VK_PAGE_UP) {
					if (isTreeView()) {
						LocalUtils.selectNextTreeNode(tree, tfFilter.getText(), false);
					} else {
						LocalUtils.selectNextElement(painter, tfFilter.getText(), false);
					}
				} else if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_PAGE_DOWN || code == KeyEvent.VK_ENTER) {
					if (isTreeView()) {
						LocalUtils.selectNextTreeNode(tree, tfFilter.getText(), true);
					} else {
						LocalUtils.selectNextElement(painter, tfFilter.getText(), true);
					}
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
		// popup.add(new JMenuItem(removeAction));
		popup.add(new JMenuItem(removeMethodAction));
		popup.add(new JMenuItem(removeClassAction));
		popup.add(new JMenuItem(removePackageAction));
		popup.addSeparator();
		popup.add(new JMenuItem(copyAction));
		popup.add(new JMenuItem(copyMethodAction));
		popup.addSeparator();
		popup.add(new JCheckBoxMenuItem(topWindowAction));
		popup.addSeparator();
		popup.add(new JMenuItem(exportTreeAction));

		tree.setComponentPopupMenu(popup);

		tree.addTreeSelectionListener(e -> {
			TreePath path = e.getNewLeadSelectionPath();
			if (path == null)
				return;

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
			if (node == null || node == root)
				return;

			Invocation data = (Invocation) node.getUserObject();
			selected(data);
		});

		painter.addSelectionListener(e -> {
			Collection<? extends Element> targets = e.getElements();
			Element target = null;
			if (targets.size() > 0) {
				target = targets.iterator().next();
			}

			if (target == null)
				return;

			if (target.isNode())
				tfSelection.setText(target.getName());
			else if (target.isLine()) {
				Line line = (Line) target;
				Invocation invoke = line.getInvoke();
				selected(invoke);
			}
		});

		tree.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					removeMethodAction.actionPerformed(new ActionEvent(e.getSource(), 0, "deleteMethod"));
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
		tfSelection.setEditable(false);
		taDetail.setComponentPopupMenu(popupMenu);

		painter.addDataChangeListener(e -> {
			int type = e.getEventType();
			Integer targetId = e.getTargetId();
			if (type == DataChangeEvent.REMOVE) {
				Collection<? extends Element> targets = e.getElements();
				for (Element c : targets) {
					if (c.isNode()) {
						ActionEvent event1 = new ActionEvent(c.getName(), 0, "deleteClass");
						removeClassAction.actionPerformed(event1);
					} else if (c.isLine()) {
						ActionEvent event2 = new ActionEvent(c.getName(), targetId, "deleteMethod");
						removeMethodAction.actionPerformed(event2);
					}
				}
			}
		});
	}

	/**
	 * 
	 * @param node
	 * @param text
	 *            为空删除当前节点，否则删除同名方法节点
	 */
	private void removeTreeNode(DefaultMutableTreeNode node, String text, MatchType type) {
		if (node == root) {
			int count = node.getChildCount();
			for (int i = count - 1; i >= 0; i--) {
				removeTreeNode((DefaultMutableTreeNode) node.getChildAt(i), text, type);
			}
			return;
		}

		Invocation data = (Invocation) node.getUserObject();
		if (data.isMatch(text, type)) { // 当前节点匹配删除条件
			int count = node.getChildCount();
			for (int i = count - 1; i >= 0; i--) {
				removeTreeNode((DefaultMutableTreeNode) node.getChildAt(i), null, MatchType.ALL); // 下级都无条件删除
			}

			deleteNode(node);
			tfFilter.setText(null);
		} else {
			int count = node.getChildCount();
			for (int i = count - 1; i >= 0; i--) {
				removeTreeNode((DefaultMutableTreeNode) node.getChildAt(i), text, type);
			}
		}
	}

	private DefaultMutableTreeNode findTarget(DefaultMutableTreeNode node, String text) {
		DefaultMutableTreeNode target = (DefaultMutableTreeNode) node.getParent();
		if (text != null) {
			DefaultMutableTreeNode next = node.getNextSibling();
			while (next != null) {
				Invocation data = (Invocation) next.getUserObject();
				if (!data.getLine().contains(text)) {
					return next;
				}

				next = next.getNextSibling();
			}
		}

		return target;
	}

	private void addNode(DefaultMutableTreeNode node, DefaultMutableTreeNode parentNode, int index) {
		model.insertNodeInto(node, parentNode, index);
		nodeCount++;

		Invocation invoke = (Invocation) node.getUserObject();
		Invocation source = parentNode == root ? invoke : (Invocation) parentNode.getUserObject();
		String srcClzName = source.getClassName();
		painter.addLine(srcClzName, invoke);

		treeMap.put(invoke.getId(), node);
	}

	private void deleteNode(DefaultMutableTreeNode node) {
		model.removeNodeFromParent(node);
		nodeCount--;

		if (node.getUserObject() instanceof Invocation) {
			Invocation invoke = (Invocation) node.getUserObject();
			treeMap.remove(invoke.getId());

			painter.removeLine(invoke);
		}

		setTitle(title + " - rows: " + nodeCount);
	}

	private void selected(Invocation data) {
		if (data == null)
			return;

		tfSelection.setText(data.getMethod());
		String tagLine = renderer.getTagLine(data);
		taDetail.setText("<font size='5' face='Courier New'>" + tagLine + "</font>");
	}

	private void parseFile() {
		new SwingWorker<Object, Object>() {

			@Override
			protected Object doInBackground() {
				File file = new File(path);
				if (!file.exists()) {
					System.out.println(path + " not exists");
					return null;
				}

				try {
					List<String> list = Files.readAllLines(Paths.get(file.toURI()));
					title = file.getName() + " - " + list.get(0);

					String line = "";
					int row = 1;
					for (int i = 2; i < list.size(); i++) {
						String e = list.get(i);
						if (!isPrefix(e))
							continue;

						line += e;

						int k = i + 1;
						while (k < list.size()) {
							String nextLine = list.get(k);
							if (isPrefix(nextLine))
								break;

							line += nextLine;
							k++;
						}

						addNode(line, row++);
						line = "";
					}
					setTitle(title + " - rows: " + nodeCount);

					// painter.init(root);
				} catch (IOException e) {
					e.printStackTrace();
				}

				return null;
			}
		}.execute();
	}

	private void exportTreeData() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		fileChooser.setDialogTitle("save tree data");
		fileChooser.showDialog(this, "确定");

		File file = fileChooser.getSelectedFile();
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}

		List<String> list = new ArrayList<>();
		list.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		list.add("--------------------");
		logs(root, list);

		try (FileWriter fw = new FileWriter(file)) {
			for (String line : list) {
				fw.append(line + "\n");
			}

			fw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void logs(DefaultMutableTreeNode parent, List<String> list) {
		if (parent == null)
			return;

		for (int i = 0; i < parent.getChildCount(); i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(i);
			Invocation invoke = (Invocation) child.getUserObject();
			int count = invoke.getCount();
			Mod m = Mod.getByModifier(invoke.getMod());
			list.add(m.getLogPrefix(count) + invoke.getRawLine());

			logs(child, list);
		}
	}

	private void addNode(String line, int row) {
		int count = countSpace(line);
		int index = line.indexOf("->");
		int mod = Mod.getModByChar(line.charAt(0)).getCode();

		String result = null;
		if (index > 0) { // 结果行，取结果
			result = line.substring(index, line.length());
		} else {
			index = line.length();
		}

		String newLine = line.substring(count, index).replaceAll("> +", ">");
		if (result != null)
			newLine += result;

		Invocation invoke = new Invocation(line, newLine, count, mod);
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(invoke);

		if (count < lastCount) {
			parentNode = (DefaultMutableTreeNode) parentNode.getParent();

			if (parentNode.getUserObject() instanceof Invocation) {
				Invocation parent = (Invocation) parentNode.getUserObject();
				lastCount = parent.getCount();
			} else {
				lastCount = 0;
			}
		}

		if (count > lastCount) {
			addNode(node, parentNode, parentNode.getChildCount());
			parentNode = node;
		} else {
			Invocation data = (Invocation) parentNode.getUserObject();
			if (count == lastCount && data.isResult(line)) { // 同级别的退出记录，作为结果计入当前节点
				data.setResultLine(line);
			} else {
				DefaultMutableTreeNode pp = (DefaultMutableTreeNode) parentNode.getParent();
				addNode(node, pp, pp.getChildCount());
				parentNode = node;
			}
		}

		lastCount = count;
	}

	private int countSpace(String line) {
		if (!isPrefix(line))
			return -1;

		char char0 = line.charAt(0);
		for (int i = 0; i < line.length(); i++) {
			if (line.charAt(i) == char0)
				continue;

			return i;
		}

		return 0;
	}

	private boolean isPrefix(String line) {
		return Utils.isNotEmpty(line) && Mod.isModSign(line.charAt(0));
	}
}
