package rslib.gui.board;

import rslib.cs.common.Status;
import rslib.cs.protocol.events.board.BoardEvent;
import rslib.cs.protocol.events.main_client.MainClientEvent;
import rslib.gui.ExternalizableComponent;
import rslib.gui.container.BoardContainer;
import rslib.gui.container.ExternalizableContainer;
import rslib.gui.container.file.ExternalizableFileContainer;
import rslib.gui.container.image.ExternalizableImageContainer;
import rslib.gui.container.text.ExternalizableTextContainer;
import rslib.gui.style.ColorModel;
import rslib.gui.style.FontModel;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.concurrent.CopyOnWriteArrayList;


/***
 * Info about board that can be serialized
 */
public class ExternalizableBoard extends ExternalizableComponent implements InteractiveBoard {

    /** For better parsing */
    public static final long serialVersionUID = 85451821811231L;

    /** All containers */
    protected CopyOnWriteArrayList<ExternalizableContainer> containers;

    /** General container font */
    protected FontModel generalFont;

    /** General container opaque */
    protected boolean generalOpaque;

    /** General container foreground */
    protected ColorModel generalForeground;

    /** General container background */
    protected ColorModel generalBackground;

    /** If the board working is asynchronous */
    protected boolean asynchronous;

    /***
     * Constructor
     * @param owner component owner username
     * @param status component status
     * @param left component left
     * @param top component top
     * @param width component width
     * @param height component height
     * @param minimumWidth component minimum width
     * @param minimumHeight component minimum height
     * @param maximumWidth component maximum width
     * @param maximumHeight component maximum height
     * @param name component name
     * @param id component id
     * @param font component font
     * @param opaque component opaque
     * @param foreground component foreground color
     * @param background component background color
     * @param blocked component blocked status
     * @param blockOwner block owner
     * @param containers all board containers
     * @param generalFont general container font
     * @param generalOpaque general container opaque
     * @param generalForeground general container foreground color
     * @param generalBackground general container background color
     * @param asynchronous if board is asynchronous
     */
    public ExternalizableBoard(String owner, Status status, int left, int top, int width, int height,
                               int minimumWidth, int minimumHeight, int maximumWidth, int maximumHeight,
                               String name, int id,
                               FontModel font, boolean opaque,
                               ColorModel foreground, ColorModel background,
                               boolean blocked, String blockOwner,
                               CopyOnWriteArrayList<ExternalizableContainer> containers,
                               FontModel generalFont, boolean generalOpaque,
                               ColorModel generalForeground, ColorModel generalBackground,
                               boolean asynchronous) {
        super(owner, status, left, top, width, height,
        minimumWidth, minimumHeight,  maximumWidth, maximumHeight, name, id, font, opaque, foreground, background,
                blocked, blockOwner);
        if (containers == null) {
            throw new IllegalArgumentException("SerializableBoard: "
                    + "containers is null!");
        }
        if (generalFont == null) {
            throw new IllegalArgumentException("SerializableBoard: "
                    + "generalFont is null!");
        }
        if (generalForeground == null) {
            throw new IllegalArgumentException("SerializableBoard: "
                    + "generalForeground is null!");
        }
        if (generalBackground == null) {
            throw new IllegalArgumentException("SerializableBoard: "
                    + "generalBackground is null!");
        }
        this.containers = containers;
        this.generalFont = generalFont;
        this.generalOpaque = generalOpaque;
        this.generalForeground = generalForeground;
        this.generalBackground = generalBackground;
        this.asynchronous = asynchronous;
    }

    /***
     * Constructor
     * @param sb saved data
     */
    public ExternalizableBoard(ExternalizableBoard sb) {
        super(sb.owner, sb.status, sb.left, sb.top, sb.width, sb.height, sb.minimumWidth, sb.minimumHeight,
                sb.maximumWidth, sb.maximumHeight, sb.name, sb.id,
                sb.font, sb.opaque, sb.foreground, sb.background, sb.blocked, sb.blockOwner);
        containers = sb.containers;
        generalFont = sb.generalFont;
        generalOpaque = sb.generalOpaque;
        generalForeground = sb.generalForeground;
        generalBackground = sb.generalBackground;
        asynchronous = sb.asynchronous;
    }

    /***
     * Constructor for externalization
     */
    public ExternalizableBoard() {
    }

    public CopyOnWriteArrayList<ExternalizableContainer> getContainers() {
        return containers;
    }


    @Override
    public void clearBoard() {
        containers.clear();
    }

