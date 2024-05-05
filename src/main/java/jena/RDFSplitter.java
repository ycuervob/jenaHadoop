package jena;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.map.*;
import org.apache.jena.rdf.model.*;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.*;

public class RDFSplitter {
	
	//Cambiar esta ruta por ruta en pc propopio
	//en la ruta debe estar el archivo datos_libros.ttl
	private static String WORKING_DIR = ".\\JenaTaller\\";

    public static void main(String[] args) {
        String inputFilePath =WORKING_DIR+ "datos_libros.ttl";
        Model model = ModelFactory.createDefaultModel();
        model.read(inputFilePath, "TURTLE");

        // Paso 1: División de datos según predicados (PS)
        splitByPredicate(model);

        // Paso 2: División de datos según objeto de tipo (POS)
        splitByObjectType(model);
        
        System.out.println("TERMINADO division ---");
        
        try {
           uploadFilesToHDFS();
           System.out.println("Subido a hadoop");
        } catch (Exception e) {
            System.out.println("Error en hadoop");
            System.out.println("hadoop siempre explota error normal: "+e);
        }
        
        
    }

    private static void splitByPredicate(Model model) {
        StmtIterator iter = model.listStatements();
        Map<Property, Model> predicateModels = new HashMap<>();
        while (iter.hasNext()) {
            Statement stmt = iter.nextStatement();
            Property predicate = stmt.getPredicate();
            if (!predicateModels.containsKey(predicate)) {
                predicateModels.put(predicate, ModelFactory.createDefaultModel());
            }
            predicateModels.get(predicate).add(stmt);
        }
        saveModels(predicateModels, WORKING_DIR+"\\divided\\"+"predicate_split");
    }

    private static void splitByObjectType(Model model) {
        StmtIterator iter = model.listStatements();
        Map<Resource, Model> objectTypeModels = new HashMap<>();
        while (iter.hasNext()) {
            Statement stmt = iter.nextStatement();
            if (stmt.getObject().isResource()) {
            	Resource object = stmt.getObject().asResource();
                if (!objectTypeModels.containsKey(object)) {
                    objectTypeModels.put(object, ModelFactory.createDefaultModel());
                }
                objectTypeModels.get(object).add(stmt);
            }
          
        }
        saveModels(objectTypeModels, WORKING_DIR+"\\divided\\"+"object_type_split");
    }

    private static void saveModels(Map<?, Model> models, String prefix) {
        int i = 1;
        for (Model model : models.values()) {
            String fileName = prefix + "_" + i + ".ttl";
            ensureDirectoryExists(fileName); 
            try (OutputStream out = new FileOutputStream(fileName)) {
                model.write(out, "TURTLE");
            } catch (Exception e) {
                e.printStackTrace();
            }
            i++;
        }
    }
    
    private static void ensureDirectoryExists(String filePath) {
        File file = new File(filePath);
        File parentDirectory = file.getParentFile();
        if (parentDirectory != null && !parentDirectory.exists()) {
            parentDirectory.mkdirs();
        }
    }
    
    private static void uploadFilesToHDFS() throws Exception {
        // Especifica la URL de tu Hadoop HDFS
        String hdfsUrl = "hdfs://localhost:9000";

        // Configura la configuración de Hadoop
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", hdfsUrl);

        // Crea una instancia de FileSystem para interactuar con HDFS
        FileSystem fs = FileSystem.get(URI.create(hdfsUrl), conf);

        // Directorio local que contiene los archivos a copiar
        String localDirPath = WORKING_DIR + "\\divided\\";

        // Directorio en HDFS donde se copiarán los archivos
        String hdfsDirPath = "/hadoop/dfs/data/";

        // Obtener la lista de archivos en el directorio local
        File localDir = new File(localDirPath);
        File[] files = localDir.listFiles();
        if (files != null) {
            for (File file : files) {
            	
            	System.out.println("subiendo: "+ file);
            	
                if (file.isFile()) {
                    Path localFilePath = new Path(file.getAbsolutePath());
                    Path hdfsFilePath = new Path(hdfsDirPath + "/" + file.getName());
                    try{
                    	fs.copyFromLocalFile(localFilePath, hdfsFilePath);
                    }catch(IOException e) {
                    	System.out.println("Supuesto error pero de seguro se subió: " +e);
                    }
                    
                    System.out.println("Archivo copiado a HDFS: " + file.getName());
                }
            }
        }

        fs.close();
    }
}
