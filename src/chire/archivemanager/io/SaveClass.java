package chire.archivemanager.io;

import arc.Core;
import arc.files.Fi;
import arc.util.Log;

import java.io.*;

@Deprecated
public class SaveClass {

    /**仅支持Serializable*/
    public static void write(Object obj, Fi file)
    {
        FileOutputStream out;
        try {
            out = new FileOutputStream(file.file());
            ObjectOutputStream objOut=new ObjectOutputStream(out);
            objOut.writeObject(obj);
            objOut.flush();
            objOut.close();
        } catch (IOException e) {
            Log.err(e);
        }
    }

    /**仅支持Serializable*/
    public static <T> T read(Class<T> tClass, Fi file) {
        T temp = null;
        FileInputStream in;
        try {
            in = new FileInputStream(file.file());
            ObjectInputStream objIn=new ObjectInputStream(in);
            temp = tClass.cast(objIn.readObject());
            objIn.close();
        } catch (IOException | ClassNotFoundException e) {
            Log.err(e);
        }
        return temp;
    }


//    public static byte[] toByte(Object obj) {
//        try (ByteArrayOutputStream out =new ByteArrayOutputStream();
//                ObjectOutputStream objectOutputStream = new ObjectOutputStream(out)) {
//
//            objectOutputStream.writeObject(obj);
//            objectOutputStream.flush();
//            return out.toByteArray();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    public static <T> T toClass(Class<T> tClass, byte[] bytes) {
//        try (ByteArrayInputStream byteInputStream = new ByteArrayInputStream(bytes);
//                ObjectInputStream b = new ObjectInputStream(byteInputStream)) {
//            return tClass.cast(b.readObject());
//        } catch (IOException | ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
}
