package edu.lipreading.gui;

import com.googlecode.javacv.cpp.opencv_core.IplImage;
import edu.lipreading.Sample;
import edu.lipreading.Utils;
import weka.core.xml.XStream;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

/**
 * @author Dor Leitman
 */

public class FileLipReaderPanel extends VideoCapturePanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    final JFileChooser fileChooser = new JFileChooser();
    private JLabel lblOutput;
    private JButton btnRecord;
    private JTextPane txtFilePath;
    private Sample recordedSample;
    private Thread classifierThread;
    private String[] videoExtensions = {".mov", ".mpeg", ".mpg", ".wmv", ".mp4", ".3gp", ".avi", ".mkv"};

    /**
     * Create the panel.
     */
    public FileLipReaderPanel() {
        super();
        canvas.setBackground(UIManager.getColor("InternalFrame.inactiveTitleGradient"));
        setBackground(Color.WHITE);
        setLayout(null);

        fileChooser.addChoosableFileFilter(new CustomFileFilter("Video files", true, videoExtensions));
        fileChooser.addChoosableFileFilter(new CustomFileFilter("XML Sample files", true, ".xml"));

        lblOutput = new JLabel("");
        lblOutput.setHorizontalAlignment(SwingConstants.CENTER);
        lblOutput.setFont(new Font("Tahoma", Font.PLAIN, 18));
        lblOutput.setForeground(Color.GRAY);
        lblOutput.setBounds(244, 447, 204, 22);
        this.add(lblOutput);

        canvas.setBounds(142, 10, 420, 308);

        txtFilePath = new JTextPane();
        txtFilePath.setToolTipText("Please insert a URL or choose a file");
        txtFilePath.setBackground(SystemColor.info);
        txtFilePath.setText("https://dl.dropbox.com/u/8720454/set2/no/no-1.MOV"); //TODO - Change default
        txtFilePath.setBounds(204, 337, 320, 20);
        add(txtFilePath);

        final String recordButtonText = "Read Lips From File";
        btnRecord = new JButton(recordButtonText);
        btnRecord.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {

                btnRecord.setEnabled(false);
                btnRecord.setText("Downloading File...");
                lblOutput.setText("");

                Thread videoGrabberThread = new Thread(new Runnable() {
                    public void run() {
                        if (!txtFilePath.getText().endsWith(".xml")) {
                            setVideoInput(txtFilePath.getText());
                            try {
                                initGrabber();
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            recordedSample = new Sample(txtFilePath.getText());


                            try {
                                startVideo();
                            } catch (Exception e) {
                                btnRecord.setEnabled(true);
                                btnRecord.setText(recordButtonText);
                            }
                        } else {
                            try {
                                recordedSample = (Sample) XStream.read(txtFilePath.getText());
                                classifierThread = new ClassifierThread();
                                classifierThread.start();
                            } catch (Exception e) {
                                btnRecord.setEnabled(true);
                                btnRecord.setText(recordButtonText);
                                System.out.println("Error parsing XML");
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }
                        }
                    }
                });
                videoGrabberThread.start();
            }
        });
        btnRecord.setBackground(Color.WHITE);
        btnRecord.setBounds(275, 387, 143, 23);
        this.add(btnRecord);

        JLabel lblNewLabel = new JLabel("File Path:");
        lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
        lblNewLabel.setBounds(142, 339, 53, 14);
        add(lblNewLabel);

        JButton btnChooseFile = new JButton(new ImageIcon(getClass().getResource(edu.lipreading.gui.Constants.FILE_CHOOSER_IMAGE_FILE_PATH)));
        btnChooseFile.setBorderPainted(false);
        btnChooseFile.setBackground(Color.WHITE);
        btnChooseFile.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                int returnVal = fileChooser.showOpenDialog(FileLipReaderPanel.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    txtFilePath.setText(fileChooser.getSelectedFile().getPath());
                    progressBar.setVisible(false);
                }
            }
        });
        btnChooseFile.setBounds(530, 333, 32, 32);
        add(btnChooseFile);

        progressBar.setBounds(0, 494, 715, 18);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        add(progressBar);
    }

    @Override
    protected void getVideoFromSource() throws Exception {
        try {
            IplImage grabbed;

            while ((grabbed = grabber.grab()) != null && !threadStop.get()) {
                image = grabbed.getBufferedImage();

                // Eyes detection:
                if (recordedSample.getLeftEye() == null || recordedSample.getRightEye() == null) {
                    List<Integer> eyesCoordinates = eyesFeatureExtractor.getPoints(grabbed);
                    if (eyesCoordinates != null) { // If eyes were found
                        recordedSample.setLeftEye(new Point(eyesCoordinates.get(0), eyesCoordinates.get(1)));
                        recordedSample.setRightEye(new Point(eyesCoordinates.get(2), eyesCoordinates.get(3)));
                        eyesFeatureExtractor.paintCoordinates(grabbed, eyesCoordinates);
                    }
                }

                canvas.setImage(image);
                canvas.paint(null);
                recordedSample.getMatrix().add(featureExtractor.getPoints(grabbed));
            }
            stopVideo();
            canvas.setImage(null);
            canvas.paint(null);


            classifierThread.start();

        } catch (com.googlecode.javacv.FrameGrabber.Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            grabber.stop();
        }
    }

    class CustomFileFilter extends FileFilter {
        private String description = "";
        private String[] extensions;
        private boolean directories = false;

        public CustomFileFilter(String description, boolean directories, String... extensions) {
            this.description = description;
            this.extensions = extensions;
            this.directories = directories;
        }

        public boolean accept(File file) {


            String filename = file.getName().toLowerCase();
            if(file.isDirectory() && directories) {
                return true;
            }
            for(String extension : extensions) {
                if(filename.endsWith(extension)) {
                    return true;
                }
            }
            return false;
        }

        public String getDescription() {
            return description;
        }
    }

    private class ClassifierThread extends Thread {
        public ClassifierThread() {
            super(new Runnable() {
                public void run() {
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
                    btnRecord.setText("Read Lips From File");
                    btnRecord.setEnabled(true);
                }
            });
        }
    }
}
