package com.zeroturnaround.callspy.tree;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
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
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.zeroturnaround.callspy.Utils;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = -1266662931999876034L;

	private String path = "D:\\Git\\callspy\\src\\main\\java\\com\\zeroturnaround\\callspy\\user.log.1";
	private final char spaceChar = '~';

	private DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
	private DefaultTreeModel model = new DefaultTreeModel(root);

	private JTextField tfFilter;
	private JButton btnFile = new JButton("New");

	private JTextPane taDetail = new JTextPane();

	private JTextField tfSelection = new JTextField();

	private JTree tree = new JTree(model);
	private DefaultMutableTreeNode parentNode = root;

	private int lastCount = -1;

	private Set<String> set = new HashSet<>();

	private Font font = new Font("微软雅黑", Font.PLAIN, 14);

	private String lastPath;

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

			model.removeNodeFromParent(node);
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
				removeTreeNode(root, data.getMethod());
				set.add(data.getMethod().replace('/', '.'));
				// taExclude.setText(Utils.toString(set));
			}

			if (node.getParent() != null && node.getChildCount() == 0)
				model.removeNodeFromParent(node);
		}
	};

	private AbstractAction printExcludeAction = new AbstractAction("printExclude") {

		private static final long serialVersionUID = -2683251181036247062L;

		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println(Utils.toString(set));
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
		taDetail.setFont(font);

		setTitle("trace");
		setSize(800, 600);
		setVisible(true);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void initLayout() {
		JTreeUtil.setTreeExpandedState(tree, true);
		TreeFilterDecorator filterDecorator = TreeFilterDecorator.decorate(tree, createUserObjectMatcher());
		tfFilter = filterDecorator.getFilterField();
		tree.setCellRenderer(new TreeNodeRenderer(() -> tfFilter.getText()));

		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JPanel topBar = new JPanel(new BorderLayout());
		topBar.add(tfFilter);
		topBar.add(btnFile, BorderLayout.EAST);

		panel.add(topBar, BorderLayout.NORTH);
		taDetail.setEditable(false);

		JSplitPane rootSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(tree), new JScrollPane(taDetail));
		rootSplit.setDividerLocation(1500);
		rootSplit.setOneTouchExpandable(true);
		rootSplit.setDividerSize(10);

		panel.add(rootSplit);
		panel.add(tfSelection, BorderLayout.SOUTH);

		this.add(panel);

		parseFile();

		tree.putClientProperty("JTree.lineStyle", "Horizontal");
		tree.setRowHeight(24);

		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}

		// tree.setRootVisible(false);
	}

	private BiPredicate<Object, String> createUserObjectMatcher() {
		return (userObject, textToFilter) -> {
			if (userObject instanceof Node) {
				Node project = (Node) userObject;
				return project.getLine().toLowerCase().contains(textToFilter);
			}

			return userObject.toString().toLowerCase().contains(textToFilter);
		};
	}

	private void initListener() {
		// taExclude.addMouseListener(new MouseAdapter() {
		// @Override
		// public void mouseClicked(MouseEvent e) {
		// if (e.getClickCount() > 1)
		// taExclude.selectAll();
		// }
		// });

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
		popup.addSeparator();
		popup.add(new JMenuItem(copyAction));
		popup.add(new JMenuItem(copyMethodAction));
		popup.addSeparator();
		popup.add(new JMenuItem(printExcludeAction));

		tree.setComponentPopupMenu(popup);

		tree.addTreeSelectionListener(e -> {
			TreePath path = e.getNewLeadSelectionPath();
			if (path == null)
				return;

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
			if (node == null || node == root)
				return;

			Node data = (Node) node.getUserObject();
			tfSelection.setText(data.getCallName());
			taDetail.setText(data.getLine());
		});

		tree.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					removeMethodAction.actionPerformed(new ActionEvent(e.getSource(), e.getID(), "deleteMethod"));
				}
			}
		});

		JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.add(new JMenuItem(copyAction));
		tfSelection.setComponentPopupMenu(popupMenu);
	}

	private void removeTreeNode(DefaultMutableTreeNode node, String text) {
		if (node.getUserObject().getClass() == Node.class) {
			Node data = (Node) node.getUserObject();
			if (data != null && data.getLine().contains(text) && node.getChildCount() == 0) {
				model.removeNodeFromParent(node);
			}
		}

		int count = node.getChildCount();
		for (int i = count - 1; i >= 0; i--) {
			removeTreeNode((DefaultMutableTreeNode) node.getChildAt(i), text);
		}
	}

	private void parseFile() {

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
			lastCount = ((Node) parentNode.getUserObject()).getCount();
		}

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
		if (!line.startsWith(String.valueOf(spaceChar)))
			return -1;

		for (int i = 0; i < line.length(); i++) {
			if (line.charAt(i) == spaceChar)
				continue;

			return i;
		}

		return 0;
	}
}
