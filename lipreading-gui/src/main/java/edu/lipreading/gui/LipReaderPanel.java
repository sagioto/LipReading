package edu.lipreading.gui;

import com.googlecode.javacv.FFmpegFrameRecorder;
import com.googlecode.javacv.cpp.avutil;
import edu.lipreading.Sample;
import edu.lipreading.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Beans;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.googlecode.javacv.cpp.opencv_core.*;

public class LipReaderPanel extends VideoCapturePanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    protected JLabel lblOutput;
    protected boolean recording;
    protected JButton btnRecord;
    protected JButton btnCancelRecord;
    protected Sample recordedSample;
    protected String recordedVideoFilePath;
    protected FFmpegFrameRecorder recorder = null;
    protected String videoFilePath;
    protected boolean showLipsIdentification = true;
    private String sampleName;
    private String label;
    private boolean recordToFile = false;
    private boolean cancelSaveToFile = false;

    /**
     * Create the panel.
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


                    if (recordedVideoFilePath != null && !recordedVideoFilePath.isEmpty()) {
                        recorder = null;
                        setVideoFilePath(recordedVideoFilePath, sampleId.replaceAll("[:/]", "."));//TODO Change
                        setRecordingToFile(true);
                        setCancelSaveToFile(false);
                    }

                    lblOutput.setText("");
                    recording = true;
                    btnCancelRecord.setVisible(true);
                } else // Button should stop recording
                {
                    recording = false;

                    btnRecord.setIcon(new ImageIcon(getClass().getResource(Constants.RECORD_IMAGE_FILE_PATH)));

                    // Stop saving video file
                    if (isRecordingToFile()) {
                        recordedVideoFilePath = "";
                        setRecordingToFile(false);
                    }

                    handleRecordedSample();
                }
            }
        });
        btnRecord.setBorder(BorderFactory.createEmptyBorder());
        btnRecord.setBorderPainted(false);
        btnRecord.setBounds(332, 382, 50, 48);
        this.add(btnRecord);

        btnCancelRecord = new JButton("");
        btnCancelRecord.setBounds(495, 403, 12, 12);

        if (!Beans.isDesignTime())
            btnCancelRecord.setIcon(new ImageIcon(getClass().getResource(edu.lipreading.gui.Constants.CANCEL_IMAGE_FILE_PATH)));
        btnCancelRecord.setVisible(false);

        lblOutput = new JLabel("Output Label");
        lblOutput.setHorizontalAlignment(SwingConstants.CENTER);
        lblOutput.setFont(new Font("Tahoma", Font.PLAIN, 18));
        lblOutput.setForeground(Color.GRAY);
        lblOutput.setBounds(253, 452, 200, 22);
        this.add(lblOutput);


        canvas.setBounds(129, 10, 456, 362);


    }

    @Override
    protected void getVideoFromSource() throws Exception {
        IplImage grabbed;
        while (!threadStop.get()) {
            synchronized (threadStop) {
                if ((grabbed = grabber.grab()) == null) {
                    break;
                }
            }

            List<Integer> points = featureExtractor.getPoints(grabbed);
            if (recording) {
                List<Integer> eyesCoordinates = null;
                if (recordedSample.getLeftEye() == null || recordedSample.getRightEye() == null) {
                    //when initializing eyes location set resolution fields too
                    recordedSample.setHeight(grabber.getImageHeight());
                    recordedSample.setWidth(grabber.getImageWidth());
                    eyesCoordinates = eyesFeatureExtractor.getPoints(grabbed);
                    if (eyesCoordinates != null) { // If eyes were found
                        recordedSample.setLeftEye(new Point(eyesCoordinates.get(0), eyesCoordinates.get(1)));
                        recordedSample.setRightEye(new Point(eyesCoordinates.get(2), eyesCoordinates.get(3)));
                    }
                }
                if (points != null) {
                    recordedSample.getMatrix().add(points);
                }
                if (isRecordingToFile()) {
                    if (recorder == null) {
                        File videoFile = new File(videoFilePath);
                        videoFile.createNewFile();
                        recorder = new FFmpegFrameRecorder(videoFile, grabber.getImageWidth(), grabber.getImageHeight());
                        recorder.setVideoCodec(13);
                        recorder.setFormat("MOV");
                        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
                        recorder.setFrameRate(30);

                        try {
                            recorder.start();
                            setRecordingToFile(true);
                        } catch (com.googlecode.javacv.FrameRecorder.Exception e) {
                            JOptionPane.showMessageDialog(this,
                                    "Can not record and save video file: " + e.getMessage(),
                                    "Recording Video File Error",
                                    JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                    recorder.record(grabbed);
                }
                if (eyesCoordinates != null && showLipsIdentification) {
                    eyesFeatureExtractor.paintCoordinates(grabbed, eyesCoordinates);
                }
            } else if (recorder != null && !isRecordingToFile()) {
                recorder.stop();
                recorder = null;
                if (cancelSaveToFile) {
                    File file = new File(videoFilePath);
                    file.delete();
                }
            }
            if ((points != null) && showLipsIdentification) {
                featureExtractor.paintCoordinates(grabbed, points);
            }
            cvFlip(grabbed, grabbed, 1);
            if (recording)
                cvCircle(grabbed, new CvPoint(20, 20), 8, CvScalar.RED, -1, 1, 0);
            image = grabbed.getBufferedImage();
            canvas.setImage(image);
            canvas.paint(null);
        }
    }

    protected void handleRecordedSample() {
        //TODO - Extract to thread:
        try {
            final String outputText = classify(recordedSample);
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
        } catch (Exception e) {
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

    protected void setVideoFilePath(String folderPath, String fileName) {
        File folder = new File(folderPath);
        if (!folder.exists())
            folder.mkdirs();
        String fileNameNoSpaces = fileName.replace(' ', '-');
        videoFilePath = (folder.getAbsolutePath() + "/" + fileNameNoSpaces + ".MOV"); //TODO Extract file type to properties file
    }

    public void stopRecordingVideo() {
        if (isRecordingToFile()) {
            try {
                recorder.stop();
                setRecordingToFile(false);
            } catch (com.googlecode.javacv.FrameRecorder.Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void stopVideo() {
        synchronized (threadStop) {
            threadStop.set(true);
            try {
                if (grabber != null) {
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

    public void setCancelSaveToFile(boolean toSaveInFile) {
        this.cancelSaveToFile = toSaveInFile;
    }
}
