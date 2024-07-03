package chire.archivemanager.io;

import arc.Files;
import arc.files.Fi;
import arc.struct.ArrayMap;
import arc.util.Log;
import arc.util.serialization.JsonReader;
import arc.util.serialization.JsonValue;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

/**简便设计的json操作工具*/
public class CRJson extends Fi{

    public CRJson(String fileName){
        super(fileName);
    }

    public CRJson(File file){
        super(file);
    }

    public CRJson(String fileName, Files.FileType type){
        super(fileName, type);
    }

    public CRJson(Fi fi){
        super(fi.file());
    }

    public void writeJson(String contents){
        writeString(contents);
    }

    public void writeJson(Object... objects){
        writeJson(toJson(objects));
    }

    /**TODO aaaa，或者有错误的地方？*/
    public <T> T toClassVar(Class<T> c){
        try {
            Field[] fs = c.getFields();
            JsonValue jv = parse();

            T nc = c.getDeclaredConstructor().newInstance();

            for (var f : fs) {
                Object value = null;
                JsonValue jValue = jv.get(f.getName());

                Log.info(jValue.isString());
                if (jValue.isString() || jValue.isObject()) {
                    value = jValue.asString();
                } else if (jValue.isNumber()) {
                    value = jValue.asInt();
                } else if (jValue.isDouble()) {
                    value = jValue.asDouble();
                } else if (jValue.isLong()) {
                    value = jValue.asLong();
                } else if (jValue.isBoolean()) {
                    value = jValue.asBoolean();
                } else if (jValue.isArray()) {
                    value = jValue.asStringArray();
                } else {
                    if (jValue.isNull()) {
                        continue;
                    } else if (!jValue.isValue()){
                        value = jValue.asFloat();
                    }
                }

                c.getDeclaredField(f.getName()).set(nc, value);
            }
            return nc;
        } catch (NoSuchMethodException | NoSuchFieldException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            Log.err(e);
            throw new RuntimeException(e);
        }
    }

    public static String toJson(Object... objects){
        if(objects.length == 0) return "{}";
        StringBuilder buffer = new StringBuilder();
        buffer.append("{\"");
        buffer.append(objects[0].toString());
        buffer.append("\":");
        buffer.append(objects.length >= 2 ? toNorms(objects[1]) : "\"\"");
        for(int i = 3; i < objects.length; i += 2){
            buffer.append(", ");
            buffer.append("\"");
            buffer.append(objects[i-1].toString());
            buffer.append("\"");
            buffer.append(':');
            buffer.append(toNorms(objects[i]));
        }
        buffer.append('}');
        return buffer.toString();
    }

    public static String toJson(Object[] keys, Object[] values){
        if(keys.length == 0 || values.length == 0 || keys.length != values.length) return "{}";
        StringBuilder buffer = new StringBuilder();
        buffer.append("{\"");
        buffer.append(keys[0].toString());
        buffer.append("\":");
        buffer.append(toNorms(values[0]));
        for(int i = 1; i < keys.length; i ++){
            buffer.append(", ");
            buffer.append("\"");
            buffer.append(keys[i-1].toString());
            buffer.append("\"");
            buffer.append(':');
            buffer.append(toNorms(values[i]));
        }
        buffer.append('}');
        return buffer.toString();
    }

    public static String toNorms(Object text){
        if (text == null) return "\"\"";
        if (text instanceof String t) {
            return "\""+ t +"\"";
        } else if (text instanceof Boolean t){
            return t ? "true" : "false";
        } else if (text instanceof Integer t) {
            return Integer.toString(t);
        } else if (text instanceof Double t) {
            return Double.toString(t);
        } else if (text instanceof Float f) {
            return Float.toString(f);
        } else {
            return "\""+ text +"\"";
        }
    }

    public JsonValue parse(){
        return new JsonReader().parse(readString());
    }

    public static String getName(JsonValue value){
        return value.parent().get(value.name).toString();
    }

    public String getStr(String name){
        try {
            return parse().getString(name);
        } catch (IllegalArgumentException e){
            return null;
        }
    }

    public Integer getInt(String name){
        try {
            return parse().getInt(name);
        } catch (IllegalArgumentException e){
            return null;
        }
    }

    public Float getFloat(String name){
        try {
            return parse().getFloat(name);
        } catch (IllegalArgumentException e){
            return null;
        }
    }

    public Double getDouble(String name){
        try {
            return parse().getDouble(name);
        } catch (IllegalArgumentException e){
            return null;
        }
    }

    public Boolean getBoolean(String name){
        try {
            return parse().getBoolean(name);
        } catch (IllegalArgumentException e){
            return null;
        }
    }

    public Long getLong(String name){
        try {
            return parse().getLong(name);
        } catch (IllegalArgumentException e){
            return null;
        }
    }
}
