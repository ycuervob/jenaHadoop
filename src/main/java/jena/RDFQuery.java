package jena;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class RDFQuery {
	
	  public static class QueryMapper extends Mapper<LongWritable, Text, Text, Text> {

	        @Override
	        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
	            // Convertir el valor de texto en un modelo RDF
	            Model model = ModelFactory.createDefaultModel();
	            ByteArrayInputStream inputStream = new ByteArrayInputStream(value.toString().getBytes());
	            model.read(inputStream, null, "TURTLE");

	            // Aquí puedes realizar consultas SPARQL en el modelo RDF
	            // y emitir (clave, valor) para pasar datos al Reducer
	            // Por ejemplo:
	            String queryString = "SELECT ?s WHERE { ?s ?p ?o }";
	            Query query = QueryFactory.create(queryString);
	            QueryExecution qe = QueryExecutionFactory.create(query, model);
	            ResultSet results = qe.execSelect();
	            while (results.hasNext()) {
	                QuerySolution soln = results.nextSolution();
	                RDFNode subj = soln.get("s");
	                String subjURI = subj.toString();
	                context.write(new Text(subjURI), new Text(model.toString()));
	            }
	        }
	    }

	    public static class QueryReducer extends Reducer<Text, Text, Text, Text> {

	        @Override
	        protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
	            // Aquí puedes procesar los datos emitidos por el Mapper
	            // por ejemplo, fusionar todos los modelos RDF relacionados con una clave común
	            Model mergedModel = ModelFactory.createDefaultModel();
	            for (Text value : values) {
	                Model model = ModelFactory.createDefaultModel();
	                ByteArrayInputStream inputStream = new ByteArrayInputStream(value.toString().getBytes());
	                model.read(inputStream, null, "TURTLE");
	                mergedModel.add(model);
	            }

	            // Ahora puedes ejecutar consultas SPARQL en el modelo fusionado
	            // o realizar otras operaciones según sea necesario
	            // Por ejemplo, aquí ejecutaremos una consulta para obtener todas las propiedades de la clave
	            String queryString = "SELECT ?p ?o WHERE { <" + key.toString() + "> ?p ?o }";
	            Query query = QueryFactory.create(queryString);
	            QueryExecution qe = QueryExecutionFactory.create(query, mergedModel);
	            ResultSet results = qe.execSelect();
	            while (results.hasNext()) {
	                QuerySolution soln = results.nextSolution();
	                RDFNode prop = soln.get("p");
	                RDFNode obj = soln.get("o");
	                context.write(new Text(prop.toString()), new Text(obj.toString()));
	            }
	        }
	    }

}
