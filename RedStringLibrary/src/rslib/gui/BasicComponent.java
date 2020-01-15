package rslib.gui;

import rslib.cs.common.Status;
import rslib.gui.style.ColorModel;
import rslib.gui.style.FontModel;

/***
 * Represents a basic component
 */
public interface BasicComponent {

    /***
     * Sets component owner
     * @param owner component owner
     */
    public void setComponentOwner(String owner);

    /***
     * Gets component owner username
     * @return component owner username
     */
    public String getComponentOwner();

    /***
     * Sets component status
     * @param status component status
     */
    public void setComponentStatus(Status status);

    /***
     * Gets component allowing moderating status
     * @return component allowing moderating status
     */
    public Status getComponentStatus();

    /***
     * Moves component
     * @param left left coordinate
     * @param top top coordinate
     */
    public void moveComponent(int left, int top);

    /***
     * Returns component left coordinate of the origin
     * @return left coordinate
     */
    public int getComponentLeft();

    /***
     * Returns component top coordinate of the origin
     * @return top coordinate
     */
    public int getComponentTop();

    /***
     * Resizes component
     * @param left new component origin left
     * @param top new component origin top
     * @param width new component width
     * @param height new component height
     */
    public void resizeComponent(int left, int top, int width, int height);

    /***
     * Returns object width
     * @return object width
     */
    public int getComponentWidth();

    /***
     * Returns object height
     * @return object height
     */
    public int getComponentHeight();


    /***
     * Returns object minimum width
     * @return object width
     */
    public int getComponentMinimumWidth();

    /***
     * Returns object minimum height
     * @return object height
     */
    public int getComponentMinimumHeight();

    /***
     * Returns object maximum width
     * @return object width
     */
    public int getComponentMaximumWidth();

    /***
     * Returns object maximum height
     * @return object height
     */
    public int getComponentMaximumHeight();

    /***
     * Gets unique component id
     * @return id
     */
    public int getComponentId();

    /***
     * Sets name to the component
     * @param name component name
     */
    public void setComponentName(String name);

    /***
     * Gets component name
     * @return component name
     */
    public String getComponentName();

    /***
     * Sets font to the component
     * @param font font
     */
    public void setComponentFont(FontModel font);

    /***
     * Gets component font
     * @return component font
     */
    public FontModel getComponentFont();

    /***
     * Sets component foreground color
     * @param color foreground color
     */
    public void setComponentForeground(ColorModel color);

    /***
     * Sets component foreground color
     * @return foreground color
     */
    public ColorModel getComponentForeground();

    /***
     * Sets component background color
     * @param color background color
     */
    public void setComponentBackground(ColorModel color);

    /***
     * Sets component background color
     * @return background color
     */
    public ColorModel getComponentBackground();

    /***
     * Sets component opaque
     * @param opaque opaque or not
     */
    public void setComponentOpaque(boolean opaque);

    /***
     * Gets component opaque
     * @return opaque or not
     */
    public boolean isComponentOpaque();

    /***
     * Sets container blocked or unblocked
     * @param blocked blocked/unblocked
     * @param blockOwner if blocked, blockOwner
     */
    public void setBlocked(boolean blocked, String blockOwner);

    /***
     * Returns container blocking status
     * @return container blocking status
     */
    public boolean isBlocked();

    /***
     * Returns block owner
     * @return block owner, null if is not blocked
     */
    public String getBlockOwner();
}
