package frames;

import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.table.DefaultTableModel;

import builders.Background;
import cores.Form;
import databases.Forms;
import globals._Settings;
import net.miginfocom.swing.MigLayout;

public class Main extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private Forms db;
	private JScrollPane spForm;
	private JTable tblForm;
	private JButton btnBuild;
	private JButton btnDelete;
	private JLabel lblAvailable;
	private JLabel lblMessage;
	private JButton btnCflow;

	public void updateTable() {
		List<Form> list = db.getList();
		tblForm.setModel(new MyTableModel(list));
	}

	public void setFrameFocusable(boolean isFocusable) {
		setFocusableWindowState(isFocusable);
	}

	public Main getMain() {
		return this;
	}

	/**
	 * Create the frame.
	 */
	public Main(Forms formDB) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane = new JPanel();
		contentPane.setBorder(null);
		setContentPane(contentPane);
		contentPane.setLayout(new MigLayout("", "[150][grow][50][100]", "[60][40][200:n,grow][10][50]"));
		setTitle("cFlow Form Builder");
		contentPane.setBackground(_Settings.backgroundColor);
		setAutoRequestFocus(true);

		db = formDB;

		spForm = new JScrollPane();
		spForm.setBackground(null);
		spForm.setOpaque(false);
		spForm.getViewport().setOpaque(false);

		btnBuild = new JButton("BUILD");
		btnBuild.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnBuild.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnBuild.setBackground(_Settings.backgroundColor);
		btnBuild.setForeground(_Settings.labelColor);
		btnBuild.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				setFrameFocusable(false);
				JFileChooser source = new JFileChooser();
				source.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int choice = source.showOpenDialog(null);
				if (choice == JFileChooser.APPROVE_OPTION) {
					Builder builder = new Builder(new Background(source.getSelectedFile().getAbsolutePath()), formDB,
							getMain());
					builder.addWindowListener(new WindowListener() {

						@Override
						public void windowOpened(WindowEvent e) {
							// TODO Auto-generated method stub

						}

						@Override
						public void windowIconified(WindowEvent e) {
							// TODO Auto-generated method stub

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
							setFrameFocusable(true);
							updateTable();
						}

						@Override
						public void windowActivated(WindowEvent arg0) {
							// TODO Auto-generated method stub

						}

					});
				}
			}
		});
		contentPane.add(btnBuild, "cell 0 0,grow");

		btnCflow = new JButton("cFlow");
		btnCflow.setBackground(_Settings.backgroundColor);
		btnCflow.setForeground(_Settings.labelColor);
		btnCflow.setFont(new Font("Arial", Font.BOLD, 14));
		btnCflow.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnCflow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				File file = new File(System.getProperty("user.home") + "\\cFlow\\cFlow.jar");
				if (file.exists()) {
					try {
						Desktop.getDesktop().open(file);
						System.exit(0);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						new MyOptionPane(
								"cFlow has failed to start. Please try reinstalling the software via the installer.",
								MyOptionPane.ERROR_DIALOG_BOX);
					}
				}
			}
		});
		contentPane.add(btnCflow, "cell 3 0,grow");

		lblAvailable = new JLabel("Available Forms");
		lblAvailable.setHorizontalAlignment(SwingConstants.CENTER);
		lblAvailable.setVerticalAlignment(SwingConstants.BOTTOM);
		lblAvailable.setBorder(null);
		lblAvailable.setFont(new Font("Century Gothic", Font.PLAIN, 25));
		lblAvailable.setBackground(null);
		lblAvailable.setForeground(_Settings.textFieldColor);
		contentPane.add(lblAvailable, "cell 0 1 4 1,grow");
		contentPane.add(spForm, "cell 0 2 4 1,grow");

		new DefaultTableModel() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}

		};

		tblForm = new JTable();
		tblForm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblForm.setGridColor(_Settings.labelColor);
		tblForm.setFont(new Font("Century Gothic", Font.BOLD, 16));
		tblForm.setBackground(_Settings.backgroundColor);
		tblForm.setForeground(_Settings.textFieldColor);
		tblForm.setRowHeight(80);
		tblForm.setTableHeader(null);
		tblForm.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				if (e.getClickCount() == 2) {
					int row = tblForm.getSelectedRow();
					if (row >= 0) {
						String title = (String) tblForm.getValueAt(row, 0);
						Form form = db.get(db.find(title));
						Background bg = new Background(form.getDirectory());
						setFrameFocusable(false);
						String newDir = null;
						if (bg.getImage() == null) {
							JFileChooser source = new JFileChooser();
							source.setFileSelectionMode(JFileChooser.FILES_ONLY);
							int choice = source.showOpenDialog(null);
							if (choice == JFileChooser.APPROVE_OPTION) {
								newDir = source.getSelectedFile().getAbsolutePath();
								bg = new Background(newDir);
							}
							setFrameFocusable(true);
						}
						if (bg.getImage() != null) {
							if (newDir != null) {
								form.setDirectory(newDir);
								db.saveList();
							}
							setFrameFocusable(false);
							frames.Form frame = new frames.Form(bg, form.getList(), form.getTitle());
							frame.addWindowListener(new WindowListener() {

								@Override
								public void windowOpened(WindowEvent arg0) {
									// TODO Auto-generated method stub

								}

								@Override
								public void windowIconified(WindowEvent arg0) {
									// TODO Auto-generated method stub

								}

								@Override
								public void windowDeiconified(WindowEvent arg0) {
									// TODO Auto-generated method stub

								}

								@Override
								public void windowDeactivated(WindowEvent arg0) {
									// TODO Auto-generated method stub

								}

								@Override
								public void windowClosing(WindowEvent arg0) {
									// TODO Auto-generated method stub

								}

								@Override
								public void windowClosed(WindowEvent arg0) {
									// TODO Auto-generated method stub
									setFrameFocusable(true);
								}

								@Override
								public void windowActivated(WindowEvent arg0) {
									// TODO Auto-generated method stub

								}
							});
						}
					}
				}
			}
		});
		spForm.setViewportView(tblForm);

		btnDelete = new JButton("DELETE");
		btnDelete.setBackground(_Settings.backgroundColor);
		btnDelete.setForeground(_Settings.labelColor);
		btnDelete.setFont(new Font("Arial", Font.BOLD, 14));
		btnDelete.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnDelete.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				if (tblForm.getSelectedRow() == -1)
					new MyOptionPane("Select a record first!", MyOptionPane.ERROR_DIALOG_BOX);
				else {
					String title = (String) tblForm.getValueAt(tblForm.getSelectedRow(), 0);
					if (db.remove(title)) {
						db.saveList();
						new MyOptionPane("Form deleted!", MyOptionPane.CONFIRMATION_DIALOG_BOX);
						updateTable();
					} else
						new MyOptionPane("Form could not be deleted!", MyOptionPane.ERROR_DIALOG_BOX);
				}
			}
		});

		lblMessage = new JLabel("Double-click on a form on the list to use.");
		lblMessage.setHorizontalAlignment(SwingConstants.LEFT);
		lblMessage.setBorder(null);
		lblMessage.setFont(new Font("Arial", Font.ITALIC, 15));
		lblMessage.setBackground(null);
		lblMessage.setForeground(_Settings.labelColor);
		contentPane.add(lblMessage, "cell 0 4 2 1,grow");
		contentPane.add(btnDelete, "cell 2 4 2 1,grow");

		updateTable();
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

}
