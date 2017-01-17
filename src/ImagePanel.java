
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

public class ImagePanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	private BufferedImage image;

	public ImagePanel()
	{
		super();
		Dimension dimension = new Dimension(500, 500);
		setPreferredSize(dimension);
	}

	@Override
	public void paintComponent(Graphics g)
	{
		Graphics2D g2d = (Graphics2D) g;
		g2d.drawImage(image, 0, 0, this);
	}
	
	public void setImage(BufferedImage imageNew)
	{
		image = imageNew;
	}
}
