package frames;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import builders.Background;
import builders.InteractivePanel;
import builders.JFontChooser;
import builders.MyTextField;
import globals._Settings;
import net.miginfocom.swing.MigLayout;

public class Builder extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtX;
	private JTextField txtY;
	private JTextField txtX2;
	private JTextField txtY2;
	private JTextField txtWidth;
	private JTextField txtHeight;
	private databases.Forms db;
	private Main mainFrame;

	/**
	 * Create the frame.
	 */
	public Builder(Background background, databases.Forms formDB, Main frame) {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		contentPane = new JPanel();
		setContentPane(contentPane);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setTitle("Builder");
		getContentPane().setLayout(new MigLayout("", "[75%][10][grow][150][10][150][80]",
				"[50][10][200][10][200][10][200][10][50][10][70][60][70][10][60][grow]"));
		contentPane.setBackground(_Settings.backgroundColor);

		db = formDB;
		mainFrame = frame;

		JTextField txtTitle = new JTextField();
		txtTitle.setHorizontalAlignment(SwingConstants.CENTER);
		txtTitle.setBorder(new TitledBorder(null, " Title ", TitledBorder.LEFT, TitledBorder.TOP, null, _Settings.labelColor));
		txtTitle.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtTitle.setBackground(null);
		txtTitle.setForeground(_Settings.textFieldColor);
		txtTitle.setColumns(10);
		getContentPane().add(txtTitle, "cell 2 0 5 1,grow");

		JPanel pStats = new JPanel();
		pStats.setBorder(new TitledBorder(null, " Stats ", TitledBorder.LEFT, TitledBorder.TOP, null, _Settings.labelColor));
		getContentPane().add(pStats, "cell 2 2 5 1,grow");
		pStats.setLayout(new MigLayout("", "[17.5%][17.5%][17.5%][17.5%][17.5%][17.5%]", "[33%][33%][33%]"));
		pStats.setBackground(_Settings.backgroundColor);
		pStats.setForeground(_Settings.labelColor);

		JLabel lblStartPoint = new JLabel("Start Point");
		lblStartPoint.setHorizontalAlignment(SwingConstants.CENTER);
		lblStartPoint.setForeground(_Settings.labelColor);
		lblStartPoint.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblStartPoint.setBorder(null);
		pStats.add(lblStartPoint, "cell 0 0 2 1,grow");

		JLabel lblEndPoint = new JLabel("End Point");
		lblEndPoint.setHorizontalAlignment(SwingConstants.CENTER);
		lblEndPoint.setForeground(_Settings.labelColor);
		lblEndPoint.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblEndPoint.setBorder(null);
		pStats.add(lblEndPoint, "cell 2 0 2 1,grow");

		JLabel lblX = new JLabel("X:");
		lblX.setHorizontalAlignment(SwingConstants.CENTER);
		lblX.setForeground(_Settings.labelColor);
		lblX.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblX.setBorder(null);
		pStats.add(lblX, "cell 0 1,grow");

		txtX = new JTextField();
		txtX.setHorizontalAlignment(SwingConstants.CENTER);
		txtX.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtX.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtX.setBackground(null);
		txtX.setForeground(_Settings.textFieldColor);
		txtX.setColumns(10);
		pStats.add(txtX, "cell 1 1,grow");

		JLabel lblX2 = new JLabel("X:");
		lblX2.setHorizontalAlignment(SwingConstants.CENTER);
		lblX2.setForeground(_Settings.labelColor);
		lblX2.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblX2.setBorder(null);
		pStats.add(lblX2, "cell 2 1,grow");

		txtX2 = new JTextField();
		txtX2.setHorizontalAlignment(SwingConstants.CENTER);
		txtX2.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtX2.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtX2.setBackground(null);
		txtX2.setForeground(_Settings.textFieldColor);
		txtX2.setColumns(10);
		pStats.add(txtX2, "cell 3 1,grow");

		JLabel lblWidth = new JLabel("Width:");
		lblWidth.setHorizontalAlignment(SwingConstants.CENTER);
		lblWidth.setForeground(_Settings.labelColor);
		lblWidth.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblWidth.setBorder(null);
		pStats.add(lblWidth, "cell 4 1,alignx trailing,growy");

		txtWidth = new JTextField();
		txtWidth.setEditable(false);
		txtWidth.setHorizontalAlignment(SwingConstants.CENTER);
		txtWidth.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtWidth.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtWidth.setBackground(null);
		txtWidth.setForeground(_Settings.textFieldColor);
		txtWidth.setColumns(10);
		pStats.add(txtWidth, "cell 5 1,grow");

		JLabel lblHeight = new JLabel("Height:");
		lblHeight.setHorizontalAlignment(SwingConstants.CENTER);
		lblHeight.setForeground(_Settings.labelColor);
		lblHeight.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblHeight.setBorder(null);
		pStats.add(lblHeight, "cell 4 2,alignx trailing,growy");

		txtHeight = new JTextField();
		txtHeight.setEditable(false);
		txtHeight.setHorizontalAlignment(SwingConstants.CENTER);
		txtHeight.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtHeight.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtHeight.setBackground(null);
		txtHeight.setForeground(_Settings.textFieldColor);
		txtHeight.setColumns(10);
		pStats.add(txtHeight, "cell 5 2,grow");

		JLabel lblY = new JLabel("Y:");
		lblY.setHorizontalAlignment(SwingConstants.CENTER);
		lblY.setForeground(_Settings.labelColor);
		lblY.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblY.setBorder(null);
		pStats.add(lblY, "cell 0 2,grow");

		txtY = new JTextField();
		txtY.setHorizontalAlignment(SwingConstants.CENTER);
		txtY.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtY.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtY.setBackground(null);
		txtY.setForeground(_Settings.textFieldColor);
		txtY.setColumns(10);
		pStats.add(txtY, "cell 1 2,grow");

		JLabel lblY2 = new JLabel("Y:");
		lblY2.setHorizontalAlignment(SwingConstants.CENTER);
		lblY2.setForeground(_Settings.labelColor);
		lblY2.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblY2.setBorder(null);
		pStats.add(lblY2, "cell 2 2,grow");

		txtY2 = new JTextField();
		txtY2.setHorizontalAlignment(SwingConstants.CENTER);
		txtY2.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtY2.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtY2.setBackground(null);
		txtY2.setForeground(_Settings.textFieldColor);
		txtY2.setColumns(10);
		pStats.add(txtY2, "cell 3 2,grow");

		JFontChooser fcFont = new JFontChooser();
		fcFont.setFontValue(new Font("Calibri", Font.PLAIN, 20));
		getContentPane().add(fcFont, "cell 2 4 5 1,grow");

		JColorChooser ccFont = new JColorChooser(_Settings.textFieldColor);
		ccFont.setBackground(_Settings.backgroundColor);
		ccFont.setPreviewPanel(new JPanel());
		ccFont.setBorder(new TitledBorder(null, " Color ", TitledBorder.LEFT, TitledBorder.TOP, null, _Settings.labelColor));
		for (AbstractColorChooserPanel panel : ccFont.getChooserPanels())
			if (!panel.getDisplayName().equals("HSV"))
				ccFont.removeChooserPanel(panel);
			else
				panel.setBackground(_Settings.backgroundColor);
		ccFont.getSelectionModel().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				fcFont.getSampleTextArea().setForeground(ccFont.getColor());
			}
		});
		getContentPane().add(ccFont, "cell 2 6 5 1,grow");

		DefaultListModel<JComponent> model = new DefaultListModel<JComponent>();
		JList<JComponent> list = new JList<JComponent>(model);
		list.setBackground(_Settings.backgroundColor);
		list.setForeground(_Settings.textFieldColor);
		JScrollPane spItems = new JScrollPane(list);
		spItems.setBackground(_Settings.backgroundColor);
		spItems.setForeground(_Settings.textFieldColor);
		spItems.setBorder(new TitledBorder(null, " Items ", TitledBorder.LEFT, TitledBorder.TOP, null, _Settings.labelColor));
		list.setFont(new Font("Century Gothic", Font.BOLD, 17));
		getContentPane().add(spItems, "cell 2 8 4 3,grow");

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		JScrollPane spBackground = new JScrollPane(background);
		spBackground.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		spBackground.setBounds(0, 0, (int) (0.5 * screenSize.getWidth()), (int) (0.9 * screenSize.getHeight()));

		InteractivePanel glass = new InteractivePanel();
		glass.bindTextFields(txtX, txtY, txtX2, txtY2, txtWidth, txtHeight);
		glass.bindScrollPane(spBackground);
		glass.bindList(list);
		glass.setBounds(0, 0, spBackground.getViewportBorderBounds().width - 15,
				spBackground.getViewportBorderBounds().height - 15);

		JLayeredPane lp = new JLayeredPane();
		lp.add(spBackground, Integer.valueOf(1));
		lp.add(glass, Integer.valueOf(2));
		getContentPane().add(lp, "cell 0 0 1 16,grow");

		ButtonGroup bg = new ButtonGroup();

		JRadioButton rdbtnTextField = new JRadioButton("Text Field");
		rdbtnTextField.setHorizontalAlignment(SwingConstants.CENTER);
		rdbtnTextField.setSelected(true);
		rdbtnTextField.setForeground(_Settings.labelColor);
		rdbtnTextField.setBackground(null);
		rdbtnTextField.setFont(new Font("Arial Black", Font.PLAIN, 17));
		rdbtnTextField.setBorder(null);
		bg.add(rdbtnTextField);

		JRadioButton rdbtnDropdownMenu = new JRadioButton("Drop-down Menu");
		rdbtnDropdownMenu.setHorizontalAlignment(SwingConstants.CENTER);
		rdbtnDropdownMenu.setForeground(_Settings.labelColor);
		rdbtnDropdownMenu.setBackground(null);
		rdbtnDropdownMenu.setFont(new Font("Arial Black", Font.PLAIN, 17));
		rdbtnDropdownMenu.setBorder(null);
		bg.add(rdbtnDropdownMenu);

		JPanel pRadioButtons = new JPanel();
		pRadioButtons.setLayout(new MigLayout("", "[grow]", "[50%][50%]"));
		pRadioButtons.setBorder(new TitledBorder(null, " Type ", TitledBorder.LEFT, TitledBorder.TOP, null, _Settings.labelColor));
		pRadioButtons.add(rdbtnTextField, "cell 0 0,grow");
		pRadioButtons.add(rdbtnDropdownMenu, "cell 0 1,grow");
		pRadioButtons.setBackground(_Settings.backgroundColor);
		pRadioButtons.setForeground(_Settings.labelColor);
		getContentPane().add(pRadioButtons, "cell 2 6 4 1,grow");

		JButton btnAdd = new JButton("ADD");
		btnAdd.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnAdd.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnAdd.setBackground(_Settings.backgroundColor);
		btnAdd.setForeground(_Settings.labelColor);
		btnAdd.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				int offsetY = spBackground.getVerticalScrollBar().getValue();
				int offsetX = spBackground.getHorizontalScrollBar().getValue();
				Rectangle selection = glass.getSelection();
				Rectangle drawing = new Rectangle(selection.x + offsetX, selection.y + offsetY, selection.width,
						selection.height);
				if (rdbtnTextField.isSelected())
					model.addElement(new MyTextField(drawing, fcFont.getFontValue(), ccFont.getColor()));
				// else if (rdbtnDropdownMenu.isSelected())
				// model.addElement(new MyComboBox(glass.getX(), glass.getY(), glass.getX2(),
				// glass.getY2(), fcFont.getFontValue(), ccFont.getColor()));
			}
		});
		getContentPane().add(btnAdd, "cell 6 9,grow");

		JButton btnCancel = new JButton("CANCEL");
		btnCancel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnCancel.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnCancel.setBackground(_Settings.backgroundColor);
		btnCancel.setForeground(_Settings.labelColor);
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		getContentPane().add(btnCancel, "cell 3 12,grow");

		JButton btnBuild = new JButton("BUILD");
		btnBuild.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnBuild.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnBuild.setBackground(_Settings.backgroundColor);
		btnBuild.setForeground(_Settings.labelColor);
		btnBuild.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				String title = txtTitle.getText();
				if (title.isEmpty())
					title = "Form";
				List<JComponent> list = new ArrayList<JComponent>();
				for (Object object : model.toArray())
					list.add((JComponent) object);
				db.add(new cores.Form(background.getDirectory(), title, list));
				db.saveList();
				Form form = new Form(background, list, title);
				form.addWindowListener(new WindowListener() {

					@Override
					public void windowOpened(WindowEvent e) {
						// TODO Auto-generated method stub
						mainFrame.setFrameFocusable(false);
					}

					@Override
					public void windowIconified(WindowEvent e) {
						// TODO Auto-generated method stub
						mainFrame.setFrameFocusable(false);
					}

					@Override
					public void windowDeiconified(WindowEvent e) {
						// TODO Auto-generated method stub

					}

					@Override
					public void windowDeactivated(WindowEvent e) {
						// TODO Auto-generated method stub

					}

					@Override
					public void windowClosing(WindowEvent e) {
						// TODO Auto-generated method stub

					}

					@Override
					public void windowClosed(WindowEvent e) {
						// TODO Auto-generated method stub
						mainFrame.setFrameFocusable(true);
					}

					@Override
					public void windowActivated(WindowEvent e) {
						// TODO Auto-generated method stub
						mainFrame.setFrameFocusable(false);
					}
				});
				;
				dispose();
			}
		});
		getContentPane().add(btnBuild, "cell 5 12,grow");

		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

}
