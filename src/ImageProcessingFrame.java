
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class ImageProcessingFrame extends JFrame implements Runnable
{
	private static final long serialVersionUID = 1L;

	private ProgressPanel progressPanel;
	private ImagePanel imagePanel;
	private JLabel label;

	private BufferedImage image, processedImage;
	private BufferedImage[] images;
	private Semaphore progressLock;
	private int threadsNumber = 4;
	private File[] selectedFiles;

	public ImageProcessingFrame(File[] selectedFiles, int threadsNumber)
	{
		super("Programowanie wspolbiezne - Image processing");

		this.selectedFiles = selectedFiles;
		this.threadsNumber = threadsNumber;
		progressPanel = new ProgressPanel();
		imagePanel = new ImagePanel();
		label = new JLabel("Loading images...");
		progressLock = new Semaphore(1);

		Container con = getContentPane();
		con.setLayout(new BorderLayout());
		con.add(label, BorderLayout.WEST);
		con.add(progressPanel, BorderLayout.NORTH);
		con.add(imagePanel, BorderLayout.SOUTH);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}

	@Override
	public void run()
	{
		try
		{
			// Load selected files using Executor
			long start = System.currentTimeMillis();
			List<Future<BufferedImage>> loadedFiles = loadWithExecutor(selectedFiles);
			label.setText("Images loading finished: " + (System.currentTimeMillis() - start) + " ms (" + threadsNumber + " threads)");
			Thread.sleep(2000);

			// Process each loaded file image
			for (int i = 0; i < loadedFiles.size(); i++)
			{
				processImage(loadedFiles.get(i).get(), i);
				Thread.sleep(1000);
			}
		}
		catch (InterruptedException | ExecutionException e)
		{
			e.printStackTrace();
		}
	}

	public void updateProgress(int i)
	{
		progressPanel.updateProgressBar(i);
	}

	public void setMaxProgress(int i)
	{
		progressPanel.setMaxProgressBar(i);
	}

	public void setProgressValue(int i)
	{
		progressPanel.setProgressBarValue(i);
	}

	public void setImage(BufferedImage imageNew)
	{
		imagePanel.setImage(imageNew);
	}

	public void processImage(BufferedImage imageNew, int n) throws InterruptedException
	{
		image = imageNew;
		setImage(resizeImage(image, 500, 500));
		label.setText("Processing image...");
		repaint();
		images = new BufferedImage[threadsNumber];
		setMaxProgress(image.getWidth());
		int chunkWidth = image.getWidth() / threadsNumber;
		int chunkHeight = image.getHeight();

		// Slice image into chunks
		for (int i = 0; i < threadsNumber; i++)
		{
			images[i] = new BufferedImage(chunkWidth, chunkHeight, image.getType());
			Graphics2D gr = images[i].createGraphics();
			gr.drawImage(image, 0, 0, chunkWidth, chunkHeight, chunkWidth * i, 0, chunkWidth * i + chunkWidth, chunkHeight, null);
			gr.dispose();
		}
		
		ImageProcessingThread[] thread = new ImageProcessingThread[threadsNumber];
		long start = System.currentTimeMillis();

		// Process image
		for (int i = 0; i < threadsNumber; i++)
		{
			thread[i] = new ImageProcessingThread(images[i], this, progressLock, 0, 0, chunkWidth, chunkHeight);
			thread[i].start();
		}

		for (int i = 0; i < threadsNumber; i++)
		{
			thread[i].join();
		}
		
		label.setText("Image processing finished: " + (System.currentTimeMillis() - start) + " ms (" + threadsNumber + " threads)");

		// Combine image chunks into one image
		processedImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		Graphics2D gr = processedImage.createGraphics();
		for (int i = 0; i < threadsNumber; i++)
		{
			gr.drawImage(images[i], i * chunkWidth, 0, i * chunkWidth + chunkWidth, chunkHeight, 0, 0, chunkWidth, chunkHeight, null);
		}
		gr.dispose();
		setImage(resizeImage(processedImage, 500, 500));
		repaint();

		// Save processed image to file
		/*try
		{
			ImageIO.write(processedImage, "jpg", new File("output/processedImage" + n + ".jpg"));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}*/

	}

	private BufferedImage resizeImage(BufferedImage originalImage, int width, int height)
	{
		BufferedImage resizedImage = new BufferedImage(width, height, originalImage.getType());
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, width, height, null);
		g.dispose();

		return resizedImage;
	}
	
	public List<Future<BufferedImage>> loadWithExecutor(File[] files)
	{
		ExecutorService service = Executors.newFixedThreadPool(threadsNumber);
		List<ImageLoadingTask> tasks = new ArrayList<>(files.length);
		List<Future<BufferedImage>> results = null;
		for (File file : files)
		{
			tasks.add(new ImageLoadingTask(file));
		}
		try
		{
			results = service.invokeAll(tasks);
		}
		catch (InterruptedException ex)
		{
			ex.printStackTrace();
		}

		service.shutdown();
		return results;
	}
}
