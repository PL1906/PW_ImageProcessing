
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.concurrent.Semaphore;

public class ImageProcessingThread extends Thread
{
	Semaphore progressLock;
	private BufferedImage image;
	private int startWidth, startHeight, width, height;
	private ImageProcessingFrame mainFrame;
	
	public ImageProcessingThread(BufferedImage image, ImageProcessingFrame mainFrame, Semaphore progressLock, int startWidth, int startHeight, int width, int height)
	{
		this.image = image;
		this.startWidth = startWidth;
		this.startHeight = startHeight;
		this.width = width;
		this.height = height;
		this.mainFrame = mainFrame;
		this.progressLock = progressLock;
	}

	@Override
	public void run()
	{
		desaturateImage();
	}
	
	public void desaturateImage()
	{
		mainFrame.setProgressValue(0);
		for (int i = startWidth; i < startWidth + width; i++)
		{
			for (int j = startHeight; j < startHeight + height; j++)
			{
				Color c = new Color(image.getRGB(i, j));
				int red = (int) (c.getRed() * 0.299);
				int green = (int) (c.getGreen() * 0.587);
				int blue = (int) (c.getBlue() * 0.114);
				int color = red + green + blue;
				Color newColor = new Color(color, color, color);
				image.setRGB(i, j, newColor.getRGB());
			}
			try
			{
				progressLock.acquire();
				mainFrame.updateProgress(1);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			finally
			{
				progressLock.release();
			}
		}
	}
}
