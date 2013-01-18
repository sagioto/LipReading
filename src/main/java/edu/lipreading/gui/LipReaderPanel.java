package edu.lipreading.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

import edu.lipreading.Constants;
import edu.lipreading.Sample;
import edu.lipreading.TrainingSet;
import edu.lipreading.Utils;
import edu.lipreading.classification.Classifier;
import edu.lipreading.classification.TimeWarperClassifier;
import edu.lipreading.normalization.CenterNormalizer;
import edu.lipreading.normalization.Normalizer;

public class LipReaderPanel extends VideoCapturePanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private JLabel lblOutput;
    private boolean recording;
    private JButton btnRecord;
    private Sample recordedSample;

    /**
     * Create the panel.
     * @throws com.googlecode.javacv.FrameGrabber.Exception 
     */
    public LipReaderPanel() {
        super();

        canvas.setBackground(UIManager.getColor("InternalFrame.inactiveTitleGradient"));
        setBackground(Color.WHITE);
        setLayout(null);

        recording = false;

        btnRecord = new JButton("");
        btnRecord.setIcon(new ImageIcon(getClass().getResource(Constants.RECORD_IMAGE_FILE_PATH)));

        btnRecord.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if (!recording) // Button should start recording
                {
                    btnRecord.setIcon(new ImageIcon(getClass().getResource(Constants.STOP_IMAGE_FILE_PATH)));

                    recordedSample = new Sample("web cam " + new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(new Date()));
                    lblOutput.setText("");
                    recording = true;
                }
                else // Button should stop recording
                {
                    recording = false;
                    btnRecord.setIcon(new ImageIcon(getClass().getResource(Constants.RECORD_IMAGE_FILE_PATH)));
                    //btnRecord.setText("Record");

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
            image = grabbed.getBufferedImage();
            canvas.setImage(image);
            canvas.paint(null);
            if (recording)
            {
                try {
                    recordedSample.getMatrix().add(stickersExtractor.getPoints(grabbed));
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }



}
