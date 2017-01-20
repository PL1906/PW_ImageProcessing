
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class MainFrame extends JFrame implements ActionListener
{
	private static final long serialVersionUID = 1L;
	
	private JComboBox<String> threadsSelect;
	private String[] threadsString = {"1", "2", "4", "8", "16"};
	
	private JLabel threadsLabel;
	private JButton startButton;
	private JButton selectButton;
	
	private File[] selectedFiles = null;

	public MainFrame()
	{
		super("Programowanie wspolbiezne - Image processing");
		
		threadsSelect = new JComboBox<String>(threadsString);
		threadsSelect.setSelectedIndex(2);
		threadsLabel = new JLabel("Threads number", SwingConstants.RIGHT);
		startButton = new JButton("Start images processing");
		selectButton = new JButton("Choose images");
		
		startButton.addActionListener(this);
		selectButton.addActionListener(this);
		
		JPanel threadsPanel = new JPanel();
		threadsPanel.setLayout(new GridLayout(0, 2, 10, 10));
		threadsPanel.add(threadsLabel);
		threadsPanel.add(threadsSelect);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(0, 2, 10, 10));
		buttonPanel.add(selectButton);
		buttonPanel.add(startButton);

		Container con = getContentPane();
		con.setLayout(new BorderLayout());
		con.add(threadsPanel, BorderLayout.NORTH);
		con.add(buttonPanel, BorderLayout.SOUTH);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(400, 100);
		setVisible(true);
	}
	
	public static void main(String[] args)
	{
		// Create main window
		new MainFrame();
	}

	@Override
	public void actionPerformed(ActionEvent event)
	{
		Object source = event.getSource();
		
		if(source == selectButton)
		{
			JFileChooser chooser = new JFileChooser();
		    chooser.setMultiSelectionEnabled(true);
		    File f = new File("./files/");
		    chooser.setCurrentDirectory(f);
		    chooser.showOpenDialog(null);
		    selectedFiles = chooser.getSelectedFiles();
		}
		else if(source == startButton)
		{
			if(selectedFiles != null)
			{
				Thread processingFrame = new Thread(new ImageProcessingFrame(selectedFiles, (int) Math.pow(2, threadsSelect.getSelectedIndex())));
				processingFrame.start();
			}
		}
	}
}
