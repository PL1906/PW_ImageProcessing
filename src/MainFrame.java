
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class MainFrame extends JFrame
{
	private static final long serialVersionUID = 1L;

	private ProgressPanel progressPanel;
	private ImagePanel imagePanel;
	private JLabel label;
	
	// Main image
	private BufferedImage image, processedImage;
	// Image chunks
	private BufferedImage[] images;
	// Semaphore for updating progress bar value
	private Semaphore progressLock = new Semaphore(1);
	// Number of threads
	private static int threadsNumber = 4;
	
	public MainFrame()
	{
		super("Programowanie wspolbiezne - Image processing");

		progressPanel = new ProgressPanel();
		imagePanel = new ImagePanel();
		label = new JLabel("Loading image...");

		Container con = getContentPane();
		con.setLayout(new BorderLayout());
		con.add(label, BorderLayout.WEST);
		con.add(progressPanel, BorderLayout.NORTH);
		con.add(imagePanel, BorderLayout.SOUTH);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}
	
	public static void main(String[] args) throws InterruptedException, ExecutionException
	{
		// Create main window
		MainFrame mainFrame = new MainFrame();
		
		// Create file chooser
		JFileChooser chooser = new JFileChooser();
	    // Enable multi file selection
	    chooser.setMultiSelectionEnabled(true);
	    // Set default chooser directory
	    File f = new File("./files/");
	    chooser.setCurrentDirectory(f);
	    // Open chooser dialog
	    chooser.showOpenDialog(null);
	    // Get selected files from chooser
	    File[] selectedFiles = chooser.getSelectedFiles();
	    // Load selected files using Executor
	    List<Future<BufferedImage>> loadedFiles = loadWithExecutor(selectedFiles);
	    
	    // Process each loaded file image
		for (int i = 0; i < loadedFiles.size(); i++)
		{
			// Start image processing
			mainFrame.processImage(loadedFiles.get(i).get(), i);
			// Pause
			Thread.sleep(1000);
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
		// Create thumbnail of image
		setImage(resizeImage(image, 500, 500));
		// Set label text
		label.setText("Image loaded. Processing image...");
		// Refresh window
		repaint();
		// Create array with image chunks
		images = new BufferedImage[threadsNumber];
		// Set max value of progress bar
		setMaxProgress(image.getWidth());

		// Set width for chunk
		int chunkWidth = image.getWidth() / threadsNumber;
		// Set height for chunk
		int chunkHeight = image.getHeight();

		// Slice image into chunks
		for (int i = 0; i < threadsNumber; i++)
		{
			// Initialize the image array with image chunks
			images[i] = new BufferedImage(chunkWidth, chunkHeight, image.getType());

			// Draws the image chunk
			Graphics2D gr = images[i].createGraphics();
			gr.drawImage(image, 0, 0, chunkWidth, chunkHeight, chunkWidth * i, 0, chunkWidth * i + chunkWidth, chunkHeight, null);
			gr.dispose();
		}

		// Create array with threads
		ImageProcessingThread[] thread = new ImageProcessingThread[threadsNumber];
		// Get image processing start time
		long start = System.currentTimeMillis();

		// Process image
		for (int i = 0; i < threadsNumber; i++)
		{
			// Initialize the image array with threads
			thread[i] = new ImageProcessingThread(images[i], this, progressLock, 0, 0, chunkWidth, chunkHeight);
			// Run the thread
			thread[i].start();
		}

		for (int i = 0; i < threadsNumber; i++)
		{
			thread[i].join();
		}

		// Get image processing stop time
		label.setText("Image processing finished: " + (System.currentTimeMillis() - start) + " ms (" + threadsNumber
				+ " threads)");

		// Combine image chunks into one image
		processedImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		Graphics2D gr = processedImage.createGraphics();
		for (int i = 0; i < threadsNumber; i++)
		{
			gr.drawImage(images[i], i * chunkWidth, 0, i * chunkWidth + chunkWidth, chunkHeight, 0, 0, chunkWidth,
					chunkHeight, null);
		}
		gr.dispose();
		// Create thumbnail of image
		setImage(resizeImage(processedImage, 500, 500));
		// Refresh window
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
	
	// Resize image
	private BufferedImage resizeImage(BufferedImage originalImage, int width, int height)
	{
		BufferedImage resizedImage = new BufferedImage(width, height, originalImage.getType());
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, width, height, null);
		g.dispose();

		return resizedImage;
	}
	
	// Load files using Executor
	@SuppressWarnings("finally")
	public static List<Future<BufferedImage>> loadWithExecutor(File[] files)
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
		finally
		{
			service.shutdown();
			return results;
		}
	}

	// Load single image file via Executor
	public static class ImageLoadingTask implements Callable<BufferedImage>
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
}
