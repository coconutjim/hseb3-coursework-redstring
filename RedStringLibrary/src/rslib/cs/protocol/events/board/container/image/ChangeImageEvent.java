package rslib.cs.protocol.events.board.container.image;

import rslib.cs.protocol.events.board.common.ComponentEvent;
import rslib.gui.container.image.ImageModel;
import rslib.gui.container.image.ImageContainer;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents a change image event
 */
public class ChangeImageEvent extends ComponentEvent {

    /** Image */
    private ImageModel image;

    /***
     * Constructor
     * @param hash board hash
     * @param id component id
     * @param image image
     */
    public ChangeImageEvent(int hash, int id, ImageModel image) {
        super(hash, id);
        if (image != null && (image.getWidth() > ImageContainer.IMAGE_MAXIMUM_WIDTH
                || image.getHeight() > ImageContainer.IMAGE_MAXIMUM_HEIGHT)) {
            throw new IllegalArgumentException("ChangeImageEvent: too big image! Sizes not more than" +
                    "1000 allowed!");
        }
        this.image = image;
    }

    /***
     * Constructor for externalization
     */
    public ChangeImageEvent() {
    }

    @Override
    public BoardEventType getIndex() {
        return BoardEventType.CHANGE_IMAGE_E;
    }

    public ImageModel getImage() {
        return image;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(image);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        image = (ImageModel) in.readObject();
    }
}
