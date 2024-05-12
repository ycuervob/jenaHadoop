package jena;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.jena.hadoop.rdf.io.input.TriplesInputFormat;
import org.apache.jena.hadoop.rdf.io.output.ntriples.NTriplesNodeOutputFormat;
import org.apache.jena.hadoop.rdf.mapreduce.count.NodeCountReducer;
import org.apache.jena.hadoop.rdf.mapreduce.count.TripleNodeCountMapper;
import org.apache.jena.hadoop.rdf.types.NodeWritable;

public class HDFSmanager {

	private String hdfsUrl;

	private Configuration conf;

	public HDFSmanager(String hdfsUrl, Configuration conf) {
		super();
		this.hdfsUrl = hdfsUrl;
		this.conf = conf;
	}

	public HDFSmanager(String hdfsUrl) {
		super();
		this.hdfsUrl = hdfsUrl;
		this.conf = new Configuration();
		this.conf.set("fs.defaultFS", hdfsUrl);
	}

	public void uploadFilesinFolderToHDFS(String localSource, String destinationHdfs) throws Exception {

		// Crea una instancia de FileSystem para interactuar con HDFS
		FileSystem fs = FileSystem.get(URI.create(hdfsUrl), conf);

		// Obtener la lista de archivos en el directorio local
		File localDir = new File(localSource);
		File[] files = localDir.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isFile()) {
					Path localFilePath = new Path(file.getAbsolutePath());
					Path hdfsFilePath = new Path(destinationHdfs + "/" + file.getName());
					try {
						fs.copyFromLocalFile(localFilePath, hdfsFilePath);
					} catch (IOException e) {
						System.out.println("Supuesto error pero de seguro se subió: " + e);
					}

					System.out.println("Archivo copiado a HDFS: " + file.getName());
				}
			}
		}

		fs.close();
	}

	public void runMapReduceCountJob(String source, String destination) throws Exception {

		try {
			// Create job
			Job job = Job.getInstance(conf);
			job.setJarByClass(HDFSmanager.class);
			job.setJobName("RDF Triples Node Usage Count");

			// Map/Reduce classes
			job.setMapperClass(TripleNodeCountMapper.class);
			job.setMapOutputKeyClass(NodeWritable.class);
			job.setMapOutputValueClass(LongWritable.class);
			job.setReducerClass(NodeCountReducer.class);

			// Input and Output
			job.setInputFormatClass(TriplesInputFormat.class);
			job.setOutputFormatClass(NTriplesNodeOutputFormat.class);
			FileInputFormat.setInputPaths(job, new Path(source));
			FileOutputFormat.setOutputPath(job, new Path(destination));

			// Launch the job and await completion
			job.submit();
			if (job.monitorAndPrintJob()) {
				// OK
				System.out.println("Counter Completed");
			} else {
				// Failed
				System.err.println("Failed");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	
	public void runMapReduceGroupByObjectJob(String source, String destination) throws Exception {

        try {
            // Create job
            Job job = Job.getInstance(conf);
            job.setJarByClass(HDFSmanager.class);
            job.setJobName("RDF Triples Group by Object");

            // Map/Reduce classes
            job.setMapperClass(TripleNodeGroupMapper.class);
            job.setMapOutputKeyClass(NodeWritable.class);
            job.setMapOutputValueClass(NodeWritable.class);
            job.setReducerClass(NodeGroupReducer.class);

            // Input and Output
            job.setInputFormatClass(TriplesInputFormat.class);
            job.setOutputFormatClass(NTriplesNodeOutputFormat.class);
            FileInputFormat.setInputPaths(job, new Path(source));
            FileOutputFormat.setOutputPath(job, new Path(destination));

            // Launch the job and await completion
            job.submit();
            if (job.monitorAndPrintJob()) {
                // OK
                System.out.println("Grouping Completed");
            } else {
                // Failed
                System.err.println("Failed");
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

	public Configuration getConf() {
		return conf;
	}

	public void setConf(Configuration conf) {
		this.conf = conf;
	}

	public String getHdfsUrl() {
		return hdfsUrl;
	}

	public void setHdfsUrl(String hdfsUrl) {
		this.hdfsUrl = hdfsUrl;
	}

	public void deleteDirIfExist(String dir) throws IOException {
		FileSystem fs = FileSystem.get(URI.create(hdfsUrl), conf);

		// Obtener la lista de archivos en el directorio local
		File localDir = new File(dir);
		File[] files = localDir.listFiles();
		// Eliminar archivos

		if (files != null) {
			for (File file : files) {
				if (file.isFile()) {
					Path hdfsFilePath = new Path(dir + "/" + file.getName());
					fs.delete(hdfsFilePath, true);
					System.out.println("Archivo eliminado de HDFS: " + file.getName());
				}
			}
		}

		// Eliminar directorio
		fs.delete(new Path(dir), true);
		System.out.println("Directorio eliminado de HDFS: " + dir);

		fs.close();
	}

}
