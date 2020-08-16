package com.cc.graph.event;

import java.util.EventListener;

public interface SelectionListener extends EventListener {

	void selectionChanged(SelectionEvent e);

}
