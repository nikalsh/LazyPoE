package lazypoe;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nikalsh
 */

//older version of lazyPoE, not used anymore
public class lazyPoe {

    File serFile = null;
    Scanner scan;
    byte[] currItem = new byte[]{0};
    List<ItemData> inMemoryItemList;

    public lazyPoe() throws IOException {
        inMemoryItemList = new ArrayList<>();
        serFile = new File("items.ser");

        if (!serFile.exists()) {
            serFile.createNewFile();
        } else {
//            System.out.println("file already exists");
        }
        forceRead();
    }

    public boolean evalItem(String item) {
        for (ItemData data : inMemoryItemList) {
            if (Arrays.equals(data.getBytes(), item.getBytes())) {
                System.out.println("we keep");
                return false;
            }
        }
        System.out.println("we vendor");
        return true;

    }

    public void tempSave(String item) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                inMemoryItemList.add(new ItemData(item.getBytes()));
                inMemoryItemList.forEach((e -> System.out.println(e)));
                System.out.println(inMemoryItemList.size());
            }

        }).start();

    }

    public void forceSave() {


        try (FileOutputStream fos = new FileOutputStream(serFile);
                ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            for (ItemData item : inMemoryItemList) {
                try {
                    oos.writeObject(item);
                } catch (IOException ex) {
                    Logger.getLogger(lazyPoe.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void forceRead() {
        ItemData data = null;
        try (FileInputStream fis = new FileInputStream(serFile);
                ObjectInputStream ois = new ObjectInputStream(fis)) {
            while (true) {
                try {
                    data = (ItemData) ois.readObject();
                    inMemoryItemList.add(data);
                } catch (EOFException e) {
                    System.out.println("reached EOF.");
                    ois.close();
                    break;
                }
            }

        } catch (IOException e) {
            System.out.println("io something");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(lazyPoe.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
//            e.printStackTrace();
            System.out.println("done");
        }

    }

    public ArrayList<ItemData> tempRead() {
        System.out.println("io something");

        ArrayList<ItemData> temp = new ArrayList<>();
        ItemData data = null;
        try (FileInputStream fis = new FileInputStream(serFile);
                ObjectInputStream ois = new ObjectInputStream(fis)) {

            data = (ItemData) ois.readObject();
            temp.add(data);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return temp;
    }

    public void removeFromMemory(String item) {
        for (ItemData data : inMemoryItemList) {
            if (Arrays.equals(data.getBytes(), item.getBytes())) {
                inMemoryItemList.remove(data);
            }
        }

    }

}
