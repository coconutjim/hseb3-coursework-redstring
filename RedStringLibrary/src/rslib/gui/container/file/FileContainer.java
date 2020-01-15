package rslib.gui.container.file;

import rslib.gui.container.BoardContainer;

/***
 * Represents a container that holds pure file
 */
public interface FileContainer extends BoardContainer {

    /** Maximum file size in bytes (5 MB) */
    public final int FILE_MAXIMUM_SIZE = 5 * 1024 * 1024;

    /***
     * Sets file to the container
     * @param file file
     */
    public void setFile(FileModel file);

    /***
     * Gets container file
     * @return container file
     */
    public FileModel getFile();
}
