
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.Callable;
import javax.imageio.ImageIO;

public class ImageLoadingTask implements Callable<BufferedImage>
{
	private File file;

	public ImageLoadingTask(File file)
	{
		this.file = file;
	}

	@Override
	public BufferedImage call() throws Exception
	{
		return ImageIO.read(file);
	}
}
