package edu.lipreading.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

import edu.lipreading.Sample;
import edu.lipreading.classification.TimeWarperClassifier;
import edu.lipreading.library.TrainingSet;
import javax.swing.JTextPane;
import java.awt.SystemColor;
import javax.swing.UIManager;

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
	private boolean recording;
	private JButton btnRecord;
	private TimeWarperClassifier classifier;
	private List<Sample> trainingSet;
	private JTextPane txtFilePath;
	private Thread classifierThread;
	private JLabel lblNewLabel;
	/**
	 * Create the panel.
	 * @throws com.googlecode.javacv.FrameGrabber.Exception 
	 */
	public FileLipReaderPanel() {
		super();
		canvas.setBackground(UIManager.getColor("InternalFrame.inactiveTitleGradient"));
		setBackground(Color.WHITE);
		setLayout(null);
		
		recording = false;
		
		lblOutput = new JLabel("");
		lblOutput.setHorizontalAlignment(SwingConstants.CENTER);
		lblOutput.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblOutput.setForeground(Color.GRAY);
		lblOutput.setBounds(294, 451, 102, 22);
		this.add(lblOutput);
		
		canvas.setBounds(142, 10, 420, 308);
		
		txtFilePath = new JTextPane();
		txtFilePath.setBackground(SystemColor.info);
		txtFilePath.setText("https://dl.dropbox.com/u/8720454/no-%281%29.MOV"); //TODO - Change default
		txtFilePath.setBounds(204, 337, 358, 20);
		add(txtFilePath);
		
		trainingSet = TrainingSet.getTrainingSet();
		
		classifier = new TimeWarperClassifier();
		classifier.train(trainingSet);
		
		
		btnRecord = new JButton("Read Lips From File");
		btnRecord.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				btnRecord.setEnabled(false);
				btnRecord.setText("Downloading File...");
				lblOutput.setText("");
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
				stickersExt.initSample(txtFilePath.getText());
				startVideo();
				
				
			}
		});
		btnRecord.setBackground(Color.WHITE);
		btnRecord.setBounds(277, 384, 143, 23);
		this.add(btnRecord);
		
		lblNewLabel = new JLabel("File Path:");
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNewLabel.setBounds(142, 339, 53, 14);
		add(lblNewLabel);
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
						stickersExt.savePoints(grabbed);
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
		    		String outputText = classifier.test(stickersExt.getSample());
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
