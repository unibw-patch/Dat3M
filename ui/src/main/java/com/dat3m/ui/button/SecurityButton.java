package com.dat3m.ui.button;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import com.dat3m.ui.options.ConfigurationPane;
import com.dat3m.ui.options.OptionsPane;
import com.dat3m.ui.options.utils.ControlCode;

public class SecurityButton extends JButton implements ActionListener {

	private final ConfigurationPane confPane;
	
	public SecurityButton(ConfigurationPane confPane) {
        super("Configure Locations");
        this.confPane = confPane;
        setActionCommand(ControlCode.CONF.actionCommand());
		setMaximumSize(new Dimension(OptionsPane.OPTWIDTH, 50));
		addActionListener(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if(ControlCode.CONF.actionCommand().equals(command)){
			confPane.open();
		}
	}
}
