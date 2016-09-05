package util;

public class Filename {
	
	private String directory;
	private String filename;
	private String hostname;
	private String port;
	private String extension;
	private String fullname;
	
	/**
	 * Only to be used by the CompareChainsTest class. <p>
	 * TODO Fix access levels
	 * @param fullname full name of path to file
	 */
	public Filename(String fullname) {
		this.fullname = fullname;
	}
	
	public Filename(String directory, String filename, String extension) {
		this.directory = directory;
		this.filename = filename;
		this.extension = extension;
		this.fullname = directory + filename + extension;
	}
	
	public Filename(String directory, String filename, String extension, String hostname, String port) {
		this.directory = directory;
		this.filename = filename;
		this.hostname = hostname;
		this.port = port;
		this.extension = extension;
		this.fullname = directory + filename + "_" + hostname + "_" + port + extension;
	}
	
	public String directory() {
		return directory;
	}
	
	public String filename() {
		return filename;
	}
	
	public String hostname() {
		return hostname;
	}
	
	public String port() {
		return port;
	}
	
	public String extension() {
		return extension;
	}
	
	public String fullname() {
		return fullname;
	}

}