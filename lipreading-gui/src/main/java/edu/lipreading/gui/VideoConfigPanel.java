package edu.lipreading.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static java.awt.Color.WHITE;

public class VideoConfigPanel extends JPanel {
    private StickersConfigPanel stickersConfigPanel;
    private JRadioButton rdbtnColoredStickersBased;
    private ConfigEvent actionListener;


    public VideoConfigPanel() {
        setBackground(WHITE);
        setLayout(null);

        JLabel lblNewLabel = new JLabel("Choose operation mode:");
        lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
        lblNewLabel.setBounds(10, 11, 213, 14);
        lblNewLabel.setHorizontalAlignment(SwingConstants.LEFT);
        add(lblNewLabel);

        JRadioButton rdbtnWorkLocally = new JRadioButton("Work locally");
        rdbtnWorkLocally.setBackground(WHITE);
        rdbtnWorkLocally.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                //TODO
            }
        });
        rdbtnWorkLocally.setBounds(10, 32, 268, 23);
        add(rdbtnWorkLocally);
        rdbtnWorkLocally.setSelected(true);

        JRadioButton rdbtnWorkWithServer = new JRadioButton("Work with remote server (requiers fast internet connection)");
        rdbtnWorkWithServer.setSelected(true);
        rdbtnWorkWithServer.setBackground(WHITE);
        rdbtnWorkWithServer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //TODO
            }
        });
        rdbtnWorkWithServer.setBounds(289, 32, 332, 23);
        rdbtnWorkWithServer.setEnabled(false);
        add(rdbtnWorkWithServer);

        ButtonGroup rBtnGroup = new ButtonGroup();
        rBtnGroup.add(rdbtnWorkLocally);
        rBtnGroup.add(rdbtnWorkWithServer);

        JSeparator separator = new JSeparator();
        separator.setBounds(0, 67, 708, 2);
        add(separator);

        JLabel lblVideoLipIdentification = new JLabel("Video lip detection mode:");
        lblVideoLipIdentification.setFont(new Font("Tahoma", Font.BOLD, 11));
        lblVideoLipIdentification.setHorizontalAlignment(SwingConstants.LEFT);
        lblVideoLipIdentification.setBounds(10, 76, 194, 14);
        add(lblVideoLipIdentification);

        JRadioButton rdbtnAutoLipIdentification = new JRadioButton("Auto lip detection");
        rdbtnAutoLipIdentification.setSelected(true);
        rdbtnAutoLipIdentification.setBackground(WHITE);
        rdbtnAutoLipIdentification.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stickersConfigPanel.setFeatureExtractor(edu.lipreading.gui.Constants.NO_STICKERS_FE);
                actionListener.changeSettings(edu.lipreading.gui.Constants.NO_STICKERS_FE);
                try {
                    stickersConfigPanel.stopVideo();
                } catch (Exception e1) {
                    e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                stickersConfigPanel.setVisible(false);
            }
        });
        rdbtnAutoLipIdentification.setBounds(10, 97, 268, 23);
        add(rdbtnAutoLipIdentification);

        rdbtnColoredStickersBased = new JRadioButton("Colored stickers based lip detection");
        rdbtnColoredStickersBased.setBackground(WHITE);
        rdbtnColoredStickersBased.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stickersConfigPanel.setFeatureExtractor(edu.lipreading.gui.Constants.STICKERS_FE);
                actionListener.changeSettings(edu.lipreading.gui.Constants.STICKERS_FE);
                stickersConfigPanel.setVisible(true);
                try {
                    stickersConfigPanel.startVideo();
                } catch (Exception e1) {
                    e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        });
        rdbtnColoredStickersBased.setBounds(289, 97, 332, 23);
        add(rdbtnColoredStickersBased);

        ButtonGroup rBtnGroup2= new ButtonGroup();
        rBtnGroup2.add(rdbtnAutoLipIdentification);
        rBtnGroup2.add(rdbtnColoredStickersBased);

        JSeparator separator2 = new JSeparator();
        separator2.setBounds(0, 125, 708, 2);
        add(separator2);

        stickersConfigPanel = new StickersConfigPanel();
        stickersConfigPanel.setBounds(0, 130, 708, 398);
        add(stickersConfigPanel);
        stickersConfigPanel.setVisible(false);

    }

    public void stopVideo() {
        if (rdbtnColoredStickersBased.isSelected()){
            try {
                stickersConfigPanel.stopVideo();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //To change body of created methods use File | Settings | File Templates.
    }

    public void startVideo() {
        if (rdbtnColoredStickersBased.isSelected()){
            try {
                stickersConfigPanel.startVideo();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //To change body of created methods use File | Settings | File Templates.
    }


    // Listener interface
    public interface ConfigEvent {
        void changeSettings(int s);
    }

    public void addActionEventListener(ConfigEvent listener)
    {
        actionListener = listener;
    }
}
