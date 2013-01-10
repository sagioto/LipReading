package edu.lipreading.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

import edu.lipreading.Constants;
import edu.lipreading.Sample;
import edu.lipreading.TrainingSet;
import edu.lipreading.classification.Classifier;
import edu.lipreading.classification.TimeWarperClassifier;
import edu.lipreading.normalization.CenterNormalizer;
import edu.lipreading.normalization.Normalizer;

import javax.swing.JTextPane;
import java.awt.SystemColor;
import javax.swing.UIManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;

/**
 * @author Dor Leitman
 *
 */

public class FileLipReaderPanel extends VideoCapturePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JLabel lblOutput;
	private JButton btnRecord;
	private Classifier classifier;
	private Normalizer normalizer;
	private List<Sample> trainingSet;
	private JTextPane txtFilePath;
	private Thread classifierThread;
	private JLabel lblNewLabel;
	private Sample recordedSample;
	final JFileChooser fileChooser = new JFileChooser(); //TODO - make only for video files
	
	/**
	 * Create the panel.
	 * @throws com.googlecode.javacv.FrameGrabber.Exception 
	 */
	public FileLipReaderPanel() {
		super();
		canvas.setBackground(UIManager.getColor("InternalFrame.inactiveTitleGradient"));
		setBackground(Color.WHITE);
		setLayout(null);
		
		
		lblOutput = new JLabel("");
		lblOutput.setHorizontalAlignment(SwingConstants.CENTER);
		lblOutput.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblOutput.setForeground(Color.GRAY);
		lblOutput.setBounds(244, 447, 204, 22);
		this.add(lblOutput);
		
		canvas.setBounds(142, 10, 420, 308);
		
		txtFilePath = new JTextPane();
		txtFilePath.setToolTipText("Please insert a http path or choose a file");
		txtFilePath.setBackground(SystemColor.info);
		txtFilePath.setText("https://dl.dropbox.com/u/8720454/set2/no/no-1.MOV"); //TODO - Change default
		txtFilePath.setBounds(204, 337, 320, 20);
		add(txtFilePath);
		
		trainingSet = TrainingSet.get();
		
		classifier = new TimeWarperClassifier();
		classifier.train(trainingSet);
		normalizer = new CenterNormalizer();
		
		btnRecord = new JButton("Read Lips From File");
		btnRecord.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				btnRecord.setEnabled(false);
				btnRecord.setText("Downloading File...");
				lblOutput.setText("");
				Thread videoGrabberThread = new Thread(new Runnable()
				{
					public void run()
				    {
						setVideoInput(txtFilePath.getText());
						try {
							initGrabber();
						} catch (MalformedURLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						recordedSample = new Sample(txtFilePath.getText());
						startVideo();
				    }
				});
				videoGrabberThread.start();
			}
		});
		btnRecord.setBackground(Color.WHITE);
		btnRecord.setBounds(275, 387, 143, 23);
		this.add(btnRecord);
		
		lblNewLabel = new JLabel("File Path:");
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNewLabel.setBounds(142, 339, 53, 14);
		add(lblNewLabel);
		
		JButton btnChooseFile = new JButton(new ImageIcon(getClass().getResource(Constants.FILE_CHOOSER_IMAGE_FILE_PATH)));
		btnChooseFile.setBorderPainted(false);
		btnChooseFile.setBackground(Color.WHITE);
		btnChooseFile.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				int returnVal = fileChooser.showOpenDialog(FileLipReaderPanel.this);
				 
	            if (returnVal == JFileChooser.APPROVE_OPTION)
	            	txtFilePath.setText(fileChooser.getSelectedFile().getPath());
			}
		});
		btnChooseFile.setBounds(530, 333, 32, 32);
		add(btnChooseFile);

	}

	@Override
	protected void getVideoFromSource() throws com.googlecode.javacv.FrameGrabber.Exception {
		try {
			IplImage grabbed;
			
			while((grabbed = grabber.grab()) != null && !threadStop){
				synchronized (threadStop) {
					image = grabbed.getBufferedImage();
					canvas.setImage(image);
					canvas.paint(null);
					try {
						recordedSample.getMatrix().add(stickersExtractor.getPoints(grabbed));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
			stopVideo();
			canvas.setImage(null);
			canvas.paint(null);
			
			
			classifierThread = new Thread(new Runnable()
			{

				public void run()
			    {
					recordedSample = normalizer.normalize(recordedSample);
		    		String outputText = classifier.test(recordedSample);
					lblOutput.setText(outputText);
					btnRecord.setText("Read Lips From File");
					btnRecord.setEnabled(true);

			    }
			});
			classifierThread.start();

		} catch (com.googlecode.javacv.FrameGrabber.Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			grabber.stop();
		}
	}
}
