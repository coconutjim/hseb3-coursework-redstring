package rslib.gui.container.image;

import rslib.gui.container.BoardContainer;

/***
 * Represents a container that holds image
 */
public interface ImageContainer extends BoardContainer {

    /** Maximum image width in pixels */
    public final int IMAGE_MAXIMUM_WIDTH = 1024;

    /** Maximum image height in pixels */
    public final int IMAGE_MAXIMUM_HEIGHT = 1024;

    /***
     * Sets image to the container
     * @param image image
     */
    public void setImage(ImageModel image);

    /***
     * Gets container image
     * @return container image
     */
    public ImageModel getImage();

}
