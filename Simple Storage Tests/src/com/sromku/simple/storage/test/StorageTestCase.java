package com.sromku.simple.storage.test;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.test.InstrumentationTestCase;

import com.sromku.simple.storage.SimpleStorage;
import com.sromku.simple.storage.SimpleStorageConfiguration;
import com.sromku.simple.storage.Storage;

public class StorageTestCase extends InstrumentationTestCase {

	private Storage mStorage;

	private final static String DIR_NAME = "Storage Test";
	private final static String FILE_NAME = "test.txt";
	private final static String FILE_CONTENT = "some file content";

	private final static String FILE_SECURE_NAME = "test_secure.txt";
	private final static String FILE_SECURE_CONTENT = "something very secret";

	@Override
	protected void setUp() throws Exception {
		Context context = getInstrumentation().getContext();

		// set a storage
		mStorage = null;
		if (SimpleStorage.isExternalStorageWritable()) {
			mStorage = SimpleStorage.getExternalStorage();
		}
		else {
			mStorage = SimpleStorage.getInternalStorage(context);
		}
	}

	@Override
	protected void tearDown() throws Exception {

		// delete dir if exists
		mStorage.deleteDirectory(DIR_NAME);

		super.tearDown();
	}

	/**
	 * Create directory and check that the directory was created
	 */
	public void testCreateDirectory() {

		// TEST: create dir
		boolean wasCreated = mStorage.createDirectory(DIR_NAME, true);
		assertEquals(true, wasCreated);

	}

	/**
	 * Create directory and check that the directory was created
	 */
	public void testCreateFile() {

		// create dir
		testCreateDirectory();

		// TEST: create file
		boolean wasCreated = mStorage.createFile(DIR_NAME, FILE_NAME, FILE_CONTENT);
		assertEquals(true, wasCreated);

	}

	/**
	 * Create directory and check that the directory was created
	 */
	public void testReadFile() {

		// create file with content
		testCreateFile();

		// TEST: read the content and test
		String content = mStorage.readTextFile(DIR_NAME, FILE_NAME);
		assertEquals(FILE_CONTENT, content);

	}

	/**
	 * Create directory and check that the directory was created
	 */
	public void testAppendFile() {

		// create file with content
		testCreateFile();

		String newData = "new added data";

		// TEST: append new data and test
		mStorage.appendFile(DIR_NAME, FILE_NAME, newData);
		String content = mStorage.readTextFile(DIR_NAME, FILE_NAME);
		assertTrue(content.contains(newData));
	}

	/**
	 * Create file with encrypted data
	 */
	public void testEncryptContent() {

		// create dir
		testCreateDirectory();

		// set encryption
		final String IVX = "abcdefghijklmnop";
		final String SECRET_KEY = "secret1234567890";

		SimpleStorageConfiguration configuration = new SimpleStorageConfiguration.Builder().setEncryptContent(IVX, SECRET_KEY).build();
		SimpleStorage.updateConfiguration(configuration);

		// create file
		mStorage.createFile(DIR_NAME, FILE_SECURE_NAME, FILE_SECURE_CONTENT);

		// TEST: check the content of the file to be encrypted
		String content = mStorage.readTextFile(DIR_NAME, FILE_SECURE_NAME);
		assertEquals(FILE_SECURE_CONTENT, content);

		// TEST: check after reseting the configuration to default
		SimpleStorage.resetConfiguration();
		content = mStorage.readTextFile(DIR_NAME, FILE_SECURE_NAME);
		assertNotSame(FILE_SECURE_CONTENT, content);
	}
	
	public void testRename() {
		
		// create file
		testCreateFile();
		
		// rename
		File file = mStorage.getFile(DIR_NAME, FILE_NAME);
		mStorage.rename(file, "new_"+FILE_NAME);
		boolean isExist = mStorage.isFileExist(DIR_NAME, "new_"+FILE_NAME);
		assertEquals(true, isExist);
	}
	
	public void testGetFilesByRegex() {
		
		// create dir
		testCreateDirectory();
		
		// create 5 files
		mStorage.createFile(DIR_NAME, "file1.txt", "");
		mStorage.createFile(DIR_NAME, "file2.txt", "");
		mStorage.createFile(DIR_NAME, "file3.log", "");
		mStorage.createFile(DIR_NAME, "file4.log", "");
		mStorage.createFile(DIR_NAME, "file5.txt", "");
		
		// get files that ends with *.txt only. should be 3 of them
		String TXT_PATTERN = "([^\\s]+(\\.(?i)(txt))$)";
		List<File> filesTexts = mStorage.getFiles(DIR_NAME, TXT_PATTERN);
		assertEquals(3, filesTexts.size());
		
		// create more log files and check for *.log. should be 4 of them
		String LOG_PATTERN = "([^\\s]+(\\.(?i)(log))$)";
		mStorage.createFile(DIR_NAME, "file6.log", "");
		mStorage.createFile(DIR_NAME, "file7.log", "");
		List<File> filesLogs = mStorage.getFiles(DIR_NAME, LOG_PATTERN);
		assertEquals(4, filesLogs.size());
		
		// create dir and add files to dir. check again for *.log files. should be 4 of them.
		mStorage.createDirectory(DIR_NAME + File.separator + "New Dir");
		mStorage.createFile(DIR_NAME + File.separator + "New Dir", "file8.log", "");
		mStorage.createFile(DIR_NAME + File.separator + "New Dir", "file9.log", "");
		mStorage.createFile(DIR_NAME + File.separator + "New Dir", "file10.txt", "");
		List<File> filesLogs2 = mStorage.getFiles(DIR_NAME, LOG_PATTERN);
		assertEquals(4, filesLogs2.size());
		
		// check inside new dir for *.log files. should be 2 of them
		List<File> filesLogs3 = mStorage.getFiles(DIR_NAME + File.separator + "New Dir", LOG_PATTERN);
		assertEquals(2, filesLogs3.size());
	}
}
