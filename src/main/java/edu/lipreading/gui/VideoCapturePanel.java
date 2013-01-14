package edu.lipreading.gui;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import edu.lipreading.vision.ColoredStickersFeatureExtractor;

public class VideoCapturePanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String videoInput = null;
	protected BufferedImage image = null;
	protected VideoCanvas canvas;
	protected FrameGrabber grabber = null;
	protected String sampleName;
	protected ColoredStickersFeatureExtractor stickersExtractor;
	protected Thread videoGrabber;
	protected AtomicBoolean threadStop;

	/**
	 * Create the panel.
	 * @throws com.googlecode.javacv.FrameGrabber.Exception 
	 */
	public VideoCapturePanel() {
		stickersExtractor = new ColoredStickersFeatureExtractor();
		canvas = new VideoCanvas();
		canvas.setBackground(Color.LIGHT_GRAY);

		canvas.setBounds(129, 10, 456, 362);
		this.add(canvas);
		canvas.setVisible(true);
		canvas.createBufferStrategy(1);

		threadStop = new AtomicBoolean(true);
	}

	public void startVideo() throws Exception {
		if (grabber == null)
		{
			try{
				grabber = stickersExtractor.getGrabber(videoInput);
			}catch (Exception e){
				if(grabber != null){
					try {
						grabber.stop();
					} catch (com.googlecode.javacv.FrameGrabber.Exception e1) {
						e1.printStackTrace();
					}
				}
				grabber = null;
				if(e instanceof UnknownHostException){
					JOptionPane.showMessageDialog(this,
							"Could not reach host " + e.getMessage()
							+ "\nplease check the URL and your Internet connection",
							"Inane warning",
							JOptionPane.WARNING_MESSAGE);
				}
				throw e;
			}
		}
		try{
			synchronized (threadStop) {
				grabber.start();
			}
		} catch (Exception e){
			if(e.getMessage().contains("Could not setup device")){
				JOptionPane.showMessageDialog(this,
						"This feature only works with a camera",
						"Inane warning",
						JOptionPane.WARNING_MESSAGE);
				throw e;
			}
		}
		threadStop.set(false);
		videoGrabber = new Thread(new Runnable()
		{

			public void run()
			{
				try {
					getVideoFromSource();
				} catch (com.googlecode.javacv.FrameGrabber.Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		videoGrabber.start();
	}


	public void stopVideo(){
		synchronized (threadStop) {
			threadStop.set(true);
			try {
				if (grabber != null){
					grabber.stop();
				}
			} catch (com.googlecode.javacv.FrameGrabber.Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	protected void getVideoFromSource() throws com.googlecode.javacv.FrameGrabber.Exception {

		IplImage grabbed;		
		while(!threadStop.get() && (grabbed = grabber.grab()) != null){
			image = grabbed.getBufferedImage();
			canvas.setImage(image);
			canvas.paint(null);
		}
	}

	public void initGrabber() throws MalformedURLException, IOException, Exception{
		grabber = stickersExtractor.getGrabber(videoInput);
	}



	public void setVideoInput(String videoInput){
		this.videoInput = videoInput;
	}


}
