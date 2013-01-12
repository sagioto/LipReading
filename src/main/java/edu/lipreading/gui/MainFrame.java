package edu.lipreading.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

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
	private JLabel lblLipReaderMenu;
	private JLabel lblVideoConfigMenu;
	private ScreenType currentScreen;
	private JLabel lblFileLipReaderMenu;

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
		setBounds(100, 100, 732, 663);
		setResizable(false);
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		lipReaderPanel = new LipReaderPanel();
		videoConfigPanel = new VideoConfigPanel();
		fileLipReaderPanel = new FileLipReaderPanel();


		Panel titlePanel = new Panel();
		titlePanel.setBackground(new Color(0, 153, 204));
		titlePanel.setBounds(0, 0, 716, 104);
		contentPane.add(titlePanel);
		titlePanel.setLayout(null);

		lblVideoConfigMenu = new JLabel("Video Configuration");
		lblVideoConfigMenu.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (currentScreen != ScreenType.ConfigurationVideo)
				{
					changeScreen(ScreenType.ConfigurationVideo);
				}
				lblLipReaderMenu.setForeground(Color.WHITE);
				lblVideoConfigMenu.setForeground(Color.LIGHT_GRAY);
				lblFileLipReaderMenu.setForeground(Color.WHITE);
			}


		});
		lblVideoConfigMenu.setForeground(Color.WHITE);
		lblVideoConfigMenu.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblVideoConfigMenu.setHorizontalAlignment(SwingConstants.CENTER);
		lblVideoConfigMenu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lblVideoConfigMenu.setBounds(528, 90, 119, 14);
		titlePanel.add(lblVideoConfigMenu);

		JLabel lblTraining = new JLabel("Training");
		lblTraining.setHorizontalAlignment(SwingConstants.CENTER);
		lblTraining.setForeground(Color.BLACK);
		lblTraining.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblTraining.setBounds(644, 90, 72, 14);
		titlePanel.add(lblTraining);

		lblLipReaderMenu = new JLabel("Lip Reader");
		lblLipReaderMenu.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (currentScreen != ScreenType.LipReader)
				{
					changeScreen(ScreenType.LipReader);
				}
				lblVideoConfigMenu.setForeground(Color.WHITE);
				lblLipReaderMenu.setForeground(Color.LIGHT_GRAY);
				lblFileLipReaderMenu.setForeground(Color.WHITE);
			}
		});
		lblLipReaderMenu.setHorizontalAlignment(SwingConstants.CENTER);
		lblLipReaderMenu.setForeground(Color.WHITE);
		lblLipReaderMenu.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblLipReaderMenu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lblLipReaderMenu.setBounds(437, 90, 81, 14);
		titlePanel.add(lblLipReaderMenu);

		lblFileLipReaderMenu = new JLabel("Lip Reader From File");
		lblFileLipReaderMenu.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (currentScreen != ScreenType.FileLipReader)
				{
					changeScreen(ScreenType.FileLipReader);
				}
				lblVideoConfigMenu.setForeground(Color.WHITE);
				lblLipReaderMenu.setForeground(Color.WHITE);
				lblFileLipReaderMenu.setForeground(Color.LIGHT_GRAY);
			}
		});
		lblFileLipReaderMenu.setHorizontalAlignment(SwingConstants.CENTER);
		lblFileLipReaderMenu.setForeground(Color.LIGHT_GRAY);
		lblFileLipReaderMenu.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblFileLipReaderMenu.setBounds(279, 90, 148, 14);
		lblFileLipReaderMenu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		titlePanel.add(lblFileLipReaderMenu);

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
		lblTitle.setBounds(10, 16, 344, 73);
		titlePanel.add(lblTitle);

		//currentScreen = ScreenType.LipReader;
		lipReaderPanel.setBounds(0, 105, 716, 520);
		contentPane.add(lipReaderPanel);
		lipReaderPanel.setVisible(false);
		//lipReaderPanel.setVisible(true);
		//lipReaderPanel.startVideo();

		videoConfigPanel.setBounds(0, 105, 716, 520);
		contentPane.add(videoConfigPanel);
		videoConfigPanel.setVisible(false);

		currentScreen = ScreenType.FileLipReader;
		fileLipReaderPanel.setBounds(0, 105, 716, 520);
		contentPane.add(fileLipReaderPanel);
		fileLipReaderPanel.setVisible(true);



	}




	private void changeScreen(ScreenType screenType) {
		currentScreen = screenType;
		switch (screenType) {
		case LipReader:
			lipReaderPanel.setVisible(true);
			fileLipReaderPanel.setVisible(false);
			fileLipReaderPanel.stopVideo();
			videoConfigPanel.setVisible(false);
			videoConfigPanel.stopVideo();
			lipReaderPanel.startVideo();
			break;
		case ConfigurationVideo:
			videoConfigPanel.setVisible(true);
			fileLipReaderPanel.setVisible(false);
			fileLipReaderPanel.stopVideo();
			lipReaderPanel.setVisible(false);
			lipReaderPanel.stopVideo();
			videoConfigPanel.startVideo();
			break;
		case FileLipReader:
			fileLipReaderPanel.setVisible(true);
			videoConfigPanel.setVisible(false);
			videoConfigPanel.stopVideo();
			lipReaderPanel.setVisible(false);
			lipReaderPanel.stopVideo();
			break;

		default:
			break;
		}

	}
}
