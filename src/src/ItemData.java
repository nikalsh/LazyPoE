package src;

import java.io.Serializable;

/**
 *
 * @author nikalsh
 */
public class ItemData implements Serializable {

    private byte[] itemBytes;

    public ItemData(byte[] itemBytes) {
        System.out.println(itemBytes.length);
        this.itemBytes = itemBytes;
    }

    public byte[] getBytes() {
        return itemBytes;
    }
}