    @Override
    public void addContainer(BoardContainer container) {
        ExternalizableContainer sc = container.toExternalizable();
        int layer = container.getLayer();
        if (layer == BoardContainer.TO_FRONT) {
            int max = BoardContainer.BACKGROUND_LAYER - 1;
            for (BoardContainer bc : containers) {
                int bcLayer = bc.getLayer();
                if (bcLayer > max) {
                    max = bcLayer;
                }
            }
            container.setLayer(max + 1);
        }
        else {
            for (BoardContainer bc : containers) {
                int bcLayer = bc.getLayer();
                if (bcLayer >= layer) {
                    ++ bcLayer;
                    bc.setLayer(bcLayer);
                }
            }
        }
        containers.add(sc);
    }

    @Override
    public void deleteContainer(int id) {
        int layer = 10000;
        for (ExternalizableContainer sc : containers) {
            if (sc.getComponentId() == id) {
                containers.remove(sc);
                layer = sc.getLayer();
            }
        }
        for (BoardContainer container : containers) {
            int bcLayer = container.getLayer();
            if (bcLayer > layer) {
                -- bcLayer;
                container.setLayer(bcLayer);
            }
        }
    }

    @Override
    public ExternalizableBoard toExternalizable() {
        return new ExternalizableBoard(this);
    }

    @Override
    public int generateId() {
        throw new NotImplementedException();
    }

    @Override
    public BoardContainer findContainer(int id) {
        for (ExternalizableContainer container : containers) {
            if (container.getComponentId() == id) {
                return container;
            }
        }
        return null;
    }

    @Override
    public void setGeneralContainerFont(FontModel font) {
        generalFont = font;
    }

    @Override
    public FontModel getGeneralContainerFont() {
        return generalFont;
    }

    @Override
    public void setGeneralContainerOpaque(boolean opaque) {
        generalOpaque = opaque;
        generalBackground.setOpaque(opaque);
    }

    @Override
    public boolean isGeneralContainerOpaque() {
        return generalOpaque;
    }

    @Override
    public void setGeneralContainerBackground(ColorModel color) {
        generalBackground.setOpaque(generalOpaque);
        generalBackground = color;
    }

    @Override
    public ColorModel getGeneralContainerBackground() {
        return generalBackground;
    }

    @Override
    public void setGeneralContainerForeground(ColorModel color) {
        generalForeground = color;
    }

    @Override
    public ColorModel getGeneralContainerForeground() {
        return generalForeground;
    }

    @Override
    public void setLayerPosition(BoardContainer container, int layer) {
        int oldLayer = container.getLayer();
        int newLayer;
        switch (layer) {
            case BoardContainer.TO_BACKGROUND: {
                for (BoardContainer bc : containers) {
                    int bcLayer = bc.getLayer();
                    if (bcLayer < oldLayer) {
                        ++ bcLayer;
                        bc.setLayer(bcLayer);
                    }
                }
                newLayer = BoardContainer.BACKGROUND_LAYER;
                break;
            }
            case BoardContainer.TO_FRONT: {
                int max = 0;
                for (BoardContainer bc : containers) {
                    int bcLayer = bc.getLayer();
                    if (bcLayer > max) {
                        max = bcLayer;
                    }
                    if (bcLayer > oldLayer) {
                        -- bcLayer;
                        bc.setLayer(bcLayer);
                    }
                }
                newLayer = max;
                break;
            }
            default: {
                if (layer > oldLayer) {
                    for (BoardContainer bc : containers) {
                        int bcLayer = bc.getLayer();
                        if (bcLayer <= layer && bcLayer > oldLayer) {
                            -- bcLayer;
                            bc.setLayer(bcLayer);
                        }
                    }
                }
                else {
                    if (layer < oldLayer) {
                        for (BoardContainer bc : containers) {
                            int bcLayer = bc.getLayer();
                            if (bcLayer >= layer && bcLayer < oldLayer) {
                                ++ bcLayer;
                                bc.setLayer(bcLayer);
                            }
                        }
                    }
                }
                newLayer = layer;
                break;
            }
        }
        container.setLayer(newLayer);
    }

    @Override
    public int getLayerPosition(BoardContainer container) {
        return container.getLayer();
    }

    @Override
    public void setAsynchronous(boolean asynchronous) {
        this.asynchronous = asynchronous;
    }

    @Override
    public boolean isAsynchronous() {
        return asynchronous;
    }

