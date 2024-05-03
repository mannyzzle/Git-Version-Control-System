package gitlet;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/** Represents a gitlet commit object.
 *
 * A single commit in the Gitlet version control system is represented by this class.
 * A commit details the changes made to the project's files and associated metadata during a certain snapshot of the files.
 *  The commit object stores the following information:
 *  * - Commit message: The description of the changes made in this commit.
 *  * - Commit time: The timestamp when the commit was created.
 *  * - Branch: The name of the branch to which this commit belongs.
 *  * - File list: A list of files included in this commit.
 *  * - Hash code: The unique identifier for this commit.
 *  * - Blob: A list of hashed file contents corresponding to the files in the commit.
 *  * - List of changed files in the current working directory (CWD).
 *  * - List of contents of the current working directory.
 *  * - List of file names in the commit.
 *
 *  @author enmanuel hernandez
 */
public class Commit implements Serializable {
    private String message;
    private String time;
    private Date date;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy -0800");
    private String branch;
    private ArrayList<File> fileList;
    private String hashCode;
    private ArrayList<String> blob;
    private List<String> listOfCWD;
    private ArrayList<String> cwdContents;
    private ArrayList<String> fileNames = new ArrayList<>();

    // Constructor for the Commit class

    Commit(String message1, String branch1, ArrayList<File> files, ArrayList<String> blob1,
           List<String> listOfCWD1, ArrayList<String> cwdContents1) {
        timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        date = new Date();

        message = message1;
        branch = branch1;
        fileList = files;

        time = timeFormat.format(date.getTime());

        blob = blob1;
        listOfCWD = listOfCWD1;
        cwdContents = cwdContents1;

        for (File f : fileList) {
            this.fileNames.add(f.getName());
        }
    }

    public List<String> getFileContents(File file) throws IOException {
        byte[] fileContent = Utils.readContents(file);
        String contentString = new String(fileContent, StandardCharsets.UTF_8);
        String[] lines = contentString.split("\n");
        return Arrays.asList(lines);
    }

    // Getters and Setters for various attributes of the Commit class

    public String getMessage() {
        return message;
    }

    public String getBranch() {
        return branch;
    }

    public ArrayList<File> getFileList() {
        return fileList;
    }

    public String getHash() {
        return hashCode;
    }

    public void setHash(String h) {
        this.hashCode = h;
    }

    public ArrayList<String> getBlob() {
        return blob;
    }

    public List<String> getCWD() {
        return listOfCWD;
    }

    public ArrayList<String> cwdCons() {
        return cwdContents;
    }


    public String getTime() {
        return time;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(int d) {
        date.setTime(d);
    }

    public void setTime(Date d) {
        time = timeFormat.format(d);
    }

}