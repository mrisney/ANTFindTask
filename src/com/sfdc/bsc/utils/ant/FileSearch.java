package com.sfdc.bsc.utils.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.tools.ant.Task;

public class FileSearch extends Task {

	private String term;
	private String initialpath;
	private String outputfile;
	private Collection<FoundFile> foundFiles = new HashSet<FoundFile>();

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

		searchDirectory(initialpath, term);
		try {
			writeFileNames(foundFiles);
		} catch (Exception e) {

		}
	}

	private void searchDirectory(final String initalPath,
			final String searchTerm) {
		try {
			Path startPath = Paths.get(initalPath);
			Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir,
						BasicFileAttributes attrs) {
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file,
						BasicFileAttributes attrs) {

					try {
						FoundFile foundFile = fileContains(file.toFile(),
								searchTerm);
						if (null != foundFile) {
							System.out.println(file.toString());
							foundFiles.add(foundFile);
						}

					} catch (Exception e) {
						System.out.println(e.toString());
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException e) {
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static final FileSearch.FoundFile fileContains(final File file,
			final String str) throws IOException {

		FileSearch.FoundFile foundFile = null;

		Pattern searchPattern = Pattern.compile(str);
		String match;
		int i = 0;
		try (Scanner scanner = new Scanner(file, "UTF-8")) {
			scanner.findInLine(searchPattern);

			while (scanner.hasNext()) {
				if ((match = scanner.findInLine(searchPattern)) != null) {
					if (null == foundFile) {
						FileSearch fileSearch = new FileSearch();
						foundFile = fileSearch.new FoundFile(file.getAbsolutePath());

					}
					foundFile.addLineNumber(i);

				}
				i++;
				scanner.next();
			}
		}
		return foundFile;
	}

	private void writeFileNames(Collection<FoundFile> files) throws IOException {
		Writer writer = new OutputStreamWriter(new FileOutputStream(outputfile));
		try {
			for (FoundFile foundFile : files) {
				writer.write(foundFile.getFileName() + "\n");
			}
		} finally {
			writer.close();
		}
	}

	class FoundFile {

		private String fileName;
		private Set<Integer> lines;

		public FoundFile(String fileName) {
			this.fileName = fileName;
			this.lines = new HashSet<Integer>();
		}

		public String getFileName() {
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