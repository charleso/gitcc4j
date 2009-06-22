package gitcc.cc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CCCommit {

	private final List<CCFile> files = new ArrayList<CCFile>();

	private Date date;

	private String message;

	private String author;

	public CCCommit() {
		super();
	}

	public CCCommit(String author, Date date, String message) {
		this.author = author;
		this.date = date;
		this.message = message;
	}

	public List<CCFile> getFiles() {
		return files;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}
}
