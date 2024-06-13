package builders;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import frames.MyOptionPane;

public class Background extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private BufferedImage image;
	private String directory;

	public BufferedImage getImage() {
		return image;
	}

	public Background(String fileDir) {
		try {
			image = ImageIO.read(new File(fileDir));
			addMouseListener(new MouseAdapter() {
			});
			addMouseMotionListener(new MouseMotionAdapter() {
			});
			directory = fileDir;
		} catch (IOException e) {
			new MyOptionPane("The directory is not an image file or the image cannot be found.", MyOptionPane.ERROR_DIALOG_BOX);
		}
	}

	public String getDirectory() {
		return directory;
	}

	/*
	 * Override this method to display graphics on JPanel. Do not override paint
	 * method!
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(image, 0, 0, this);
	}

	/*
	 * Override getPreferredSize method so it returns dimensions of your image. Size
	 * of your container (Panel) will be equal to size of that image
	 */
	@Override
	public Dimension getPreferredSize() {
		if (image != null)
			return new Dimension(image.getWidth(), image.getHeight());
		else
			return new Dimension(200, 200);
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(getPreferredSize());
	}
}
