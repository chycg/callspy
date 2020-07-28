package com.cc.tree;

import javax.swing.UIManager;

import com.cc.Utils;

public class TraceTree {

	public static void main(String[] args) {
		String path = null;
		if (Utils.isNotEmpty(args)) {
			path = args[0];
		}

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		new MainFrame(path);
	}

}
