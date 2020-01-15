package rslib.gui.container;

import rslib.gui.BasicComponent;

/***
 * Represents a board container
 */
public interface BoardContainer extends BasicComponent {

    /** Container types */
    public enum ContainerType {

        TEXT_CONTAINER ("Text container"),
        IMAGE_CONTAINER ("Image container"),
        FILE_CONTAINER ("File container");

        /** Name */
        private final String name;

        /***
         * Constructor
         * @param name name
         */
        private ContainerType(String name) {
            this.name = name;
        }

        /***
         * Returns name
         * @return name
         */
        public String toString(){
            return name;
        }
    }

    /** If container needed to be moved to front */
    public final int TO_FRONT = -100;

    /** If container needed to be moved to background */
    public final int TO_BACKGROUND = -300;

    /** Background layer position */
    public final int BACKGROUND_LAYER = 1;

    /***
     * Returns container type
     * @return container type
     */
    public ContainerType getType();

    /***
     * Sets layer
     * @param layer layer
     */
    public void setLayer(int layer);

    /***
     * Gets layer
     * @return layer
     */
    public int getLayer();

    /***
     * Clears container content
     */
    public void clearContainer();

    /***
     * Sets content to the container
     * @param serializableContainer saved data
     */
    public void setContent(ExternalizableContainer serializableContainer);

    /***
     * Converts container into serializable info
     * @return serializable info
     */
    public ExternalizableContainer toExternalizable();
}
