package com.demo.fx;

import com.cc.tree.Invocation;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MainFrame2 extends Application {

	private TreeItem<Invocation> rootNode = new TreeItem<>();

	private TextField tfFilter = new TextField();

	private Button btnFile = new Button("New");

	private TextArea taDetail = new TextArea();

	private TextField tfSelection = new TextField();

	private TreeView<Invocation> tree = new TreeView<>(rootNode);

	@Override
	public void start(Stage stage) {
		rootNode.setExpanded(true);

		stage.setTitle("Tree View Sample");
		VBox box = new VBox();
		final Scene scene = new Scene(box, 400, 300);
		scene.setFill(Color.LIGHTGRAY);

		tree.setEditable(false);
		tree.setCellFactory((TreeView<Invocation> p) -> new TreeCellImpl());

		box.getChildren().add(tree);
		stage.setScene(scene);
		stage.show();
	}

	private final class TreeCellImpl extends TreeCell<Invocation> {

		private final ContextMenu addMenu = new ContextMenu();

		public TreeCellImpl() {
			MenuItem miRemove = new MenuItem("remove");
			addMenu.getItems().add(miRemove);
			miRemove.setOnAction((ActionEvent t) -> {
				Object o = t.getSource();
				getTreeItem().getChildren().remove(o);
			});
		}
	}

	public static void main(String[] args) {
		Application.launch(args);
	}
}