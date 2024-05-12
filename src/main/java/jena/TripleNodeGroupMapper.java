package jena;


import org.apache.jena.hadoop.rdf.types.NodeWritable;
import org.apache.jena.hadoop.rdf.types.TripleWritable;

import java.io.IOException;


import org.apache.jena.graph.Triple;

public class TripleNodeGroupMapper<TKey> extends AbstractNodeTupleNodeGroupMapper<TKey, Triple, TripleWritable> {

    @Override
    protected NodeWritable[] getNodes(TripleWritable tuple) {
        Triple t = tuple.get();
        return new NodeWritable[] { new NodeWritable(t.getSubject()), new NodeWritable(t.getPredicate()), new NodeWritable(t.getObject()) };
    }
    
    @Override
    protected void map(TKey key, TripleWritable value, Context context) throws IOException, InterruptedException { 
    	NodeWritable[] ns = this.getNodes(value);
    	context.write(ns[0], ns[2]);
    }
}