package edu.lipreading.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import weka.core.xml.XStream;
import edu.lipreading.Constants;
import edu.lipreading.Sample;
import edu.lipreading.TrainingSet;
import edu.lipreading.normalization.CenterNormalizer;
import edu.lipreading.normalization.LinearStretchTimeNormalizer;
import edu.lipreading.normalization.SimpleTimeNormalizer;
import edu.lipreading.normalization.SkippedFramesNormalizer;

public class TrainingPanel extends LipReaderPanel {
	private static final long serialVersionUID = -5713175015110844830L;
	private final JComboBox<String> chooseLabel;
	private final JComboBox<String> normalizerType;
	private String label;
	private final Map<String, AtomicInteger> counters = new HashMap<String, AtomicInteger>();
	private AtomicBoolean shouldUpdate = new AtomicBoolean(false);
	private JButton addToTrainingSet;
	private JButton saveToFile;
	private JButton normalize;

	public TrainingPanel() {
		super();
		canvas.setBounds(142, 10, 420, 308);
		btnRecord.setBounds(332, 330, 45, 45);
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
		chooseLabel.setBounds(125, 385, 320, 20);
		add(chooseLabel);

		addToTrainingSet = new JButton("Add");
		addToTrainingSet.setToolTipText("Add to training set");
		addToTrainingSet.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateSampleId();
				TrainingSet.get().add(recordedSample);
			}
		});
		addToTrainingSet.setBounds(455, 385, 60, 20);
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
						String path = saver.getSelectedFile().getAbsolutePath();
						if(path.split("\\.").length != 2)
							path += ".xml";
						XStream.write(path, recordedSample);
					} catch (Exception e1) {
						throw new RuntimeException(e1);
					}
				}
			}
		});
		saveToFile.setBounds(525, 385, 60, 20);
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
		normalize.setBounds(370, 415, 80, 20);
		normalize.setEnabled(false);
		add(normalize);
		
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
		exportDataSet.setBounds(250, 470, exportDataSetWidth, 20);
		add(exportDataSet);

	}
	
	@Override
	protected void handleRecordedSample() {
		shouldUpdate.set(true);
		addToTrainingSet.setEnabled(true);
		saveToFile.setEnabled(true);
		normalize.setEnabled(true);
		normalizerType.setEnabled(true);
	}
	
	private void updateSampleId() {
		if(shouldUpdate.get()){
			shouldUpdate.set(false);
			recordedSample.setId(getLabel().toLowerCase() + "-" 
			+ counters.get(label.toLowerCase()).incrementAndGet());
		}
	}

	private void setLabel(String value) {
		this.label = value;
	}

	public String getLabel() {
		return label;
	}
}