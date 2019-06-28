package com.dat3m.ui.listener;

import static com.dat3m.ui.utils.Utils.showError;

import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import com.dat3m.ui.options.BoundField;

public class BoundListener implements IBoundListener {
	
	private BoundField boundPane;

	public BoundListener(BoundField pane) {
		this.boundPane = pane;
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		runTest();
	}

	@Override
	public void keyReleased(KeyEvent e) {
		runTest();	
	}

	@Override
	public void keyTyped(KeyEvent e) {
		runTest();
	}

	@Override
	public void runTest() {
		String cText = boundPane.getText();
		try {
			Integer.parseInt(cText);
			boundPane.setStableBound(cText);			
		} catch (Exception e) {
			// Empty string is allowed here to allow deleting. It will be handled by focusLost
			if(cText.equals("")) {
				return;
			}
			boundPane.setText(boundPane.getStableBound());
			showError("The bound should be an integer", "Option error");
		}
	}

	@Override
	public void focusGained(FocusEvent arg0) {
		// Nothing t be done here
	}

	@Override
	public void focusLost(FocusEvent arg0) {
		if(boundPane.getText().equals("")) {
			boundPane.setText(boundPane.getStableBound());
			showError("The bound should be an integer", "Option error");
		}
	}
}
