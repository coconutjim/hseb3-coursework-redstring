package rslib.commands.container.file;

import rslib.cs.client.user.UserClient;
import rslib.cs.protocol.events.board.container.file.ChangeFileEvent;
import rslib.gui.board.InteractiveBoard;
import rslib.gui.container.file.FileContainer;
import rslib.gui.container.file.FileModel;

/***
 * Represents a change image command
 */
public class ChangeFileCommand extends FileContainerCommand {

    /** Old file */
    private FileModel oldFile;

    /** New file */
    private FileModel newFile;

    /***
     * Constructor
     * @param board link to board
     * @param fileContainer file container
     * @param newFile new image
     */
    public ChangeFileCommand(InteractiveBoard board, FileContainer fileContainer, FileModel newFile) {
        super(board, fileContainer);
        oldFile = fileContainer.getFile();
        this.newFile = newFile;
    }

    @Override
    public void execute(UserClient userClient) throws IllegalStateException {
        super.execute(userClient);
        FileContainer imageContainer = getFileContainer();
        if (userClient.checkBoardRights(imageContainer.getComponentStatus(), imageContainer.getComponentOwner())) {
            checkBlocked(userClient, imageContainer);
            int hash = getBoard().hashCode();
            userClient.addBoardEvent(new ChangeFileEvent(hash, id, newFile));
        }
        else {
            throw new IllegalStateException("You have no rights to set file to this container!");
        }
    }

    @Override
    public void undo(UserClient userClient) throws IllegalStateException {
        super.undo(userClient);
        FileContainer imageContainer = getFileContainer();
        if (userClient.checkBoardRights(imageContainer.getComponentStatus(), imageContainer.getComponentOwner())) {
            checkBlocked(userClient, imageContainer);
            int hash = getBoard().hashCode();
            userClient.addBoardEvent(new ChangeFileEvent(hash, id, oldFile));
        }
        else {
            throw new IllegalStateException("You have no rights to set file to this container!");
        }
    }
}
