package edu.lipreading.gui;

import edu.lipreading.Constants;
import edu.lipreading.Sample;
import edu.lipreading.TrainingSet;
import edu.lipreading.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

public class TrainingPanel extends LipReaderPanel {

	private static final long serialVersionUID = -5713175015110844830L;
	private final JComboBox<String> chooseLabel;
	private String label;
	private final Map<String, AtomicInteger> counters = new HashMap<String, AtomicInteger>();
	private JTextField txtInstancesNum;
	private JTextField txtPath;
	private int totalNumOfInstances;
	private JButton btnCreateTrainingSet;
	private int currentInstanceNum;
	final JFileChooser fileChooser = new JFileChooser();
    private JLabel lblSampleCurrentNum;
	private JLabel lblSampleName;
	private JButton btnChooseFile;
	private JLabel lblRecordedSample;
	private JLabel lblRecordsLeft;
	private JCheckBox chckbxSaveVideo;
	private JCheckBox chckbxShowLips;

	public TrainingPanel() {
		super();
		btnRecord.setEnabled(false);

		btnRecord.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (!isRecording()){ // If Start Recording
					setSampleName(getLabel() + (counters.get(getLabel().toLowerCase()).addAndGet(1)), getLabel());
					if (chckbxSaveVideo.isSelected())
						recordedVideoFilePath = txtPath.getText() + "/Videos";
				}
                else {
                    btnCancelRecord.setVisible(false);
                }

			}
		});
		canvas.setBounds(163, 142, 345, 255);
		btnRecord.setBounds(327, 403, 45, 45);
		remove(lblOutput);

        btnCancelRecord.addActionListener((new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                recording = false;

                btnRecord.setIcon(new ImageIcon(getClass().getResource(edu.lipreading.gui.Constants.RECORD_IMAGE_FILE_PATH)));

                // Stop saving video file
                if (isRecordingToFile())
                {
                    recordedVideoFilePath = "";
                    setCancelSaveToFile(true);
                    setRecordingToFile(false);
                }
                counters.get(getLabel().toLowerCase()).getAndDecrement();
                btnCancelRecord.setVisible(false);

            }
        }));
        btnCancelRecord.setToolTipText("Ignore this recorded video");
        add(btnCancelRecord);

        chooseLabel = new JComboBox<String>();
		chooseLabel.setMaximumRowCount(6);
		for (String word : Constants.VOCABULARY) {
			chooseLabel.addItem(word.substring(0, 1).toUpperCase() + word.substring(1, word.length()));
			counters.put(word, new AtomicInteger(0));
		}

		for(Sample sample : TrainingSet.get()){
			for (String word : Constants.VOCABULARY) {
				if (sample.getId().toLowerCase().contains(word.toLowerCase()))
					counters.get(word).incrementAndGet();
			}
		}

		setLabel((String)chooseLabel.getSelectedItem());
		chooseLabel.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent event) {
				setLabel((String)chooseLabel.getSelectedItem());
			}
		});
		chooseLabel.setToolTipText("Choose a label for the recorded sample");
		chooseLabel.setBounds(309, 11, 247, 20);
		add(chooseLabel);

		txtInstancesNum = new JTextField();
		txtInstancesNum.setBounds(248, 42, 86, 20);
		add(txtInstancesNum);
		txtInstancesNum.setColumns(10);

		JLabel lblNumberOfInstances = new JLabel("Number of instances:");
		lblNumberOfInstances.setBounds(136, 45, 113, 14);
		add(lblNumberOfInstances);

		txtPath = new JTextField();
		txtPath.setBounds(212, 71, 302, 20);
		add(txtPath);
		txtPath.setColumns(10);

		btnCreateTrainingSet = new JButton("Create Training Set");
		btnCreateTrainingSet.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                boolean error = false;
                try {
                    setTotalNumOfInstances(Integer.parseInt(txtInstancesNum.getText()));
                } catch (NumberFormatException ex) {
                    error = true;
                    JOptionPane.showMessageDialog(TrainingPanel.this,
                            "Number of instances requires a number",
                            "Wrong input",
                            JOptionPane.WARNING_MESSAGE);
                }
                if (txtPath.getText().isEmpty()) {
                    error = true;
                    JOptionPane.showMessageDialog(TrainingPanel.this,
                            "Please choose a folder to save the recorded Training Set",
                            "Wrong input",
                            JOptionPane.WARNING_MESSAGE);
                }


                if (!error) {
                    setLabel((String) chooseLabel.getSelectedItem());
                    currentInstanceNum = 0;
                    enableTrainingSetParams(false);
                }
            }
        });
		btnCreateTrainingSet.setBounds(425, 113, 131, 23);
		add(btnCreateTrainingSet);

		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		btnChooseFile = new JButton(new ImageIcon(getClass().getResource(edu.lipreading.gui.Constants.FILE_CHOOSER_IMAGE_FILE_PATH)));
		btnChooseFile.setBorderPainted(false);
		btnChooseFile.setBackground(Color.WHITE);
		btnChooseFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int returnVal = fileChooser.showOpenDialog(TrainingPanel.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    txtPath.setText(fileChooser.getSelectedFile().getPath());
                }
            }
        });
		btnChooseFile.setBounds(524, 61, 32, 32);
		add(btnChooseFile);

        JLabel lblChooseWordsentenceTo = new JLabel("Choose Word/Sentence to record:");
		lblChooseWordsentenceTo.setBounds(136, 14, 165, 14);
		add(lblChooseWordsentenceTo);

        JLabel lblPathToSave = new JLabel("Path to save:");
		lblPathToSave.setBounds(136, 76, 113, 14);
		add(lblPathToSave);

		lblRecordedSample = new JLabel("Recorded Sample:");
		lblRecordedSample.setForeground(Color.GRAY);
		lblRecordedSample.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblRecordedSample.setBounds(136, 459, 123, 14);
		add(lblRecordedSample);

		lblSampleName = new JLabel("Recorded Sample:");
		lblSampleName.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblSampleName.setForeground(Color.DARK_GRAY);
		lblSampleName.setBounds(248, 459, 308, 14);
		add(lblSampleName);

		lblRecordsLeft = new JLabel("Left to record:");
		lblRecordsLeft.setForeground(Color.GRAY);
		lblRecordsLeft.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblRecordsLeft.setBounds(136, 484, 95, 14);
		add(lblRecordsLeft);

		lblSampleCurrentNum = new JLabel("num");
		lblSampleCurrentNum.setForeground(Color.DARK_GRAY);
		lblSampleCurrentNum.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblSampleCurrentNum.setBounds(231, 484, 45, 14);
		add(lblSampleCurrentNum);

		chckbxSaveVideo = new JCheckBox("Save To Video File");
		chckbxSaveVideo.setBackground(Color.WHITE);
		chckbxSaveVideo.setSelected(true);
		chckbxSaveVideo.setBounds(136, 98, 123, 23);
		add(chckbxSaveVideo);
		
		chckbxShowLips = new JCheckBox("Show Lips Identification");
		chckbxShowLips.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				showLipsIdentification = chckbxShowLips.isSelected();
			}
		});
		chckbxShowLips.setSelected(true);
		chckbxShowLips.setBackground(Color.WHITE);
		chckbxShowLips.setBounds(262, 98, 143, 23);
		add(chckbxShowLips);

		lblSampleCurrentNum.setVisible(false);
		lblSampleName.setVisible(false);
		lblRecordsLeft.setVisible(false);
		lblRecordedSample.setVisible(false);

	}

	@Override
	protected void handleRecordedSample() {
		currentInstanceNum++;

		if (currentInstanceNum == totalNumOfInstances)
		{
			enableTrainingSetParams(true);
		}

		try {

			String fileName =  recordedSample.getId().replaceAll("[:/]", ".") + ".xml";
			Utils.writeSampleToXML(txtPath.getText() ,fileName.replace(' ','-'), recordedSample);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(TrainingPanel.this,
					"Cannot save file. Reason:" + e.getMessage(),
					"Wrong input",
					JOptionPane.ERROR_MESSAGE);
		}

		lblSampleCurrentNum.setText(Integer.toString(totalNumOfInstances - currentInstanceNum));
		lblSampleName.setText(recordedSample.getId());
	}

	private void setLabel(String value) {
		this.label = value;
	}

	public String getLabel() {
		return label;
	}

	public void setTotalNumOfInstances(int numOfInstances) {
		this.totalNumOfInstances = numOfInstances;
	}

	private void enableTrainingSetParams(boolean b) {
		chooseLabel.setEnabled(b);
		txtInstancesNum.setEnabled(b);
		btnCreateTrainingSet.setEnabled(b);
		txtPath.setEnabled(b);
		btnChooseFile.setEnabled(b);
		btnRecord.setEnabled(!b);
		lblSampleCurrentNum.setText("");
		lblSampleName.setText("");
		lblSampleCurrentNum.setVisible(!b);
		lblSampleName.setVisible(!b);
		lblRecordsLeft.setVisible(!b);
		lblRecordedSample.setVisible(!b);
	}

}
