package com.sfdc.bsc.utils.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import org.apache.tools.ant.Task;

public class FileSearch extends Task {

	private String term;
	private String initialpath;
	private String outputfile;

	public void setTerm(String term) {
		this.term = term;
	}

	public void setInitialPath(String initialpath) {
		this.initialpath = initialpath;
	}

	public void setOutputFile(String outputfile) {
		this.outputfile = outputfile;
	}

	public void execute() {

		if (getProject().getProperty("initialpath") != null) {
			setInitialPath(getProject().getProperty("initialpath"));
			System.out.println("recursively looking through files, starting @ "
					+ initialpath);
		}

		if (getProject().getProperty("term") != null) {
			setTerm(getProject().getProperty("term"));
			System.out
					.println("looking for any files that contain the word : '"
							+ term + "'");
		}

		if (getProject().getProperty("outputfile") != null) {
			setOutputFile(getProject().getProperty("outputfile"));

		}

		File root = new File(initialpath);
		Set<FileSearch.FoundFile> foundFiles = new HashSet<FileSearch.FoundFile>();

		foundFiles = recursiveSearch(root, foundFiles, term);
		try {
			writeFileNames(foundFiles);
		} catch (Exception e) {
			System.out.println("problem with write files " + e.toString());
		}
	}

	private Set<FileSearch.FoundFile> recursiveSearch(File fileObject,
			Set<FileSearch.FoundFile> foundFiles, String searchTerm) {

		if (fileObject.isDirectory()) {
			File allFiles[] = fileObject.listFiles();
			for (File aFile : allFiles) {
				recursiveSearch(aFile, foundFiles, searchTerm);
			}
		} else if (fileObject.isFile()) {
			try {
				FileSearch.FoundFile foundFile = fileSearch(fileObject,
						searchTerm);
				foundFiles.add(foundFile);
			} catch (Exception e) {
				System.out.println("error on recursiveSearch " + e.toString());
			}
		}
		return foundFiles;
	}

	private FileSearch.FoundFile fileSearch(File fileObject, String searchTerm)
			throws IOException {

		FileSearch.FoundFile foundFile = null;
		FileReader fileReader = null;
		LineNumberReader lineReader = null;
		try {

			fileReader = new FileReader(fileObject);
			lineReader = new LineNumberReader(fileReader);

			String line = null;
			int lineNum = 1;
			while ((line = lineReader.readLine()) != null) {
				if (line.contains(searchTerm)) {
					if (null == foundFile) {
						foundFile = new FileSearch.FoundFile(fileObject
								.getAbsolutePath().toString());
					}
					foundFile.addLineNumber(lineNum);
					System.out.println("found '" + searchTerm + "' in file "
							+ fileObject.getAbsolutePath() + " @ line number "
							+ lineNum);
				}
				lineNum++;
			}
		} catch (Exception e) {
			System.out.println("error on fileSearch" + e.toString());

		} finally {
			if (fileReader != null)
				fileReader.close();
		}
		return foundFile;
	}

	private void writeFileNames(Set<FileSearch.FoundFile> foundFiles)
			throws IOException {

		Writer writer = new OutputStreamWriter(new FileOutputStream(outputfile));
		try {
			for (FileSearch.FoundFile foundFile : foundFiles) {
				if (null != foundFile) {
					writer.write(foundFile.getFileName() + ", Line number : "
							+ foundFile.getLines().toString() + "\n");
				}
			}
		} finally {
			writer.close();
		}
		System.out.println("found " + foundFiles.size()
				+ " file(s) with term '" + term + "'");
		System.out.println("report available @ " + outputfile);

	}

	private class FoundFile {

		private String fileName;
		private Set<Integer> lines;

		public FoundFile(String fileName) {
			this.fileName = fileName;
			this.lines = new HashSet<Integer>();
		}

		public String getFileName() {
			if (null == fileName) {
				fileName = new String();
			}
			return fileName;
		}

		public Set<Integer> getLines() {
			return lines;
		}

		public void addLineNumber(int lineNumber) {
			this.lines.add(new Integer(lineNumber));
		}
	}
}