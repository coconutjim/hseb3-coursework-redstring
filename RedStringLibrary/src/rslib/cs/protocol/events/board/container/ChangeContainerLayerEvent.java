package rslib.cs.protocol.events.board.container;

import rslib.cs.protocol.events.board.common.ComponentEvent;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents a change container layer event
 */
public class ChangeContainerLayerEvent extends ComponentEvent {

    /** For better parsing */
    public static final long serialVersionUID = 72515512127623L;

    /** New container layer */
    private int layer;

    /***
     * Constructor
     * @param hash board hash
     * @param id container id
     * @param layer new container layer
     */
    public ChangeContainerLayerEvent(int hash, int id, int layer) {
        super(hash, id);
        this.layer = layer;
    }

    /***
     * Constructor for externalization
     */
    public ChangeContainerLayerEvent() {
    }

    @Override
    public BoardEventType getIndex() {
        return BoardEventType.CHANGE_CONTAINER_LAYER_E;
    }

    public int getLayer() {
        return layer;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeInt(layer);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        layer = in.readInt();
    }
}
