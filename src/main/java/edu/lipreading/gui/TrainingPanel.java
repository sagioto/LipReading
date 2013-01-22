package edu.lipreading.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import weka.core.xml.XStream;
import edu.lipreading.Constants;
import edu.lipreading.Sample;
import edu.lipreading.TrainingSet;

public class TrainingPanel extends LipReaderPanel {
	private static final long serialVersionUID = -5713175015110844830L;
	private final JComboBox<String> chooseLabel;
	private String label;
	private final Map<String, AtomicInteger> counters = new HashMap<String, AtomicInteger>();
	private JButton addToTrainingSet;
	private JButton saveToFile;

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
		saveToFile.setBounds(525, 385, 60, 20);
		add(saveToFile);
		saveToFile.setEnabled(false);

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
		exportDataSet.setBounds(245, 470, exportDataSetWidth, 20);
		add(exportDataSet);

	}
	
	@Override
	protected void handleRecordedSample() {
		recordedSample.setId(getLabel() + "-"  + counters.get(getLabel().toLowerCase()).incrementAndGet());
		addToTrainingSet.setEnabled(true);
		saveToFile.setEnabled(true);
	}

	private void setLabel(String value) {
		this.label = value;
	}

	public String getLabel() {
		return label;
	}
}

