package utility;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class Serializator {

    public static byte[] serializeClass(Object object) {
        Properties properties = classToProperties(object);
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            properties.store(byteArrayOutputStream, null);
            byte[] result = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
            return result;
        } catch (IOException exception) {
            System.out.println("Cannot serialize class: " + exception.getMessage());
            return null;
        }
    }

    private static Properties classToProperties(Object object) {
        Coder coder = new Coder();
        return coder.toProperties(object);
    }

    public static Object deserializeClass(byte[] bytes) {
        try {
            Properties properties = new Properties();
            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(bytes);
            properties.load(byteInputStream);
            return classFromProperties(properties);
        } catch (IOException exception) {
            System.out.println("Cannot deserialize class: " + exception.getMessage());
            return null;
        }
    }

    public static Object classFromProperties(Properties properties) {
        Decoder decoder = new Decoder();
        return decoder.fromProperties(properties);
    }

    static List<Field> makeFieldsList(Class mClass) {
        ArrayList<Field> fieldsList = new ArrayList<>();
        Class currentClass = mClass;
        while (currentClass != null) {
            Field[] fields = currentClass.getDeclaredFields();
            fieldsList.addAll(Arrays.asList(fields));
            currentClass = currentClass.getSuperclass();
        }
        Iterator<Field> fieldIterator = fieldsList.iterator();
        while (fieldIterator.hasNext()) {
            Field field = fieldIterator.next();
            if (Modifier.isStatic(field.getModifiers())) fieldIterator.remove();
            if (field.getName().startsWith("class$")) fieldIterator.remove();
        }
        return fieldsList;
    }


    static class Coder {

        interface FieldReader {
            String getValue(Field field, Object object) throws IllegalAccessException;
        }

        static HashMap<Class, FieldReader> readers = new HashMap<>();

        static {
            readers.put(Integer.TYPE, (field, object) -> String.valueOf(field.getInt(object)));
            readers.put(Character.TYPE, (field, object) -> String.valueOf(field.getChar(object)));
            readers.put(Byte.TYPE, (field, object) -> String.valueOf(field.getByte(object)));
            readers.put(Short.TYPE, (field, object) -> String.valueOf(field.getShort(object)));
            readers.put(Long.TYPE, (field, object) -> String.valueOf(field.getLong(object)));
            readers.put(Boolean.TYPE, (field, object) -> String.valueOf(field.getBoolean(object)));
            readers.put(Double.TYPE, (field, object) -> String.valueOf(field.getDouble(object)));
            readers.put(Float.TYPE, (field, object) -> String.valueOf(field.getFloat(object)));
        }

        private Properties propertiesData;
        private int classCount;
        private int arrayCount;
        private HashMap<IdentityKeyWrapper, String> processedObjects;

        Properties toProperties(Object object) {
            propertiesData = new Properties();
            classCount = 0;
            arrayCount = 0;
            processedObjects = new HashMap<>();
            String strMainObjectId = storeObject(object);
            propertiesData.put("mainObjectID", String.valueOf(strMainObjectId));
            return propertiesData;
        }

        private String storeObject(Object object) {
            if (object == null) return null;
            String strResult;
            Class mClass = object.getClass();
            if (mClass.equals(String.class)) strResult = (String) object;
            else if (mClass.isArray()) strResult = storeArrayImplementation(object);
            else strResult = storeObjectImplementation(object);
            return strResult;
        }

        private String storeObjectImplementation(Object object) {
            if (object == null) return null;
            IdentityKeyWrapper objectKey = new IdentityKeyWrapper(object);
            String objectID = processedObjects.get(objectKey);
            if (objectID != null) return objectID;
            classCount++;
            String strResult = "o" + classCount;
            processedObjects.put(objectKey, strResult);
            String strKeyPrefix = strResult + ".";
            String strFieldPrefix = strKeyPrefix + "f.";
            Class mClass = object.getClass();
            propertiesData.put(strKeyPrefix + "className", mClass.getName());
            List<Field> fieldsList = makeFieldsList(mClass);
            for (Field field : fieldsList) {
                Class fieldType = field.getType();
                String strFieldName = field.getName();
                String strFieldValue;
                try {
                    try {
                        field.setAccessible(true);
                    } catch (SecurityException exception) {
                        System.out.println("Security exception: " + exception.getMessage());
                    }
                    FieldReader fieldReader = readers.get(fieldType);
                    if (fieldReader != null) strFieldValue = fieldReader.getValue(field, object);
                    else strFieldValue = storeObject(field.get(object));
                } catch (IllegalAccessException exception) {
                    System.out.println("No access to field: " + exception.getMessage());
                    strFieldValue = null;
                }
                if (strFieldValue != null)
                    propertiesData.setProperty(strFieldPrefix + strFieldName, String.valueOf(strFieldValue));
                else propertiesData.setProperty(strFieldPrefix + strFieldName + ".isNull", "true");
            }
            return strResult;
        }

        private String storeArrayImplementation(Object object) {
            if (object == null) return null;
            IdentityKeyWrapper objectKey = new IdentityKeyWrapper(object);
            String objectID = processedObjects.get(objectKey);
            if (objectID != null) return objectID;
            arrayCount++;
            String strResult = "a" + arrayCount;
            processedObjects.put(objectKey, strResult);
            Class mClass = object.getClass();
            Class fieldType = mClass.getComponentType();
            String strKeyPrefix = strResult + ".";
            String strFieldPrefix = strKeyPrefix + "f.";
            int arrayLength = Array.getLength(object);
            propertiesData.put(strKeyPrefix + "className", fieldType.getName());
            propertiesData.put(strKeyPrefix + "length", String.valueOf(arrayLength));
            String strFieldValue;
            for (int arrayIndex = 0; arrayIndex < arrayLength; arrayIndex++) {
                if (fieldType.equals(Integer.TYPE))
                    strFieldValue = String.valueOf(Array.getInt(object, arrayIndex));
                else if (fieldType.equals(Character.TYPE))
                    strFieldValue = String.valueOf(Array.getChar(object, arrayIndex));
                else if (fieldType.equals(Byte.TYPE))
                    strFieldValue = String.valueOf(Array.getByte(object, arrayIndex));
                else if (fieldType.equals(Short.TYPE))
                    strFieldValue = String.valueOf(Array.getShort(object, arrayIndex));
                else if (fieldType.equals(Long.TYPE))
                    strFieldValue = String.valueOf(Array.getLong(object, arrayIndex));
                else if (fieldType.equals(Boolean.TYPE))
                    strFieldValue = String.valueOf(Array.getBoolean(object, arrayIndex));
                else if (fieldType.equals(Double.TYPE))
                    strFieldValue = String.valueOf(Array.getDouble(object, arrayIndex));
                else if (fieldType.equals(Float.TYPE))
                    strFieldValue = String.valueOf(Array.getFloat(object, arrayIndex));
                else {
                    Object value = Array.get(object, arrayIndex);
                    strFieldValue = storeObject(value);
                }
                String strFieldName = "" + arrayIndex;
                if (strFieldValue != null)
                    propertiesData.setProperty(strFieldPrefix + strFieldName, String.valueOf(strFieldValue));
                else propertiesData.setProperty(strFieldPrefix + strFieldName + ".isNull", "true");
            }
            return strResult;
        }
    }

    protected static class Decoder {
        private Properties propertiesData;
        private HashMap<String, Object> processedObjects;

        Object fromProperties(Properties properties) {
            propertiesData = properties;
            processedObjects = new HashMap<>();
            String strMainObjectID = (String) propertiesData.get("mainObjectID");
            return restoreObject(strMainObjectID);
        }

        private Object restoreObject(String strValue) {
            if (strValue == null) return null;
            Object result;
            if (strValue.startsWith("a")) result = restoreArrayImplementation(strValue);
            else if (strValue.startsWith("o")) result = restoreObjectImplementation(strValue);
            else result = strValue;
            return result;
        }

        private Object restoreObjectImplementation(String strID) {
            if (strID == null || Objects.equals(strID, "o0")) return null;
            Object result = processedObjects.get(strID);
            if (result == null)
                try {
                    String strKeyPrefix = strID + ".";
                    String strFieldPrefix = strKeyPrefix + "f.";
                    String strClassType = (String) propertiesData.get(strKeyPrefix + "className");
                    Class mClass = Class.forName(strClassType);
                    Constructor constructor = Class.forName(strClassType).getDeclaredConstructor();
                    try {
                        constructor.setAccessible(true);
                    } catch (SecurityException exception) {
                        System.out.println("Security exception: " + exception.getMessage());
                    }
                    result = constructor.newInstance();
                    processedObjects.put(strID, result);
                    List<Field> fieldsList = makeFieldsList(mClass);
                    for (Field field : fieldsList) {
                        Class fieldType = field.getType();
                        String strFieldName = field.getName();
                        String strFieldValue = propertiesData.getProperty(strFieldPrefix + strFieldName);
                        boolean bIsNull = Boolean.parseBoolean(
                                propertiesData.getProperty(strFieldPrefix + strFieldName + ".isNull", "false"));
                        try {
                            try {
                                field.setAccessible(true);
                            } catch (SecurityException exception) {
                                System.out.println("Security exception: " + exception.getMessage());
                            }
                            if (fieldType.equals(Integer.TYPE))
                                field.setInt(result, Integer.parseInt(strFieldValue));
                            else if (fieldType.equals(Character.TYPE))
                                field.setChar(result, strFieldValue.charAt(0));
                            else if (fieldType.equals(Byte.TYPE))
                                field.setByte(result, Byte.parseByte(strFieldValue));
                            else if (fieldType.equals(Short.TYPE))
                                field.setShort(result, Short.parseShort(strFieldValue));
                            else if (fieldType.equals(Long.TYPE))
                                field.setLong(result, Long.parseLong(strFieldValue));
                            else if (fieldType.equals(Boolean.TYPE))
                                field.setBoolean(result, Boolean.parseBoolean(strFieldValue));
                            else if (fieldType.equals(Double.TYPE))
                                field.setDouble(result, Double.parseDouble(strFieldValue));
                            else if (fieldType.equals(Float.TYPE))
                                field.setFloat(result, Float.parseFloat(strFieldValue));
                            else if (fieldType.equals(String.class))
                                field.set(result, strFieldValue);
                            else {
                                if (bIsNull) field.set(result, null);
                                else field.set(result, restoreObject(strFieldValue));
                            }
                        } catch (IllegalAccessException exception) {
                            System.out.println("No access to field: " + exception.getMessage());
                        }
                    }
                } catch (Exception exception) {
                    System.out.println("Can not restore class: " + exception.getMessage());
                    result = null;
                }
            return result;
        }

        private Object restoreArrayImplementation(String strID) {
            if (strID == null || Objects.equals(strID, "a0")) return null;
            Object result = processedObjects.get(strID);
            if (result == null)
                try {
                    String strKeyPrefix = strID + ".";
                    String strFieldPrefix = strKeyPrefix + "f.";
                    String strComponentType = (String) propertiesData.get(strKeyPrefix + "className");
                    int arrayLength = Integer.parseInt((String) propertiesData.get(strKeyPrefix + "length"));
                    Class fieldType;
                    if (Integer.TYPE.getName().equals(strComponentType)) fieldType = Integer.TYPE;
                    else if (Character.TYPE.getName().equals(strComponentType)) fieldType = Character.TYPE;
                    else if (Byte.TYPE.getName().equals(strComponentType)) fieldType = Byte.TYPE;
                    else if (Short.TYPE.getName().equals(strComponentType)) fieldType = Short.TYPE;
                    else if (Long.TYPE.getName().equals(strComponentType)) fieldType = Long.TYPE;
                    else if (Boolean.TYPE.getName().equals(strComponentType)) fieldType = Boolean.TYPE;
                    else if (Double.TYPE.getName().equals(strComponentType)) fieldType = Double.TYPE;
                    else if (Float.TYPE.getName().equals(strComponentType)) fieldType = Float.TYPE;
                    else if (String.class.getName().equals(strComponentType)) fieldType = String.class;
                    else fieldType = Class.forName(strComponentType);
                    result = Array.newInstance(fieldType, arrayLength);
                    processedObjects.put(strID, result);
                    for (int arrayIndex = 0; arrayIndex < arrayLength; arrayIndex++) {
                        String strFieldName = "" + arrayIndex;
                        String strFieldValue = propertiesData.getProperty(strFieldPrefix + strFieldName);
                        boolean bIsNull = Boolean.parseBoolean(
                                propertiesData.getProperty(strFieldPrefix + strFieldName + ".isNull", "false"));
                        if (fieldType.equals(Integer.TYPE))
                            Array.setInt(result, arrayIndex, Integer.parseInt(strFieldValue));
                        else if (fieldType.equals(Character.TYPE))
                            Array.setChar(result, arrayIndex, strFieldValue.charAt(0));
                        else if (fieldType.equals(Byte.TYPE))
                            Array.setByte(result, arrayIndex, Byte.parseByte(strFieldValue));
                        else if (fieldType.equals(Short.TYPE))
                            Array.setShort(result, arrayIndex, Short.parseShort(strFieldValue));
                        else if (fieldType.equals(Long.TYPE))
                            Array.setLong(result, arrayIndex, Long.parseLong(strFieldValue));
                        else if (fieldType.equals(Boolean.TYPE))
                            Array.setBoolean(result, arrayIndex, Boolean.parseBoolean(strFieldValue));
                        else if (fieldType.equals(Double.TYPE))
                            Array.setDouble(result, arrayIndex, Double.parseDouble(strFieldValue));
                        else if (fieldType.equals(Float.TYPE))
                            Array.setDouble(result, arrayIndex, Float.parseFloat(strFieldValue));
                        else if (fieldType.equals(String.class))
                            Array.set(result, arrayIndex, strFieldValue);
                        else {
                            if (bIsNull) Array.set(result, arrayIndex, null);
                            else Array.set(result, arrayIndex, restoreObject(strFieldValue));
                        }
                    }
                } catch (Exception exception) {
                    System.out.println("Cannot restore array: " + exception.getMessage());
                    result = null;
                }
            return result;
        }
    }

    private static class IdentityKeyWrapper {

        private Object objectKey;

        IdentityKeyWrapper(Object objectKey) {
            this.objectKey = objectKey;
        }

        public boolean equals(Object otherObject) {
            return otherObject instanceof IdentityKeyWrapper
                    && ((IdentityKeyWrapper) otherObject).objectKey == this.objectKey;
        }
    }
}
