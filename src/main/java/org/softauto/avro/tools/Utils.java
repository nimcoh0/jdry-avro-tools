package org.softauto.avro.tools;

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.io.DecoderFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class Utils {

    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(Utils.class);
    
    static Object jsonToGenericDatum(Schema schema, String jsonData) throws IOException {
        GenericDatumReader<Object> reader = new GenericDatumReader<>(schema);
        Object datum = reader.read(null, DecoderFactory.get().jsonDecoder(schema, jsonData));
        return datum;
    }

    static Object datumFromFile(Schema schema, String file) throws IOException {
        try (DataFileReader<Object> in = new DataFileReader<>(new File(file), new GenericDatumReader<>(schema))) {
            return in.next();
        }
    }

    /**
     * get local class using URLClassLoader
     * @param path
     * @param clazzName
     * @return
     */
    public static Class getClazz(String path,String clazzName){
        Class c = null;
        try{
            String localPath = path.substring(0,path.lastIndexOf("classes")+8);
            String clazz = path.substring(path.lastIndexOf("classes") + 8, path.length()).replace("/", ".")+"."+clazzName;
            URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
            addURL(new File(localPath).toURL(),sysloader);
            c = (Class) sysloader.loadClass(clazz );
        }catch (Exception e){
            logger.error("fail get class "+ path+"/"+clazzName,e);
        }
        return c;
    }

    /**
     * add URL to URLClassLoader
     * @param u
     * @param sysloader
     */
    public static void addURL(URL u, URLClassLoader sysloader)  {
        Class[] parameters = new Class[]{URL.class};
        Class sysclass = URLClassLoader.class;
        try {
            Method method = sysclass.getDeclaredMethod("addURL",parameters);
            method.setAccessible(true);
            method.invoke(sysloader,new Object[]{ u });
        } catch (Throwable t) {
            logger.error("add url fail "+ u ,t);
        }
    }
}
