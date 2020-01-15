package rslib.cs.protocol.events.board.container.file;

import rslib.cs.protocol.events.board.common.ComponentEvent;
import rslib.gui.container.file.FileContainer;
import rslib.gui.container.file.FileModel;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents a change image event
 */
public class ChangeFileEvent extends ComponentEvent {

    /** File */
    private FileModel file;

    /***
     * Constructor
     * @param hash board hash
     * @param id component id
     * @param file file
     */
    public ChangeFileEvent(int hash, int id, FileModel file) {
        super(hash, id);
        this.file = file;
    }

    /***
     * Constructor for externalization
     */
    public ChangeFileEvent() {
    }

    @Override
    public BoardEventType getIndex() {
        return BoardEventType.CHANGE_FILE_E;
    }

    public FileModel getFile() {
        return file;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(file);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        file = (FileModel) in.readObject();
    }
}
