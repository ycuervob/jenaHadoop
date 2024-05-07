package jena;

import org.apache.jena.hadoop.rdf.mapreduce.count.AbstractNodeTupleNodeCountMapper;
import org.apache.jena.hadoop.rdf.types.NodeWritable;
import org.apache.jena.hadoop.rdf.types.TripleWritable;
import org.apache.jena.graph.Triple;

public class TripleNodeCountMapper<TKey> extends AbstractNodeTupleNodeCountMapper<TKey, Triple, TripleWritable> {

    @Override
    protected NodeWritable[] getNodes(TripleWritable tuple) {
        Triple t = tuple.get();
        return new NodeWritable[] { new NodeWritable(t.getSubject()), new NodeWritable(t.getPredicate()), new NodeWritable(t.getObject()) };
    }
}