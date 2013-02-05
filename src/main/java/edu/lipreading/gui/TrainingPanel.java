package edu.lipreading.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import edu.lipreading.Constants;
import edu.lipreading.Sample;
import edu.lipreading.TrainingSet;
import edu.lipreading.Utils;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTextField;

public class TrainingPanel extends LipReaderPanel {
	
	private static final long serialVersionUID = -5713175015110844830L;
	private final JComboBox<String> chooseLabel;
	//private final JComboBox<String> normalizerType;
	private String label;
	private final Map<String, AtomicInteger> counters = new HashMap<String, AtomicInteger>();
	//private AtomicBoolean shouldUpdate = new AtomicBoolean(false);
	//private JButton addToTrainingSet;
	//private JButton saveToFile;
	//private JButton normalize;
	private JTextField txtInstancesNum;
	private JTextField txtPath;
	private int totalNumOfInstances;
	private JButton btnCreateTrainingSet;
	private int currentInstanceNum;
	final JFileChooser fileChooser = new JFileChooser();
	private JLabel lblChooseWordsentenceTo;
	private JLabel lblPathToSave;
	private JLabel lblSampleCurrentNum;
	private JLabel lblSampleName;
	private JButton btnChooseFile;
	private JLabel lblRecordedSample;
	private JLabel lblRecordsLeft;
	
