package edu.lipreading.gui;

import com.googlecode.javacv.FFmpegFrameRecorder;
import com.googlecode.javacv.cpp.avutil;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import edu.lipreading.Constants;
import edu.lipreading.Sample;
import edu.lipreading.TrainingSet;
import edu.lipreading.Utils;
import edu.lipreading.classification.Classifier;
import edu.lipreading.classification.TimeWarperClassifier;
import edu.lipreading.normalization.CenterNormalizer;
import edu.lipreading.normalization.Normalizer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Beans;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.googlecode.javacv.cpp.opencv_core.cvFlip;

public class LipReaderPanel extends VideoCapturePanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    protected JLabel lblOutput;
    private boolean recording;
    protected JButton btnRecord;
    protected Sample recordedSample;
    private String sampleName; 
    private String label;
    protected String recordedVideoFilePath;
	protected FFmpegFrameRecorder recorder = null;
	private boolean recordToFile = false;
	protected String videoFilePath; 
    
    /**
     * Create the panel.
     * @throws com.googlecode.javacv.FrameGrabber.Exception 
     */
    public LipReaderPanel() {
        super();
        
        setSampleName("web cam", null);
        
        canvas.setBackground(UIManager.getColor("InternalFrame.inactiveTitleGradient"));
        setBackground(Color.WHITE);
        setLayout(null);

        recording = false;

        btnRecord = new JButton("");
        
        if (!Beans.isDesignTime())
        	btnRecord.setIcon(new ImageIcon(getClass().getResource(Constants.RECORD_IMAGE_FILE_PATH)));

        btnRecord.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if (!recording) // Button should start recording
                {
                    btnRecord.setIcon(new ImageIcon(getClass().getResource(Constants.STOP_IMAGE_FILE_PATH)));
                    String sampleId = getSampleName() + " " + new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(new Date());
                    recordedSample = new Sample(sampleId);
                    recordedSample.setLabel(label);
                    
                    
                    if (recordedVideoFilePath != null && !recordedVideoFilePath.isEmpty()){
                    	recorder = null;
                    	setVideoFilePath(recordedVideoFilePath, sampleId.replaceAll("[:/]", "."));//TODO Change
                    	setRecordingToFile(true);
                    }
                    
                    lblOutput.setText("");
                    recording = true;
                }
                else // Button should stop recording
                {
                    recording = false;
                    
                    btnRecord.setIcon(new ImageIcon(getClass().getResource(Constants.RECORD_IMAGE_FILE_PATH)));
                    
                    // Stop saving video file
                    if (isRecordingToFile())
                    {
	                    recordedVideoFilePath = "";
	                    //stopRecordingVideo();
	                    setRecordingToFile(false);
	                    /*try {
							recorder.stop();
						} catch (com.googlecode.javacv.FrameRecorder.Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						*/
                    }
                    
                    handleRecordedSample();
                }
            }
        });
        btnRecord.setBorder(BorderFactory.createEmptyBorder());
        btnRecord.setBorderPainted(false);
        btnRecord.setBounds(332, 382, 50, 48);
        this.add(btnRecord);

        lblOutput = new JLabel("Output Label");
        lblOutput.setHorizontalAlignment(SwingConstants.CENTER);
        lblOutput.setFont(new Font("Tahoma", Font.PLAIN, 18));
        lblOutput.setForeground(Color.GRAY);
        lblOutput.setBounds(303, 452, 102, 22);
        this.add(lblOutput);



        canvas.setBounds(129, 10, 456, 362);


    }

    @Override
    protected void getVideoFromSource() throws com.googlecode.javacv.FrameGrabber.Exception {
        IplImage grabbed;
        while(!threadStop.get()){
            synchronized (threadStop) {
                if((grabbed = grabber.grab()) == null)
                    break;
            }
            List<Integer> points = null;
            try {
                points = stickersExtractor.getPoints(grabbed);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            if(points != null){
                stickersExtractor.paintCoordinates(grabbed, points);
            }
            cvFlip(grabbed, grabbed, 1);
            image = grabbed.getBufferedImage();
            canvas.setImage(image);
            canvas.paint(null);
            if(points == null){
                continue;
            }//not sure about this

            if (recording)
            {
                recordedSample.getMatrix().add(points);
                if (isRecordingToFile()){
                	// The following recorder part is ugly, but necessary to be on one scope. 
                	// Otherwise EXCEPTION_ACCESS_VIOLATION might occur. 
                	boolean errorOccurred = false;
                	if (recorder == null){
                		File videoFile = new File(videoFilePath);
                		try {
                			videoFile.createNewFile();
                		} catch (IOException e1) {
                			errorOccurred = true;
                			// TODO Auto-generated catch block
                			e1.printStackTrace();
                		}
                		recorder = new FFmpegFrameRecorder(videoFile,  grabber.getImageWidth(),grabber.getImageHeight());
                		recorder.setVideoCodec(13);
                        recorder.setFormat("MOV");
                        recorder.setPixelFormat(avutil.PIX_FMT_YUV420P);
                        recorder.setFrameRate(30);
                        
                        try {
                			recorder.start();
                	        setRecordingToFile(true);
                		} catch (com.googlecode.javacv.FrameRecorder.Exception e) {
                			errorOccurred = true;
                			JOptionPane.showMessageDialog(this,
                					"Can not record and save video file: " + e.getMessage(),
                					"Recording Video File Error",
                					JOptionPane.ERROR_MESSAGE);
                		}
                        
                        recordedSample.setHeight(grabber.getImageHeight());
                        recordedSample.setWidth(grabber.getImageWidth());
                	}
                	try {
                		if (!errorOccurred)
                		recorder.record(grabbed);
    				} catch (com.googlecode.javacv.FrameRecorder.Exception e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
                }
                
            }
            else{
            	if (recorder != null && !isRecordingToFile()){ //TODO Fix
        			try {
    					recorder.stop();
    					recorder = null;
    				} catch (com.googlecode.javacv.FrameRecorder.Exception e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
        		}
            }
        }
    }

    protected void handleRecordedSample() {
		//TODO - Extract to thread:
		List<Sample> trainingSet;
		try {
		    trainingSet = TrainingSet.get();
		    Normalizer normalizer = new CenterNormalizer();
		    Classifier classifier = new TimeWarperClassifier();
		    classifier.train(trainingSet);
		    final String outputText = classifier.test(normalizer.normalize(recordedSample));
		    lblOutput.setText(outputText);
		    new Thread(new Runnable() {
		        @Override
		        public void run() {
		            try {
		                Utils.textToSpeech(outputText);
		            } catch (Exception e) {
		                throw new RuntimeException(e);
		            }
		        }
		    }).start();
		    lblOutput.setText(outputText);
		} 
		catch (Exception e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	}

	protected String getSampleName() {
		return sampleName;
	}

	protected void setSampleName(String sampleName, String label) {
		this.sampleName = sampleName;
		this.label = label;
	}
	
	protected boolean isRecording() {
		return recording;
	}
	
	
	protected void setVideoFilePath(String folderPath, String fileName){
		File folder = new File(folderPath);
		if (!folder.exists()) 
			folder.mkdirs();
		videoFilePath = folder.getAbsolutePath() + "\\" + fileName + ".MOV"; //TODO Extract file type to properties file
	}

	public void stopRecordingVideo(){
		if (isRecordingToFile()){
			try {
				recorder.stop();
				setRecordingToFile(false);
			} catch (com.googlecode.javacv.FrameRecorder.Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
		stopRecordingVideo();
	}
	
	protected boolean isRecordingToFile() {
		return recordToFile;
	}

	protected void setRecordingToFile(boolean recordToFile) {
		this.recordToFile = recordToFile;
	}
}
