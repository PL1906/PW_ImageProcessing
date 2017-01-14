import marvin.image.MarvinImage;
import marvin.image.MarvinImageMask;
import marvin.plugin.MarvinImagePlugin;

public class ImageProcessThread extends Thread
{
	private MarvinImagePlugin imagePlugin;
	private MarvinImage image;
	private MarvinImageMask mask;

	public ImageProcessThread(MarvinImagePlugin imagePlugin, MarvinImage image, MarvinImageMask mask)
	{
		this.imagePlugin = imagePlugin;
		this.image = image;
		this.mask = mask;
	}

	@Override
	public void run()
	{
		imagePlugin.process(image, image, mask);
	}
}
