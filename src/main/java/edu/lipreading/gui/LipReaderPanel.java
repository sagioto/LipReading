package edu.lipreading.gui;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

import edu.lipreading.Sample;
import edu.lipreading.Utils;
import edu.lipreading.classification.TimeWarperClassifier;
import edu.lipreading.vision.VideoConfiguration;

import java.awt.Color;
import java.awt.Font;
import java.util.Calendar;
import java.util.List;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class LipReaderPanel extends VideoCapturePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JLabel lblOutput;
	private boolean recording;
	private JButton btnRecord;
	
	/**
	 * Create the panel.
	 * @throws com.googlecode.javacv.FrameGrabber.Exception 
	 */
	public LipReaderPanel() {
		super();
		setBackground(Color.WHITE);
		setLayout(null);
		
		recording = false;
		
		btnRecord = new JButton("Record");
		btnRecord.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!recording) // Button should start recording
				{
					btnRecord.setText("Stop");
					stickersExt.initSample(Calendar.getInstance().getTime().toString());
					lblOutput.setText("");
					recording = true;
				}
				else // Button should stop recording
				{
					recording = false;
					btnRecord.setText("Record");
					Sample recordedSample = stickersExt.getSample();
					
					//TODO - Extract to thread:
					List<Sample> trainingSet;
					try {
						trainingSet = Utils.getTrainingSetFromZip(VideoConfiguration.XMLS_URL);
						TimeWarperClassifier twc = new TimeWarperClassifier();
						String outputText = twc.classify(trainingSet, recordedSample);
						lblOutput.setText(outputText);
					} 
					catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		btnRecord.setBackground(Color.WHITE);
		btnRecord.setBounds(315, 382, 89, 23);
		this.add(btnRecord);
		
		lblOutput = new JLabel("Output Label");
		lblOutput.setHorizontalAlignment(SwingConstants.CENTER);
		lblOutput.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblOutput.setForeground(Color.GRAY);
		lblOutput.setBounds(312, 452, 102, 22);
		this.add(lblOutput);
		
		

		canvas.setBounds(129, 10, 456, 362);
		
		
	}

	protected void getVideoFromCamera() throws com.googlecode.javacv.FrameGrabber.Exception {
		try {
			IplImage grabbed;
			while(!threadStop){
				synchronized (threadStop) {
					if (!threadStop)
					{
						if ((grabbed = grabber.grab()) == null)
							break;
						image = grabbed.getBufferedImage();
						canvas.setImage(image);
						canvas.paint(null);
						if (recording)
						{
							try {
								stickersExt.savePoints(grabbed);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}
			

		} catch (com.googlecode.javacv.FrameGrabber.Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			grabber.stop();
		}
	}

	
	
}
