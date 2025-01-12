package chire.archivemanager.io;

import arc.files.Fi;
import arc.func.Prov;
import arc.struct.ArrayMap;
import arc.util.Log;
import arc.util.io.ReusableByteInStream;
import arc.util.serialization.Json;
import arc.util.serialization.UBJsonReader;
import arc.util.serialization.UBJsonWriter;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DataFile {
    protected Fi fi;

    protected final static byte typeBool = 0, typeInt = 1, typeLong = 2, typeFloat = 3, typeString = 4, typeBinary = 5;

    protected UBJsonReader ureader = new UBJsonReader();

    protected Json json = new Json();

    protected ByteArrayOutputStream byteStream = new ByteArrayOutputStream(32);

    protected ReusableByteInStream byteInputStream = new ReusableByteInStream();

    protected HashMap<String, Object> values = new HashMap<>();

    protected boolean modified = false;

    public DataFile(Fi file){
        this.fi = file;
    }

    public void setJson(Json json){
        this.json = json;
    }

    public boolean modified(){
        return modified;
    }

    public Fi getFile(){
        return fi;
    }

    public HashMap<String, Object> getValues(){
        return values;
    }

    public synchronized void putClass(String name, Object value){
        putClass(name, null, value);
    }

    public synchronized void putClass(String name, Class<?> elementType, Object value){
        byteStream.reset();

        json.setWriter(new UBJsonWriter(byteStream));
        json.writeValue(value, value == null ? null : value.getClass(), elementType);

        putObject(name, byteStream.toByteArray());

        modified = true;
    }

    public synchronized void putObject(String name, Object object){
        if(object instanceof Float || object instanceof Integer || object instanceof Boolean || object instanceof Long
                || object instanceof String || object instanceof byte[]){
            values.put(name, object);
            modified = true;
        }else{
            throw new IllegalArgumentException("Invalid object stored: " + (object == null ? null : object.getClass()) + ".");
        }
    }

    public synchronized <K, Y> void putMap(String name, ArrayMap<K, Y> map){
        putClass(name, map.getClass(), map);
    }

    public synchronized <V> void putList(String name, List<V> list){
        putClass(name, list.getClass(), list);
    }

    public synchronized boolean has(String name){
        return values.containsKey(name);
    }

    public byte[] getBytes(String name, byte[] def){
        return (byte[])get(name, def);
    }

    public byte[] getBytes(String name){
        return getBytes(name, null);
    }

    public synchronized Object get(String name, Object def){
        return values.containsKey(name) ? values.get(name) : def;
    }

    public synchronized Object getObject(String name) {
        return get(name, null);
    }

    public synchronized String getString(String name){
        return get(name, "").toString();
    }

    public synchronized <T> T getDataClass(String name, Class<T> type, Class elementType, Prov<T> def){
        try{
            if(!has(name)) return def.get();
            byteInputStream.setBytes(getBytes(name));
            return json.readValue(type, elementType, ureader.parse(byteInputStream));
        }catch(Throwable e){
            Log.warn("出现错误，关于getDataClass");
            Log.warn(e.toString());
            return def.get();
        }
    }

    public <T> T getDataClass(String name, Class<T> type, Prov<T> def){
        return getDataClass(name, type, null, def);
    }

    public <T> T getDataClass(String name, Class<T> type){
        return getDataClass(name, type, ()-> null);
    }

    public <T, V> ArrayMap<T, V> getMap(String name, Class<T> c1, Class<V> c2){
        return getDataClass(name, ArrayMap.class, ArrayMap::new);
    }

    public <V> List<V> getList(String name, Class<V> c){
        return getDataClass(name, List.class, null);
    }

    public synchronized void remove(String name){
        values.remove(name);
        modified = true;
    }

    public synchronized void saveValues(){
        try(DataOutputStream stream = new DataOutputStream(fi.write(false, 8192))){
            stream.writeInt(values.size());

            for(Map.Entry<String, Object> entry : values.entrySet()){
                stream.writeUTF(entry.getKey());

                Object value = entry.getValue();

                if(value instanceof Boolean){
                    stream.writeByte(typeBool);
                    stream.writeBoolean((Boolean)value);
                }else if(value instanceof Integer){
                    stream.writeByte(typeInt);
                    stream.writeInt((Integer)value);
                }else if(value instanceof Long){
                    stream.writeByte(typeLong);
                    stream.writeLong((Long)value);
                }else if(value instanceof Float){
                    stream.writeByte(typeFloat);
                    stream.writeFloat((Float)value);
                }else if(value instanceof String){
                    stream.writeByte(typeString);
                    stream.writeUTF((String)value);
                }else if(value instanceof byte[]){
                    stream.writeByte(typeBinary);
                    stream.writeInt(((byte[])value).length);
                    stream.write((byte[])value);
                }
            }

        }catch(Throwable e){
            //file is now corrupt, delete it
            fi.delete();
            throw new RuntimeException("Error writing preferences: " + fi, e);
        }
    }

    public synchronized void loadValues() throws IOException {
        try(DataInputStream stream = new DataInputStream(fi.read(8192))){
            int amount = stream.readInt();
            //current theory: when corruptions happen, the only things written to the stream are a bunch of zeroes
            //try to anticipate this case and throw an exception when 0 values are written
            if(amount <= 0) throw new IOException("0 values are not allowed.");
            for(int i = 0; i < amount; i++){
                String key = stream.readUTF();
                byte type = stream.readByte();

                switch (type) {
                    case typeBool -> values.put(key, stream.readBoolean());
                    case typeInt -> values.put(key, stream.readInt());
                    case typeLong -> values.put(key, stream.readLong());
                    case typeFloat -> values.put(key, stream.readFloat());
                    case typeString -> values.put(key, stream.readUTF());
                    case typeBinary -> {
                        int length = stream.readInt();
                        byte[] bytes = new byte[length];
                        stream.read(bytes);
                        values.put(key, bytes);
                    }
                    default -> throw new IOException("Unknown key type: " + type);
                }
            }
            //make sure all data was read - this helps with potential corruption
            int end = stream.read();
            if(end != -1){
                throw new IOException("Trailing settings data; expected EOF, but got: " + end);
            }
        }
    }
}
