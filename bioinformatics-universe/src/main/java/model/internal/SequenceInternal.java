package model.internal;

public class SequenceInternal {
	private String firstFile;
	private String secondFile;
	private String firstFileDelim;
	private int firstFileColumn;
	private String secondFileDelim;	
	private int secondFileColumn;
	
	public String getFirstFile() {
		return firstFile;
	}
	public void setFirstFile(String firstFile) {
		this.firstFile = firstFile;
	}
	public String getSecondFile() {
		return secondFile;
	}
	public void setSecondFile(String secondFile) {
		this.secondFile = secondFile;
	}
	public String getFirstFileDelim() {
		return firstFileDelim;
	}
	public void setFirstFileDelim(String firstFileDelim) {
		this.firstFileDelim = firstFileDelim;
	}
	public int getFirstFileColumn() {
		return firstFileColumn;
	}
	public void setFirstFileColumn(int firstFileColumn) {
		this.firstFileColumn = firstFileColumn;
	}
	public String getSecondFileDelim() {
		return secondFileDelim;
	}
	public void setSecondFileDelim(String secondFileDelim) {
		this.secondFileDelim = secondFileDelim;
	}
	public int getSecondFileColumn() {
		return secondFileColumn;
	}
	public void setSecondFileColumn(int secondFileColumn) {
		this.secondFileColumn = secondFileColumn;
	}

}
