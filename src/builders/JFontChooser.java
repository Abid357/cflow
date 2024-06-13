package builders;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import globals._Settings;

@SuppressWarnings("serial")
public class JFontChooser extends JPanel {

	public JFontChooser() {
		this(UIManager.getFont("Button.font"));
	}

	public JFontChooser(Font initialFont) {
		initLocalData();
		initGuiComponents();
		initListeners();
		setFontValue(initialFont);
	}// public JFontChooser(Font initialFont)

	protected void initLocalData() {

	}

	protected void initGuiComponents() {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		familyCombo = new JComboBox<String>(
				GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
		familyCombo.setSelectedItem(UIManager.getFont("Label.font").getFamily());
		familyCombo.setFont(new Font("Dialog", Font.BOLD, 17));

		sizeCombo = new JComboBox<String>(new String[] { "6", "8", "10", "12", "14", "16", "18", "20", "22", "24", "26",
				"28", "30", "32", "34", "36", "38", "40" });
		sizeCombo.setSelectedItem(new Integer(UIManager.getFont("Label.font").getSize()).toString());
		sizeCombo.setFont(new Font("Dialog", Font.BOLD, 17));

		italicChk = new JCheckBox("<html><i>Italic</i></html>", false);
		italicChk.setFont(new Font("Dialog", Font.PLAIN, 17));
		boldChk = new JCheckBox("<html><b>Bold</b></html>", false);
		boldChk.setFont(new Font("Dialog", Font.PLAIN, 17));
		
		familyCombo.setForeground(Color.DARK_GRAY);
		sizeCombo.setForeground(Color.DARK_GRAY);
		italicChk.setBackground(null);
		italicChk.setForeground(_Settings.labelColor);
		boldChk.setBackground(null);
		boldChk.setForeground(_Settings.labelColor);
		

		JPanel fontBox = new JPanel();
		fontBox.setBackground(_Settings.backgroundColor);
		fontBox.setForeground(_Settings.labelColor);
		fontBox.setLayout(new BoxLayout(fontBox, BoxLayout.X_AXIS));
		fontBox.add(familyCombo);
		fontBox.add(sizeCombo);
		fontBox.setBorder(BorderFactory.createTitledBorder(" Font "));
		add(fontBox);

		JPanel effectsBox = new JPanel();
		effectsBox.setBackground(_Settings.backgroundColor);
		effectsBox.setForeground(_Settings.labelColor);
		effectsBox.setLayout(new BoxLayout(effectsBox, BoxLayout.X_AXIS));
		effectsBox.add(italicChk);
		effectsBox.add(boldChk);
		effectsBox.setBorder(BorderFactory.createTitledBorder(" Effects "));
		add(effectsBox);

		setSampleTextArea(new JTextArea("Type your sample here..."));
		getSampleTextArea().setLineWrap(true);
		getSampleTextArea().setBackground(_Settings.backgroundColor);
		getSampleTextArea().setForeground(_Settings.textFieldColor);
		JPanel samplePanel = new JPanel();
		samplePanel.setBackground(_Settings.backgroundColor);
		samplePanel.setForeground(_Settings.labelColor);
		samplePanel.setLayout(new BoxLayout(samplePanel, BoxLayout.X_AXIS));
		samplePanel.add(new JScrollPane(getSampleTextArea()));
		samplePanel.setBorder(BorderFactory.createTitledBorder(" Sample "));
		add(samplePanel);

		// setMaximumSize(new Dimension(10, -1));
	}// initGuiComponents()

	protected void initListeners() {
		familyCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateFont();
			}
		});

		sizeCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateFont();
			}
		});

		boldChk.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateFont();
			}
		});

		italicChk.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateFont();
			}
		});
	}// initListeners()

	protected void updateFont() {
		Map<TextAttribute, Object> fontAttrs = new HashMap<TextAttribute, Object>();
		fontAttrs.put(TextAttribute.FAMILY, familyCombo.getSelectedItem());
		fontAttrs.put(TextAttribute.SIZE, new Float((String) sizeCombo.getSelectedItem()));

		if (boldChk.isSelected())
			fontAttrs.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
		else
			fontAttrs.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_REGULAR);

		if (italicChk.isSelected())
			fontAttrs.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
		else
			fontAttrs.put(TextAttribute.POSTURE, TextAttribute.POSTURE_REGULAR);

		Font newFont = new Font(fontAttrs);
		Font oldFont = fontValue;
		fontValue = newFont;
		getSampleTextArea().setFont(newFont);
		String text = getSampleTextArea().getText();
		getSampleTextArea().setText("");
		getSampleTextArea().setText(text);
		firePropertyChange("fontValue", oldFont, newFont);
	}// updateFont()

	public void setFontValue(java.awt.Font newfontValue) {
		boldChk.setSelected(newfontValue.isBold());
		italicChk.setSelected(newfontValue.isItalic());
		familyCombo.setSelectedItem(newfontValue.getName());
		sizeCombo.setSelectedItem(Integer.toString(newfontValue.getSize()));
		this.fontValue = newfontValue;
	}

	public java.awt.Font getFontValue() {
		return fontValue;
	}

	public JTextArea getSampleTextArea() {
		return sampleTextArea;
	}

	public void setSampleTextArea(JTextArea sampleTextArea) {
		this.sampleTextArea = sampleTextArea;
	}

	JComboBox<String> familyCombo;
	JCheckBox italicChk;
	JCheckBox boldChk;
	JComboBox<String> sizeCombo;
	private JTextArea sampleTextArea;
	private java.awt.Font fontValue;
}// class JFontChooser extends JPanel
