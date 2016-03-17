package mobisocial.musubi.cloudstorage;

import java.util.Date;

/**
 * Stores properties of a file description.
 */
public class FileDescription {
    private String fileName;
    private String displayName;
    private Date lastModified;
    private long bytes;

    public FileDescription(String fileName, String displayName, Date
                           lastModified, long bytes) {
        this.fileName = fileName;
        this.lastModified = lastModified;
        this.displayName = displayName;
        this.bytes = bytes;
    }

    public String getFileName() {
        return fileName;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public String getDisplayName() {
        return displayName;
    }

    public long getBytes() {
        return bytes;
    }
}
