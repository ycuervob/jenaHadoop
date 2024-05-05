package jena;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.jena.rdf.model.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.*;

public class RDFSplitter {
	
	private static String WORKING_DIR = "C:\\Users\\yeiso\\OneDrive\\Documentos\\Jena\\";

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
           //uploadFilesToHDFS();
        } catch (Exception e) {
            e.printStackTrace();
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
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(conf);
        
        // Subir archivos generados a HDFS
        Path localPath = new Path(WORKING_DIR+"\\divided\\");
        Path hdfsPath = new Path("/rdf/");
        fs.copyFromLocalFile(localPath, hdfsPath);
        
        fs.close();
    }
}
