package com.cc;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;

import javax.swing.UIManager;

import com.cc.tree.MainFrame;

public class TraceTree {

	public static void main(String[] args) {
		String path = getClipboardPath();
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

	/**
	 * 从剪贴板中获取文本（粘贴）
	 */
	public static String getClipboardPath() {
		Transferable trans = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
		if (trans == null)
			return null;

		String text = null;
		if (trans.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			try {
				text = (String) trans.getTransferData(DataFlavor.stringFlavor);
				if (text != null && text.trim().length() > 0) {
					File file = new File(text.trim());
					if (file.exists()) {
						return file.getAbsolutePath();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return null;
	}
}