	public TrainingPanel() {
		super();
		btnRecord.setEnabled(false);
		
		
		//btnRecord.addMouseListener(new MouseAdapter() {
		//	@Override
		//	public void mouseClicked(MouseEvent e) {
        btnRecord.addActionListener(new ActionListener() {
        	@Override
            public void actionPerformed(ActionEvent arg0) {
        		if (!isRecording())
        			setSampleName(getLabel() + (counters.get(getLabel().toLowerCase()).addAndGet(1)));
			}
		});
		canvas.setBounds(163, 142, 345, 255);
		btnRecord.setBounds(327, 403, 45, 45);
		remove(lblOutput);


		chooseLabel = new JComboBox<String>();
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
			public void itemStateChanged(ItemEvent e) {
				setLabel((String)chooseLabel.getSelectedItem());
			}
		});
		chooseLabel.setToolTipText("Choose a label for the recorded sample");
		chooseLabel.setBounds(309, 11, 247, 20);
		add(chooseLabel);

		/*
		addToTrainingSet = new JButton("Add");
		addToTrainingSet.setToolTipText("Add to training set");
		addToTrainingSet.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateSampleId();
				TrainingSet.get().add(recordedSample);
			}
		});
		addToTrainingSet.setBounds(612, 11, 60, 20);
		add(addToTrainingSet);
		addToTrainingSet.setEnabled(false);

		saveToFile = new JButton("Save");
		saveToFile.setToolTipText("Save to file");
		saveToFile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				updateSampleId();
				JFileChooser saver = new JFileChooser();
				saver.setFileFilter(new FileFilter() {

					@Override
					public String getDescription() {
						return ".xml";
					}

					@Override
					public boolean accept(File arg0) {
						return arg0.isDirectory() ||
								arg0.getName().toLowerCase().contains(".xml");
					}
				});
				saver.setAcceptAllFileFilterUsed(false);
				saver.setSelectedFile(new File(recordedSample.getId()));
				int option = saver.showSaveDialog(getParent());
				if (option == JFileChooser.APPROVE_OPTION){
					try {
						XStream.write(saver.getSelectedFile(), recordedSample);
					} catch (Exception e1) {
						throw new RuntimeException(e1);
					}
				}
			}
		});
		saveToFile.setBounds(612, 42, 60, 20);
		add(saveToFile);
		saveToFile.setEnabled(false);
	
	
		normalizerType = new JComboBox<String>(new String[]{"Center", "Stretch Time", "Simple Time", "Skipped Frames"});
		normalizerType.setBounds(250, 415, 110, 20);
		normalizerType.setEnabled(false);
		add(normalizerType);

	
		normalize = new JButton("Normalize");
		
		normalize.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				switch((String)normalizerType.getSelectedItem()){
				case "Center":
					recordedSample = new CenterNormalizer().normalize(recordedSample);
					break;
				case "Stretch Time":
					recordedSample = new LinearStretchTimeNormalizer().normalize(recordedSample);
					break;
				case "Simple Time":
					recordedSample = new SimpleTimeNormalizer().normalize(recordedSample);
					break;
				case "Skipped Frames":
					recordedSample = new SkippedFramesNormalizer().normalize(recordedSample);
					break;
				}
			}
		});
		normalize.setBounds(592, 73, 80, 20);
		normalize.setEnabled(false);
		add(normalize);
		*/
		/*
		JButton exportDataSet = new JButton("Export Data Set");
		exportDataSet.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser saver = new JFileChooser();
				saver.setFileFilter(new FileFilter() {

					@Override
					public String getDescription() {
						return ".zip";
					}

					@Override
					public boolean accept(File arg0) {
						return arg0.isDirectory() ||
								arg0.getName().toLowerCase().contains(".zip");
					}
				});
				saver.setFileFilter(new FileFilter() {

					@Override
					public String getDescription() {
						return ".arff";
					}

					@Override
					public boolean accept(File arg0) {
						return arg0.isDirectory() ||
								arg0.getName().toLowerCase().contains(".arff");
					}
				});
				saver.setFileFilter(new FileFilter() {

					@Override
					public String getDescription() {
						return ".csv";
					}

					@Override
					public boolean accept(File arg0) {
						return arg0.isDirectory() ||
								arg0.getName().toLowerCase().contains(".csv");
					}
				});
				saver.setAcceptAllFileFilterUsed(false);
				saver.setSelectedFile(new File("DataSet"));
				int option = saver.showSaveDialog(getParent());
				if (option == JFileChooser.APPROVE_OPTION){
					try {
						String path = saver.getSelectedFile().getAbsolutePath();
						if(path.split("\\.").length != 2){
							path += saver.getFileFilter().getDescription();
						}
						switch(saver.getFileFilter().getDescription()){
						case ".arff":
							//TODO
							break;
						case ".csv":
							//TODO
							break;
						case ".zip":
							//TODO
							break;
						}
					} catch (Exception e1) {
						throw new RuntimeException(e1);
					}
				}
			}
		});
		int exportDataSetWidth = 200;
		exportDataSet.setBounds(592, 105, 115, 20);
		add(exportDataSet);
		*/
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
		btnCreateTrainingSet.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				boolean error = false;
				try {
					setTotalNumOfInstances(Integer.parseInt(txtInstancesNum.getText()));
				}
				catch (NumberFormatException ex) {		
					error = true;
					JOptionPane.showMessageDialog(TrainingPanel.this,
							"Number of instances requires a number",
							"Wrong input",
							JOptionPane.WARNING_MESSAGE);
				}
				if (txtPath.getText().isEmpty()){
					error = true;
					JOptionPane.showMessageDialog(TrainingPanel.this,
							"Please choose a folder to save the recorded Training Set",
							"Wrong input",
							JOptionPane.WARNING_MESSAGE);
				}
				if (error == false){
					setLabel((String)chooseLabel.getSelectedItem());
					currentInstanceNum = 0;
					enableTrainingSetParams(false);
				}
			}

		});
		btnCreateTrainingSet.setBounds(425, 104, 131, 23);
		add(btnCreateTrainingSet);
		
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		btnChooseFile = new JButton(new ImageIcon(getClass().getResource(Constants.FILE_CHOOSER_IMAGE_FILE_PATH)));
		btnChooseFile.setBorderPainted(false);
		btnChooseFile.setBackground(Color.WHITE);
		btnChooseFile.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				int returnVal = fileChooser.showOpenDialog(TrainingPanel.this);

				if (returnVal == JFileChooser.APPROVE_OPTION){
					txtPath.setText(fileChooser.getSelectedFile().getPath());
				}
			}
		});
		btnChooseFile.setBounds(524, 61, 32, 32);;
		add(btnChooseFile);
		
		lblChooseWordsentenceTo = new JLabel("Choose Word/Sentence to record:");
		lblChooseWordsentenceTo.setBounds(136, 14, 165, 14);
		add(lblChooseWordsentenceTo);
		
		lblPathToSave = new JLabel("Path to save:");
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
		lblSampleName.setBounds(248, 459, 196, 14);
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
		
		lblSampleCurrentNum.setVisible(false);
		lblSampleName.setVisible(false);
		lblRecordsLeft.setVisible(false);
		lblRecordedSample.setVisible(false);
		
	}
	
	@Override
	protected void handleRecordedSample() {
		//shouldUpdate.set(true);

		currentInstanceNum++;
		counters.put(getLabel(), counters.get(getLabel() + 1));
		
		if (currentInstanceNum == totalNumOfInstances)
		{
			enableTrainingSetParams(true);
		}
		
		try {
			
			String fileName =  recordedSample.getId().replaceAll("[:/]", ".") + ".xml";
			Utils.writeSampleToXML(txtPath.getText() ,fileName, recordedSample);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(TrainingPanel.this,
					"Cannot save file. Reason:" + e.getMessage(),
					"Wrong input",
					JOptionPane.ERROR_MESSAGE);
		}
		
		lblSampleCurrentNum.setText(new Integer(totalNumOfInstances - currentInstanceNum).toString());
		lblSampleName.setText(recordedSample.getId());

		//TODO - add save video file option
		
		//addToTrainingSet.setEnabled(true);
		//saveToFile.setEnabled(true);
		//normalize.setEnabled(true);
		//normalizerType.setEnabled(true);
		
	}
	/*
	private void updateSampleId() {
		if(shouldUpdate.get()){
			shouldUpdate.set(false);
			recordedSample.setId(getLabel().toLowerCase() + "-" + counters.get(label.toLowerCase()));
		}
	}
*/
	private void setLabel(String value) {
		this.label = value;
	}

	public String getLabel() {
		return label;
	}

	public int getTotalNumOfInstances() {
		return totalNumOfInstances;
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

	public int getCurrentInstanceNum() {
		return currentInstanceNum;
	}

	public void setCurrentInstanceNum(int currentInstanceNum) {
		this.currentInstanceNum = currentInstanceNum;
	}
}

