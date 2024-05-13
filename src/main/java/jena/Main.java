package jena;

public class Main {

    public static void main(String[] args) {

        String nameRdfFile;
    	String urlhdfs;
    	String baseDir;
    	String folderOutputName;
    	String hdfsDirPath;  
    	
        if (args.length == 0) {
        	System.out.println("Usage: java -jar Main.jar <nameRdfFile> <urlhdfs> <baseDir> <folderOutputName> <hdfsDirPath>");
            System.out.println("Where:");
            System.out.println("<nameRdfFile>: Nombre del archivo .ttl junto con su extensión.");
            System.out.println("<urlhdfs>: HDFS URL.");
            System.out.println("<baseDir>: Directorio base.");
            System.out.println("<folderOutputName>: Nombre de la carpeta de salida dentro del directorio base.");
            System.out.println("<hdfsDirPath>: Ruta dentro del sistema hdfs a copiar archivos.");
            System.out.println("Example: java -jar main.jar datos_libros.ttl hdfs://localhost:9000 .\\JenaTaller\\ divided /hadoop/dfs/data/");
            
            System.out.println("Corriendo con valores del ejemplo base ---------");
            
            nameRdfFile = "datos_libros.ttl";
            nameRdfFile = "datos_libros.ttl";
            urlhdfs = "hdfs://localhost:9000";
            baseDir =  ".\\JenaTaller\\";
            folderOutputName = "divided";
            hdfsDirPath = "/hadoop/dfs/data/";
        } else if (args.length == 5) {
            nameRdfFile = args[0];
            urlhdfs = args[1];
            baseDir = args[2];
            folderOutputName = args[3];
            hdfsDirPath = args[4];
        } else {
            System.out.println("Error en los argumentos");
            return;
        }

        
        LocalFileManager fileManager = new LocalFileManager(baseDir, nameRdfFile, folderOutputName);
        System.out.println("-- Eliminando archivos locales viejos --");
        fileManager.deleteFilesinBaseDir();
        System.out.println("-- Terminado eliminacion --");
        fileManager.splitByPredicate();
        fileManager.splitByObjectType();
        
        System.out.println("TERMINADO division ---");
        
        HDFSmanager hdfsfileManager = new HDFSmanager(urlhdfs);
        
        try {
        	hdfsfileManager.deleteDirIfExist(hdfsDirPath);
        	hdfsfileManager.deleteDirIfExist("/hadoop/dfs/output/");
        	hdfsfileManager.deleteDirIfExist("/hadoop/dfs/output1/");
        	hdfsfileManager.uploadFilesinFolderToHDFS(fileManager.getFolderOutputPath(), hdfsDirPath);
           System.out.println("Subido a hadoop");
           hdfsfileManager.runMapReduceCountJob(hdfsDirPath,"/hadoop/dfs/output/");
           hdfsfileManager.runMapReduceGroupByObjectJob(hdfsDirPath, "/hadoop/dfs/output1/");
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        
    } 
}
