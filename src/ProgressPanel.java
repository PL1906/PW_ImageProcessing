
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class ProgressPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	private JProgressBar progressBar;
	
	public ProgressPanel()
	{
		super();
		
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		add(progressBar);
	}
	
	public void updateProgressBar(int i)
	{
		progressBar.setValue(progressBar.getValue() + i);
	}
	
	public void setMaxProgressBar(int i)
	{
		progressBar.setMaximum(i);
	}
}
