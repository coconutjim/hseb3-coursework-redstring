package rslib.gui.container.text;

import rslib.gui.container.BoardContainer;

/***
 * Represents a container that holds pure text
 */
public interface TextContainer extends BoardContainer {

    /** Maximum text size in symbols */
    public final int TEXT_MAXIMUM_SIZE = 100000;

    /***
     * Sets text to the container
     * @param text text
     */
    public void setText(String text);

    /***
     * Appends text to the container
     * @param text text
     */
    public void appendText(String text);

    /***
     * Gets container text
     * @return container text
     */
    public String getText();
}
