package edu.lipreading.classification;

import edu.lipreading.Constants;
import edu.lipreading.Sample;
import edu.lipreading.Utils;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class TimeWarperClassifier implements Classifier{
	private List<Sample> trainingSet;
	private List<String> vocabulary = Constants.VOCABULARY;


	@Override
	public String test(final Sample test) {
		final AtomicLong [] results = new AtomicLong[vocabulary.size()];
		final AtomicInteger [] counts = new AtomicInteger[vocabulary.size()];
		final double [] finalResults = new double[vocabulary.size()];
		final ExecutorService threadPool = Executors.newCachedThreadPool();
		final List<Future<?>> futures = new Vector<Future<?>>();
		
		
		for (int i = 0; i < vocabulary.size(); i++) {
			results[i] = new AtomicLong(0);
			counts[i] = new AtomicInteger(0);
		}


		for (final Sample training : trainingSet) {
			futures.add(threadPool.submit(new Runnable() {
				@Override
				public void run() {
					final TimeWarper tw = new TimeWarper();
					if(!test.equals(training)){
						for (int i = 0; i < vocabulary.size(); i++) {
							if(vocabulary.get(i).equals(training.getLabel())){
								results[i].addAndGet(Double.doubleToLongBits(tw.dtw(test, training)));
								counts[i].incrementAndGet();
							}
						}
					}
				}
			}));
		}
		for (Future<?> future : futures) {
			try {
				future.get();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		for (int i = 0; i < vocabulary.size(); i++) {
			finalResults[i] = Double.doubleToLongBits(Double.longBitsToDouble(results[i].get()) / counts[i].get());
		}

		int minIndex = Utils.getMinIndex(finalResults);
		threadPool.shutdownNow();
		return vocabulary.get(minIndex);
	}


	@Override
	public void train(List<Sample> trainingSet) {
		this.trainingSet = trainingSet;

	}


	@Override
	public void update(Sample train) {
		this.trainingSet.add(train);
	}

}
