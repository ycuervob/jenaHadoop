package jena;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

public class LocalFileManager {

	private String nameRdfFile;
	private String baseDir;
	private String folderOutputName;
	private String folderOutputPath;
	private String hdfsDirPath;
	private String inputFilePath;
	
	private Model model;
	
	public LocalFileManager(String baseDir, String nameRdfFile, String folderOutputName) {
		this.model = ModelFactory.createDefaultModel();
		this.nameRdfFile = nameRdfFile;
		this.folderOutputName = folderOutputName;
		this.baseDir = baseDir;
		this.folderOutputPath = this.baseDir + this.folderOutputName;
		this.inputFilePath = this.baseDir + this.nameRdfFile;
		this.model.read(inputFilePath, "TURTLE");
	}
	
	public void splitByPredicate() {
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
        saveModels(predicateModels, folderOutputPath+"predicate_split");
    }
	
	public void splitByObjectType() {
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
        saveModels(objectTypeModels, folderOutputPath+"object_type_split");
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
	
	public String getNameRdfFile() {
		return nameRdfFile;
	}
	public void setNameRdfFile(String nameRdfFile) {
		this.nameRdfFile = nameRdfFile;
	}
	public String getBaseDir() {
		return baseDir;
	}
	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}
	public String getFolderOutputPath() {
		return folderOutputPath;
	}
	public void setFolderOutputPath(String folderOutputPath) {
		this.folderOutputPath = folderOutputPath;
	}
	public String getHdfsDirPath() {
		return hdfsDirPath;
	}
	public void setHdfsDirPath(String hdfsDirPath) {
		this.hdfsDirPath = hdfsDirPath;
	}
	public String getInputFilePath() {
		return inputFilePath;
	}
	public void setInputFilePath(String inputFilePath) {
		this.inputFilePath = inputFilePath;
	}
	public String getFolderOutputName() {
		return folderOutputName;
	}
	public void setFolderOutputName(String folderOutputName) {
		this.folderOutputName = folderOutputName;
	}
	public Model getModel() {
		return model;
	}
	public void setModel(Model model) {
		this.model = model;
	}
}
