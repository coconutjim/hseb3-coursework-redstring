package rslib.gui.board;

import rslib.gui.BasicComponent;
import rslib.gui.container.BoardContainer;
import rslib.gui.container.ExternalizableContainer;
import rslib.gui.style.ColorModel;
import rslib.gui.style.FontModel;
import rslib.listeners.BoardListener;
import rslib.listeners.MainClientListener;

import java.util.concurrent.CopyOnWriteArrayList;

/***
 * Represents interactive board and its functions
 */
public interface InteractiveBoard extends BasicComponent,
        BoardListener, MainClientListener {

    /** Board id */
    public final int BOARD_ID = -100;

    /** Delta between board border and last component */
    public final int BORDER_DELTA = 10;

    /***
     * Clears the board. Deletes all containers and paintings
     */
    public void clearBoard();

    /***
     * Sets containers
     * @param serializableContainers saved containers
     */
    public void setBoardContent(CopyOnWriteArrayList<ExternalizableContainer> serializableContainers);

    /***
     * Adds a container to the board
     * @param container current container
     */
    public void addContainer(BoardContainer container);

    /***
     * Deletes container from board
     * @param id container id
     */
    public void deleteContainer(int id);

    /***
     * Sets container layer position on the board
     * @param layer layer position
     */
    public void setLayerPosition(BoardContainer container, int layer);

    /***
     * Gets layer position of the board container
     * @param container board container
     * @return layer position, -100 otherwise
     */
    public int getLayerPosition(BoardContainer container);

    /***
     * Converts board into serializable info
     * @return serializable info
     */
    public ExternalizableBoard toExternalizable();

    //TODO: may be not useful
    /***
     * Generate id for new container
     * @return unique id
     */
    public int generateId();

    /***
     * Finds container by its id
     * @param id container id
     * @return container with its id, null otherwise
     */
    public BoardContainer findContainer(int id);

    /***
     * Sets font to all board containers
     * @param font new font
     */
    public void setGeneralContainerFont(FontModel font);

    /***
     * Gets general to all board containers font
     * @return general font
     */
    public FontModel getGeneralContainerFont();

    /***
     * Sets all board containers opaque
     * @param opaque new opaque
     */
    public void setGeneralContainerOpaque(boolean opaque);

    /***
     * Gets general to all board containers opaque
     * @return general opaque
     */
    public boolean isGeneralContainerOpaque();

    /***
     * Sets background color to all board containers
     * @param color new background color
     */
    public void setGeneralContainerBackground(ColorModel color);

    /***
     * Gets general to all board containers background color
     * @return general background color
     */
    public ColorModel getGeneralContainerBackground();

    /***
     * Sets foreground color to all board containers
     * @param color new foreground color
     */
    public void setGeneralContainerForeground(ColorModel color);

    /***
     * Gets general to all board containers foreground color
     * @return general foreground color
     */
    public ColorModel getGeneralContainerForeground();

    /***
     * Sets board asynchronous
     * @param asynchronous asynchronous
     */
    public void setAsynchronous(boolean asynchronous);

    /***
     * Gets board asynchronous
     * @return if board is asynchronous
     */
    public boolean isAsynchronous();

    /***
     * Inflates container from saved data
     * @param serializableContainer saved data
     * @return board container
     */
    public BoardContainer inflateContainer(ExternalizableContainer serializableContainer);
}
