package com.zeroturnaround.callspy;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = -1266662931999876034L;

	private JTree tree = new JTree();

	public MainFrame() {

		initLayout();

		setTitle("trace");
		setSize(800, 600);
		setVisible(true);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void initLayout() {
		this.add(new JScrollPane(tree));
	}

	public void parseFile(String path) {

	}

}
