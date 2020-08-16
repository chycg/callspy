package com.cc.graph.event;

import java.util.EventListener;

public interface DataChangeListener extends EventListener {

	void dataChanged(DataChangeEvent e);

}
