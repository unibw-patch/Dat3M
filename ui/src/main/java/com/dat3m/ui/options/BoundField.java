package com.dat3m.ui.options;

import static com.dat3m.ui.options.utils.ControlCode.BOUND;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JTextField;

import com.dat3m.ui.listener.PositiveBoundListener;
import com.dat3m.ui.listener.BoundListener;
import com.dat3m.ui.listener.IBoundListener;

public class BoundField extends JTextField {

	private String stableBound;
	
    private Set<ActionListener> actionListeners = new HashSet<>();

	public BoundField(boolean positive) {
		this.stableBound = "1";
		this.setColumns(3);
		this.setText(stableBound);

		IBoundListener listener = positive ? new PositiveBoundListener(this) : new BoundListener(this); 

		this.addKeyListener(listener);
		this.addFocusListener(listener);
	}
	
    public void addActionListener(ActionListener actionListener){
        actionListeners.add(actionListener);
    }

	public void setStableBound(String bound) {
		this.stableBound = bound;
		// Listeners are notified when a new stable bound is set
        ActionEvent boundChanged = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, BOUND.actionCommand());
        for(ActionListener actionListener : actionListeners){
            actionListener.actionPerformed(boundChanged);
        }
	}

	public String getStableBound() {
		return stableBound;
	}
}
