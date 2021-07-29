package com.github.javlock.sieve.operator.gui;

import java.awt.BorderLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

public class OperatorGuiMain extends JFrame {

	private static final long serialVersionUID = 767184369252800226L;
	private JTextField tfLevel;
	private JTextField tfLevelCurrent;
	private JTextField tfRegEx;

	public OperatorGuiMain() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, BorderLayout.CENTER);

		JList list = new JList();
		scrollPane.setViewportView(list);

		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout(0, 0));

		JButton btnTmp = new JButton("|");
		btnTmp.setEnabled(false);
		panel.add(btnTmp, BorderLayout.EAST);

		JPanel panel_ctl = new JPanel();
		panel.add(panel_ctl, BorderLayout.CENTER);
		SpringLayout sl_panel_ctl = new SpringLayout();
		panel_ctl.setLayout(sl_panel_ctl);

		tfLevel = new JTextField();
		sl_panel_ctl.putConstraint(SpringLayout.WEST, tfLevel, 0, SpringLayout.WEST, panel_ctl);
		sl_panel_ctl.putConstraint(SpringLayout.SOUTH, tfLevel, 0, SpringLayout.SOUTH, panel_ctl);
		sl_panel_ctl.putConstraint(SpringLayout.EAST, tfLevel, 60, SpringLayout.WEST, panel_ctl);
		tfLevel.setHorizontalAlignment(SwingConstants.CENTER);
		tfLevel.setText("0");
		sl_panel_ctl.putConstraint(SpringLayout.NORTH, tfLevel, 0, SpringLayout.NORTH, panel_ctl);
		panel_ctl.add(tfLevel);
		tfLevel.setColumns(10);

		JButton btnChangeLevel = new JButton("Выбор");
		sl_panel_ctl.putConstraint(SpringLayout.NORTH, btnChangeLevel, 0, SpringLayout.NORTH, tfLevel);
		panel_ctl.add(btnChangeLevel);

		tfLevelCurrent = new JTextField();
		sl_panel_ctl.putConstraint(SpringLayout.EAST, tfLevelCurrent, 60, SpringLayout.EAST, tfLevel);
		tfLevelCurrent.setText("0");
		tfLevelCurrent.setHorizontalAlignment(SwingConstants.CENTER);
		tfLevelCurrent.setEditable(false);
		sl_panel_ctl.putConstraint(SpringLayout.WEST, btnChangeLevel, 0, SpringLayout.EAST, tfLevelCurrent);
		sl_panel_ctl.putConstraint(SpringLayout.NORTH, tfLevelCurrent, 0, SpringLayout.NORTH, panel_ctl);
		sl_panel_ctl.putConstraint(SpringLayout.WEST, tfLevelCurrent, 0, SpringLayout.EAST, tfLevel);
		sl_panel_ctl.putConstraint(SpringLayout.SOUTH, tfLevelCurrent, 0, SpringLayout.SOUTH, panel_ctl);
		panel_ctl.add(tfLevelCurrent);
		tfLevelCurrent.setColumns(10);

		tfRegEx = new JTextField();
		tfRegEx.setHorizontalAlignment(SwingConstants.CENTER);
		sl_panel_ctl.putConstraint(SpringLayout.NORTH, tfRegEx, 0, SpringLayout.NORTH, tfLevel);
		sl_panel_ctl.putConstraint(SpringLayout.WEST, tfRegEx, 0, SpringLayout.EAST, btnChangeLevel);
		sl_panel_ctl.putConstraint(SpringLayout.SOUTH, tfRegEx, 0, SpringLayout.SOUTH, panel_ctl);
		sl_panel_ctl.putConstraint(SpringLayout.EAST, tfRegEx, 280, SpringLayout.EAST, btnChangeLevel);
		panel_ctl.add(tfRegEx);
		tfRegEx.setColumns(10);

		JButton button = new JButton("Выбрать как фильтр");
		sl_panel_ctl.putConstraint(SpringLayout.WEST, button, 0, SpringLayout.EAST, tfRegEx);
		sl_panel_ctl.putConstraint(SpringLayout.SOUTH, button, 0, SpringLayout.SOUTH, tfLevel);
		panel_ctl.add(button);

		//
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();
		setSize(width, height);
	}
}
