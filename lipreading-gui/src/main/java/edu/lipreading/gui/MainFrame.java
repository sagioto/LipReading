package edu.lipreading.gui;

import edu.lipreading.classification.Classifier;
import edu.lipreading.classification.SVMClassifier;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
public class MainFrame extends JFrame {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private LipReaderPanel lipReaderPanel;
    //private StickersConfigPanel videoConfigPanel;
    private VideoConfigPanel videoConfigPanel;
    private FileLipReaderPanel fileLipReaderPanel;
    private TrainingPanel trainingPanel;

    protected static Classifier classifier;

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
        JPanel contentPane = new JPanel();
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
        tabbedPane.setUI(new ModernTabbedPaneUI(716 / 4));
        tabbedPane.setBounds(0, 88, 716, 537);
        fileLipReaderPanel = new FileLipReaderPanel();
        lipReaderPanel = new LipReaderPanel();
        videoConfigPanel = new VideoConfigPanel();
        VideoConfigPanel.ConfigEvent event = new Event();
        videoConfigPanel.addActionEventListener(event);
        tabbedPane.addTab("Read From File", null, fileLipReaderPanel, "Read from a file in the file system or a URL");
        tabbedPane.addTab("Live Read", null, lipReaderPanel, "Read from the camera");
        tabbedPane.addTab("Configurations", null, videoConfigPanel, "Set application configurations");

        trainingPanel = new TrainingPanel();
        tabbedPane.addTab("Training", null, trainingPanel, "Train the reader");

        tabbedPane.addChangeListener(new ChangeListener(){

            @Override
            public void stateChanged(ChangeEvent e) {
                switch(tabbedPane.getSelectedIndex()){
                    case 0: //file
                        try{
                            videoConfigPanel.stopVideo();
                            lipReaderPanel.stopVideo();
                            trainingPanel.stopVideo();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        break;
                    case 1: //camera
                        try {
                            fileLipReaderPanel.stopVideo();
                            videoConfigPanel.stopVideo();
                            trainingPanel.stopVideo();
                            lipReaderPanel.startVideo();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        break;
                    case 2: //configuration
                        try {
                            fileLipReaderPanel.stopVideo();
                            lipReaderPanel.stopVideo();
                            trainingPanel.stopVideo();
                            videoConfigPanel.startVideo();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        break;
                    case 3://training
                        try {
                            fileLipReaderPanel.stopVideo();
                            lipReaderPanel.stopVideo();
                            videoConfigPanel.stopVideo();
                            trainingPanel.startVideo();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        break;
                }
            }

        });
        contentPane.add(tabbedPane);
    }

    public static Classifier getClassifier() {
        if(classifier == null) {
            System.out.println("Initializing classifier");
            String model = Constants.CLASSIFIER_MODEL;
            System.out.println("Model used: " + model);
            try {
                classifier = new SVMClassifier(model);
                System.out.println("Classifier Initialized successfully from model");
            } catch (Exception e) {
                System.out.println("Error when initializing classifier");
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return classifier;
    }


    public class Event implements VideoConfigPanel.ConfigEvent{

        @Override
        public void changeSettings(int fe) {
            System.out.println("Event" + fe);
            //fileLipReaderPanel
            lipReaderPanel.setFeatureExtractor(fe);
            trainingPanel.setFeatureExtractor(fe);
            fileLipReaderPanel.setFeatureExtractor(fe);

        }
    }

}
