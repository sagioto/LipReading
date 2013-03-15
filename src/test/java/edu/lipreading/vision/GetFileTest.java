package edu.lipreading.vision;

import java.io.File;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import edu.lipreading.Utils;


public class GetFileTest {

	protected static final String FILE_URL = "https://dl.dropbox.com/u/8720454/set2/yes/yes-5.MOV";

	@Before
	public void setUp() throws Exception {
		cleanFile();
	}
	
    private static void cleanFile() throws Exception {
        File file = new File(Utils.getFileNameFromUrl(FILE_URL));
		if(file.exists())
			Assert.assertTrue("file was not deleted", file.delete());
    }

    @Test
    public void readWordsFileTest() throws Exception {
    	List<String> readFile = Utils.readFile("vocabularies/words.txt");
    	Assert.assertEquals("job", readFile.get(0));
    }

	@Test
	public void downloadTest() throws Exception{
		Utils.get(FILE_URL);
		Assert.assertTrue("file was not downloaded", new File(Utils.getFileNameFromUrl(FILE_URL)).exists());
	}

	@Test
	public void readFromFileTest() throws Exception  {
		Utils.get(FILE_URL);
		File file = new File(Utils.getFileNameFromUrl(FILE_URL));
		Assert.assertTrue("file was not downloaded", file.exists());
		new ColoredStickersFeatureExtractor().extract(file.getName());
	}

	@Test
	public void readFromUrlTest() throws Exception{
		new NullFeatureExtrcator().extract(FILE_URL);
	}
	


	@AfterClass
	public static void teardown() throws Exception {
	    cleanFile();
	}
	
	
}
