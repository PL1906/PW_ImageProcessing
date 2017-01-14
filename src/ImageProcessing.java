import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import marvin.gui.MarvinImagePanel;
import marvin.image.MarvinImage;
import marvin.image.MarvinImageMask;
import marvin.io.MarvinImageIO;
import marvin.plugin.MarvinImagePlugin;
import marvin.util.MarvinPluginLoader;
import static marvin.MarvinPluginCollection.*;

public class ImageProcessing extends JFrame implements ActionListener
{
	private static final long serialVersionUID = 1L;
	
	private JLabel labelPerformance;
	
	private JTextField fileNameInput;

	private JButton buttonSingleThread,
					buttonMultiThread,
					buttonLoad,
					buttonReset;

	private MarvinImagePanel imagePanel;
	private MarvinImage image, backupImage;
	private MarvinImagePlugin imagePlugin;
	
	private int threadNumber = 8;
	private long processStartTime;

	public ImageProcessing()
	{
		super("Programowanie wspolbiezne - Image processing");

		// Create Graphical Interface
		buttonLoad = new JButton("Load Image");
		buttonLoad.addActionListener(this);
		buttonSingleThread = new JButton("SingleThread");
		buttonSingleThread.addActionListener(this);
		buttonMultiThread = new JButton("MultiThread");
		buttonMultiThread.addActionListener(this);
		buttonReset = new JButton("Reset Preview");
		buttonReset.addActionListener(this);
		labelPerformance = new JLabel("Performance:");
		fileNameInput = new JTextField("test.jpg");

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(buttonLoad);
		buttonPanel.add(buttonSingleThread);
		buttonPanel.add(buttonMultiThread);
		buttonPanel.add(buttonReset);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(3, 1, 0, 0));
		mainPanel.add(buttonPanel);
		mainPanel.add(fileNameInput);
		mainPanel.add(labelPerformance);

		// ImagePanel
		imagePanel = new MarvinImagePanel();
		
		Container con = getContentPane();
		con.setLayout(new BorderLayout());
		con.add(mainPanel, BorderLayout.NORTH);
		con.add(imagePanel, BorderLayout.SOUTH);

		// Load image
		String filePath = "./files/" + fileNameInput.getText();
		image = MarvinImageIO.loadImage(filePath);
		backupImage = image.clone();
		thumbnailByHeight(image.clone(), image, 500);
		imagePanel.setImage(image);
		
		setSize(500, 650);
		setVisible(true);
	}

	public static void main(String args[])
	{
		ImageProcessing t = new ImageProcessing();
		t.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void actionPerformed(ActionEvent e)
	{
		image = backupImage.clone();
		
		if (e.getSource() == buttonSingleThread)
		{
			imagePlugin = MarvinPluginLoader.loadImagePlugin("org.marvinproject.image.color.grayScale.jar");
			processStartTime = System.currentTimeMillis();
			imagePlugin.process(image, image);
			labelPerformance.setText("Performance: "+ (System.currentTimeMillis() - processStartTime)+ " milliseconds (Single Thread)");
		}
		else if (e.getSource() == buttonMultiThread)
		{
			// Create image mask for multithreading
			MarvinImageMask[] mask = new MarvinImageMask[threadNumber];
			int maskWidth = image.getWidth() / threadNumber;

			for (int i = 0; i < threadNumber; i++)
			{
				mask[i] = new MarvinImageMask(image.getWidth(), image.getHeight(), i * maskWidth, 0, maskWidth, image.getHeight());
			}
			
			imagePlugin = MarvinPluginLoader.loadImagePlugin("org.marvinproject.image.color.grayScale.jar");
			ImageProcessThread[] thread = new ImageProcessThread[threadNumber];
			processStartTime = System.currentTimeMillis();
			
			for (int i = 0; i < threadNumber; i++)
			{
				thread[i] = new ImageProcessThread(imagePlugin, image, mask[i]);
				thread[i].start();
			}
			
			for (int i = 0; i < threadNumber; i++)
			{
				try
				{
					thread[i].join();
				}
				catch (InterruptedException e1)
				{
					e1.printStackTrace();
				}
			}
			labelPerformance.setText("Performance: "+ (System.currentTimeMillis() - processStartTime)+ " milliseconds (Multi Thread)");
		}
		else if (e.getSource() == buttonLoad)
		{
			String filePath = "./files/" + fileNameInput.getText();
			image = MarvinImageIO.loadImage(filePath);
			backupImage = image.clone();
		}
		
		image.update();
		thumbnailByHeight(image.clone(), image, 500);
		imagePanel.setImage(image);
	}
}
