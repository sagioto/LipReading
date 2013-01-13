package edu.lipreading.gui;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Panel;
import java.awt.Toolkit;
import java.util.Arrays;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.googlecode.javacv.FrameGrabber;

import edu.lipreading.Constants;
public class MainFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	protected static FrameGrabber grabber;
	protected static String sampleName;
	private LipReaderPanel lipReaderPanel;
	private VideoConfigPanel videoConfigPanel;
	private FileLipReaderPanel fileLipReaderPanel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame frame = new MainFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}


	/**
	 * Create the frame.
	 */
	public MainFrame() {
		setTitle("Lip Reading");
		setIconImage(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource(Constants.LR_ICON)));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 720, 651);
		setResizable(false);
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);


		Panel titlePanel = new Panel();
		titlePanel.setBackground(new Color(0, 153, 204));
		titlePanel.setBounds(0, 0, 716, 90);
		contentPane.add(titlePanel);
		titlePanel.setLayout(null);

		JLabel lblTitle;
		List<String> fonts = Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
		String fontName = "Origin";
		if(!fonts.contains(fontName))
			lblTitle = new JLabel(new ImageIcon(MainFrame.class.getResource(Constants.LIP_READING_TITLE)));
		else{
			lblTitle = new JLabel("Lip Reading");
			lblTitle.setForeground(Color.WHITE);
			Font font = new Font(fontName, Font.PLAIN, 53);
			lblTitle.setFont(font);
		}
		lblTitle.setBounds(180, 16, 344, 73);
		titlePanel.add(lblTitle);


		final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(0, 88, 716, 537);
		fileLipReaderPanel = new FileLipReaderPanel();
		lipReaderPanel = new LipReaderPanel();
		videoConfigPanel = new VideoConfigPanel();
		tabbedPane.addTab("Read From File", null, fileLipReaderPanel, "Read from a file in the file system or a URL");
		tabbedPane.addTab("Live Read", null, lipReaderPanel, "Read from the camera");
		tabbedPane.addTab("Sticker Configurataion", null, videoConfigPanel, "Adjust the stickers' colors");

		JPanel TrainingPanel = new JPanel();
		tabbedPane.addTab("Training", null, TrainingPanel, "Train the reader");
		fileLipReaderPanel.setVisible(true);
		videoConfigPanel.setVisible(false);
		lipReaderPanel.setVisible(false);
		//disable the training tab
		tabbedPane.setEnabledAt(3, false);
		tabbedPane.addChangeListener(new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent e) {
				switch(tabbedPane.getSelectedIndex()){
				case 0: //file
					videoConfigPanel.stopVideo();
					lipReaderPanel.stopVideo();
					break;
				case 1: //camera
					fileLipReaderPanel.stopVideo();
					videoConfigPanel.stopVideo();
					lipReaderPanel.startVideo();
					break;
				case 2: //configuration
					fileLipReaderPanel.stopVideo();
					lipReaderPanel.stopVideo();
					videoConfigPanel.startVideo();	
					break;
				case 3:
					break;
				}
			}

		});
		contentPane.add(tabbedPane);
	}


}
