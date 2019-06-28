package com.dat3m.ui.options;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import com.dat3m.dartagnan.expression.IConst;
import com.dat3m.dartagnan.parsers.program.ProgramParser;
import com.dat3m.dartagnan.program.Program;
import com.dat3m.dartagnan.program.memory.Configuration;
import com.dat3m.dartagnan.program.memory.Location;
import com.dat3m.dartagnan.program.memory.LocationConfiguration;
import com.dat3m.dartagnan.program.memory.utils.SecurityLevel;
import com.dat3m.ui.editor.Editor;
import com.dat3m.ui.editor.EditorCode;
import com.dat3m.ui.options.utils.ControlCode;

import static com.dat3m.ui.utils.Utils.showError;

public class ConfigurationPane extends JFrame implements ActionListener {

	private Editor programEditor;
	private Configuration confs = new Configuration();
	
	public void setProgramEditor(Editor editor){
		programEditor = editor;
	}

	public Configuration getConfiguration(){
		return confs;
	}

	public void open() {
		SortedSet<Location> locations = mkLocations();
		if(locations != null){
			setTitle("Configuration");
			setMinimumSize(new Dimension(200, 0));
			JPanel panel = new JPanel(new GridLayout(locations.size(), 0));
			for(Location loc : locations){
				LocationConfiguration conf = confs.get(loc);
				LocationConfigurationPane locConfPane = new LocationConfigurationPane(loc, conf, this);
				locConfPane.selector.setSelectedItem(conf.getSecurity());	
				locConfPane.lBoundField.setText(conf.getMinBound().toString());
				locConfPane.uBoundField.setText(conf.getMaxBound().toString());
				panel.add(locConfPane);
			}
			setContentPane(panel);
			pack();
			setVisible(true);
		}
    }

	private class LocationConfigurationPane extends JSplitPane {

	    private JComboBox<SecurityLevel> selector;
	    private BoundField lBoundField;
	    private BoundField uBoundField;
		private Location loc;
		
	    LocationConfigurationPane(Location loc, LocationConfiguration conf, ConfigurationPane parent) {
	    	super(JSplitPane.HORIZONTAL_SPLIT);
	        selector = new JComboBox<SecurityLevel>(EnumSet.allOf(SecurityLevel.class).toArray(new SecurityLevel[0]));
	        selector.addActionListener(parent);
	        lBoundField = new BoundField(false); 
	        lBoundField.addActionListener(parent);
	        uBoundField = new BoundField(false);
	        uBoundField.addActionListener(parent);
	        if(conf != null) {
	        	lBoundField.setText(conf.getMinBound().toString());
	        	uBoundField.setText(conf.getMaxBound().toString());
	        }
	        this.loc = loc;
	        
	        JPanel pane1 = new JPanel();
	        pane1.add(new JLabel(loc.getName() + ": "));
	        
	        JPanel pane2 = new JPanel();
	        pane2.add(selector);
	        pane2.setLayout(new FlowLayout(FlowLayout.RIGHT));
	        pane2.add(lBoundField);
	        pane2.add(uBoundField);
	        
	        this.setDividerSize(0);
	        this.setLeftComponent(pane1);
	        this.setRightComponent(pane2);
	    }
}

    private SortedSet<Location> mkLocations(){
		try {
			if(programEditor == null){
				throw new RuntimeException("Editor is not set in " + getClass().getName());
			}
			Program p = new ProgramParser().parse(programEditor.getEditorPane().getText(), programEditor.getLoadedFormat());
			SortedSet<Location> locations = new TreeSet<>();
			for(Location loc : p.getLocations()){
				locations.add(loc);
			}
			return locations;
		} catch (Exception e){
			String msg = e.getMessage() == null? "Program cannot be parsed" : e.getMessage();
			showError("Locations Configuration requires the program to be correctly parsed.\n\nParsing error: " + msg, "Program error");
			return null;
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals(EditorCode.PROGRAM.editorActionCommand())) {
			confs = new Configuration();
		}
		if(e.getActionCommand().equals(ControlCode.BOUND.actionCommand()) || e.getActionCommand().equals("comboBoxChanged")) {
			LocationConfigurationPane pane = (LocationConfigurationPane)((Component)e.getSource()).getParent().getParent();
			SecurityLevel sec = (SecurityLevel) pane.selector.getSelectedItem();
			IConst lBound = new IConst(Integer.parseInt(pane.lBoundField.getText()));
			IConst uBound = new IConst(Integer.parseInt(pane.uBoundField.getText()));
			LocationConfiguration conf = new LocationConfiguration(sec, lBound, uBound);
			confs.put(pane.loc, conf);
		}
	}
}