    @Override
    public BoardContainer inflateContainer(ExternalizableContainer serializableContainer) {
        BoardContainer container = null;
        switch (serializableContainer.getType()) {
            case TEXT_CONTAINER: {
                ExternalizableTextContainer stc = (ExternalizableTextContainer) serializableContainer;
                container = new ExternalizableTextContainer(stc.getComponentOwner(),
                        stc.getComponentStatus(), stc.getComponentLeft(), stc.getComponentTop(),
                        stc.getComponentWidth(), stc.getComponentHeight(),
                        stc.getComponentMinimumWidth(), stc.getComponentMinimumHeight(),
                        stc.getComponentMaximumWidth(), stc.getComponentMaximumHeight(), stc.getComponentName(),
                        stc.getComponentId(), stc.getComponentFont(), stc.isComponentOpaque(),
                        stc.getComponentForeground(),
                        stc.getComponentBackground(),
                        stc.getLayer(), stc.getType(), stc.isBlocked(),
                        stc.getBlockOwner(),
                        stc.getText());
                break;
            }
            case IMAGE_CONTAINER: {
                ExternalizableImageContainer sic = (ExternalizableImageContainer) serializableContainer;
                container = new ExternalizableImageContainer(sic.getComponentOwner(),
                        sic.getComponentStatus(), sic.getComponentLeft(), sic.getComponentTop(),
                        sic.getComponentWidth(), sic.getComponentHeight(),
                        sic.getComponentMinimumWidth(), sic.getComponentMinimumHeight(),
                        sic.getComponentMaximumWidth(), sic.getComponentMaximumHeight(), sic.getComponentName(),
                        sic.getComponentId(), sic.getComponentFont(), sic.isComponentOpaque(),
                        sic.getComponentForeground(),
                        sic.getComponentBackground(),
                        sic.getLayer(), sic.getType(), sic.isBlocked(),
                        sic.getBlockOwner(),
                        sic.getImage());
                break;
            }
            case FILE_CONTAINER: {
                ExternalizableFileContainer sfc = (ExternalizableFileContainer) serializableContainer;
                container = new ExternalizableFileContainer(sfc.getComponentOwner(),
                        sfc.getComponentStatus(), sfc.getComponentLeft(), sfc.getComponentTop(),
                        sfc.getComponentWidth(), sfc.getComponentHeight(),
                        sfc.getComponentMinimumWidth(), sfc.getComponentMinimumHeight(),
                        sfc.getComponentMaximumWidth(), sfc.getComponentMaximumHeight(), sfc.getComponentName(),
                        sfc.getComponentId(), sfc.getComponentFont(), sfc.isComponentOpaque(),
                        sfc.getComponentForeground(),
                        sfc.getComponentBackground(),
                        sfc.getLayer(), sfc.getType(), sfc.isBlocked(),
                        sfc.getBlockOwner(),
                        sfc.getFile());
                break;
            }
        }
        return container;
    }

    @Override
    public void setBoardContent(CopyOnWriteArrayList<ExternalizableContainer> serializableContainers) {
        this.containers = serializableContainers;
    }

    @Override
    public void hear(BoardEvent event) {
        // nothing to implement
        throw new NotImplementedException();
    }

    @Override
    public void hear(MainClientEvent event) {
        // nothing to implement
        throw new NotImplementedException();
    }

    @Override
    public String toString() {
        return "InteractiveBoard{" + '\n' +
                super.toString() +
                "containers=" + containers.toString() + '\n' +
                ", generalFont=" + generalFont.toString() + '\n' +
                ", generalOpaque=" + generalOpaque + '\n' +
                ", generalForeground=" + generalForeground.toString() + '\n' +
                ", generalBackground=" + generalBackground.toString() + '\n' +
                ", asynchronous=" + asynchronous + '\n' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        if (! super.equals(o)) {
            return false;
        }

        ExternalizableBoard that = (ExternalizableBoard) o;

        if (!containers.equals(that.containers)) return false;
        if (!generalBackground.equals(that.generalBackground)) return false;
        if (generalOpaque != that.generalOpaque) return false;
        if (!generalFont.equals(that.generalFont)) return false;
        if (!generalForeground.equals(that.generalForeground)) return false;
        if (asynchronous != that.asynchronous) return false;

        return true;
    }

    @Override
    public int hashCode() {
        if (blocked) {
            return 0;
        }
        int result = super.hashCode();
        for (BoardContainer container : containers) {
            result = 31 * result + container.hashCode();
        }
        //result = 31 * result + (containers != null? containers.hashCode() : 0);
        result = 31 * result + generalFont.hashCode();
        result = 31 * result + (generalOpaque ? 1 : 0);
        result = 31 * result + generalForeground.hashCode();
        result = 31 * result + generalBackground.hashCode();
        result = 31 * result + (asynchronous ? 1 : 0);
        return result;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(containers);
        out.writeObject(generalFont);
        out.writeBoolean(generalOpaque);
        out.writeObject(generalForeground);
        out.writeObject(generalBackground);
        out.writeBoolean(asynchronous);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        containers = (CopyOnWriteArrayList<ExternalizableContainer>) in.readObject();
        generalFont = (FontModel) in.readObject();
        generalOpaque = in.readBoolean();
        generalForeground = (ColorModel) in.readObject();
        generalBackground = (ColorModel) in.readObject();
        asynchronous = in.readBoolean();
    }
}
