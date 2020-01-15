package gui.container;

import java.awt.Dimension;

/***
 * Represents a JLabel that do not changes its sizes even if it is empty
 */
public class StableLabel extends AnimatableLabel {

    @Override
    public Dimension getMinimumSize() {
        int size = getFont().getSize();
        return new Dimension(super.getMinimumSize().width, size);
    }

    @Override
    public Dimension getMaximumSize() {
        int size = getFont().getSize();
        return new Dimension(super.getMaximumSize().width, size);
    }

    @Override
    public Dimension getPreferredSize() {
        int size = getFont().getSize();
        return new Dimension(super.getPreferredSize().width, size);
    }   
}
