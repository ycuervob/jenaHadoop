package jena;

import java.io.IOException;
import java.util.Iterator;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.jena.hadoop.rdf.types.NodeWritable;

/**
 * A reducer which takes node keys with a sequence of longs representing counts
 * as the values and sums the counts together into pairs consisting of a node
 * key and a count value.
 */
public class NodeCountReducer extends Reducer<NodeWritable, LongWritable, NodeWritable, LongWritable> {

    @Override
    protected void reduce(NodeWritable key, Iterable<LongWritable> values, Context context) throws IOException,
            InterruptedException {
        long count = 0;
        Iterator<LongWritable> iter = values.iterator();
        
        while (iter.hasNext()) {
            count += iter.next().get();
        }
        
        context.write(key, new LongWritable(count));
    }
}