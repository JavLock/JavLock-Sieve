package com.github.javlock.sieve.operator.gui;

import java.awt.BorderLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.github.javlock.sieve.PdfFile;
import com.github.javlock.sieve.operator.SieveOperator;
import com.google.common.hash.Hashing;

import lombok.Getter;
import lombok.Setter;

public class OperatorGuiMain extends JFrame {

	private static final long serialVersionUID = 767184369252800226L;
	private JTextField tfLevel;
	private JTextField tfLevelCurrent;
	private JTextField tfRegEx;

	private transient @Getter @Setter SieveOperator operator;
	private JFileChooser fileChooser = new JFileChooser();

	public OperatorGuiMain() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();
		setSize(width, height);

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

		JPanel panelCtl = new JPanel();
		panel.add(panelCtl, BorderLayout.CENTER);
		SpringLayout slPanelCtl = new SpringLayout();
		panelCtl.setLayout(slPanelCtl);

		tfLevel = new JTextField();
		slPanelCtl.putConstraint(SpringLayout.WEST, tfLevel, 0, SpringLayout.WEST, panelCtl);
		slPanelCtl.putConstraint(SpringLayout.SOUTH, tfLevel, 0, SpringLayout.SOUTH, panelCtl);
		slPanelCtl.putConstraint(SpringLayout.EAST, tfLevel, 60, SpringLayout.WEST, panelCtl);
		tfLevel.setHorizontalAlignment(SwingConstants.CENTER);
		tfLevel.setText("0");
		slPanelCtl.putConstraint(SpringLayout.NORTH, tfLevel, 0, SpringLayout.NORTH, panelCtl);
		panelCtl.add(tfLevel);
		tfLevel.setColumns(10);

		JButton btnChangeLevel = new JButton("Выбор");
		btnChangeLevel.addActionListener(e -> changeLevel());
		slPanelCtl.putConstraint(SpringLayout.NORTH, btnChangeLevel, 0, SpringLayout.NORTH, tfLevel);
		panelCtl.add(btnChangeLevel);

		tfLevelCurrent = new JTextField();
		slPanelCtl.putConstraint(SpringLayout.EAST, tfLevelCurrent, 60, SpringLayout.EAST, tfLevel);
		tfLevelCurrent.setText("0");
		tfLevelCurrent.setHorizontalAlignment(SwingConstants.CENTER);
		tfLevelCurrent.setEditable(false);
		slPanelCtl.putConstraint(SpringLayout.WEST, btnChangeLevel, 0, SpringLayout.EAST, tfLevelCurrent);
		slPanelCtl.putConstraint(SpringLayout.NORTH, tfLevelCurrent, 0, SpringLayout.NORTH, panelCtl);
		slPanelCtl.putConstraint(SpringLayout.WEST, tfLevelCurrent, 0, SpringLayout.EAST, tfLevel);
		slPanelCtl.putConstraint(SpringLayout.SOUTH, tfLevelCurrent, 0, SpringLayout.SOUTH, panelCtl);
		panelCtl.add(tfLevelCurrent);
		tfLevelCurrent.setColumns(10);

		tfRegEx = new JTextField();
		tfRegEx.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				regEx();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				regEx();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				regEx();
			}

			private void regEx() {
				String regEx = tfRegEx.getText();
				// TODO REGEX
			}
		});
		tfRegEx.setHorizontalAlignment(SwingConstants.CENTER);
		slPanelCtl.putConstraint(SpringLayout.NORTH, tfRegEx, 0, SpringLayout.NORTH, tfLevel);
		slPanelCtl.putConstraint(SpringLayout.WEST, tfRegEx, 0, SpringLayout.EAST, btnChangeLevel);
		slPanelCtl.putConstraint(SpringLayout.SOUTH, tfRegEx, 0, SpringLayout.SOUTH, panelCtl);
		slPanelCtl.putConstraint(SpringLayout.EAST, tfRegEx, 280, SpringLayout.EAST, btnChangeLevel);
		panelCtl.add(tfRegEx);
		tfRegEx.setColumns(10);

		JButton button = new JButton("Выбрать как фильтр");
		slPanelCtl.putConstraint(SpringLayout.WEST, button, 0, SpringLayout.EAST, tfRegEx);
		slPanelCtl.putConstraint(SpringLayout.SOUTH, button, 0, SpringLayout.SOUTH, tfLevel);
		panelCtl.add(button);

		JButton btnSendFile = new JButton("Отправить файл");
		btnSendFile.addActionListener(e -> {
			try {
				sendFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
		slPanelCtl.putConstraint(SpringLayout.WEST, btnSendFile, 68, SpringLayout.EAST, button);
		slPanelCtl.putConstraint(SpringLayout.SOUTH, btnSendFile, 0, SpringLayout.SOUTH, tfLevel);
		panelCtl.add(btnSendFile);

		fileChooser.setDialogTitle("Выбор директории/файла");

		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	}

	private void sendFile() throws IOException, InterruptedException {
		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();

			recursiveSend(selectedFile);

		}
	}

	private void recursiveSend(File file) throws IOException, InterruptedException {
		if (file.isDirectory()) {

			System.err.println("file:" + file.getAbsolutePath() + " is dir");
			File[] arF = file.listFiles();
			for (File f : arF) {
				recursiveSend(f);
			}
		} else {
			System.err.println("file:" + file.getAbsolutePath() + " is file");
			byte[] data = Files.readAllBytes(file.toPath());
			String hash = Hashing.sha256().hashBytes(data).toString();

			PdfFile pdfFile = new PdfFile();
			pdfFile.setData(data);
			pdfFile.setId(hash);
			operator.getChannelFuture().channel().writeAndFlush(pdfFile);
		}
	}

	private void changeLevel() {
		try {
			int levelFromTF = Integer.parseInt(tfLevel.getText());
			operator.currentLevel = levelFromTF;
			tfLevelCurrent.setText(Integer.toString(levelFromTF));
		} catch (Exception e) {
			tfLevel.setText("ЦЕЛОЕ!");
			e.printStackTrace();
		}
	}

}
