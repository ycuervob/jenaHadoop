package jena;

public class Main {

    public static void main(String[] args) {
    	String nameRdfFile  = "datos_libros.ttl";
    	String urlhdfs = "hdfs://localhost:9000";
    	String baseDir =  ".\\JenaTaller\\";
    	String folderOutputName = "divided";
    	String hdfsDirPath = "/hadoop/dfs/data/";  	
        
        LocalFileManager fileManager = new LocalFileManager(baseDir, nameRdfFile, folderOutputName);
        fileManager.splitByPredicate();
        fileManager.splitByObjectType();
        
        System.out.println("TERMINADO division ---");
        
        HDFSmanager hdfsfileManager = new HDFSmanager(urlhdfs);
        
        try {
        	hdfsfileManager.uploadFilesinFolderToHDFS(fileManager.getFolderOutputPath(), hdfsDirPath);
           System.out.println("Subido a hadoop");
           hdfsfileManager.runMapReduceJob();
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        
    } 
}
