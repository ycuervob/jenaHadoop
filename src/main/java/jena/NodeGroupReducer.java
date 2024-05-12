package jena;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.mapreduce.ReduceContext;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.jena.graph.Node;
import org.apache.jena.hadoop.rdf.types.NodeWritable;


public class NodeGroupReducer extends Reducer<NodeWritable, NodeWritable, NodeWritable, Set<Node>> {

    @Override
    protected void reduce(NodeWritable key, Iterable<NodeWritable> values, Context context) throws IOException, InterruptedException {   
    	Set<Node> valuesArray = new HashSet<Node>();
        Iterator<NodeWritable> iter = values.iterator();
        
        
        //System.out.print(key+ "[");

        while (iter.hasNext()) {
        	NodeWritable invividualvalue = iter.next();
        	valuesArray.add(invividualvalue.get());
        	//System.out.print(""+invividualvalue+ ", " );
        }
        
        
       //System.out.print("]\n");
       context.write(key, valuesArray);
       System.out.println(key + " - " + valuesArray);
        
       
        
    }
    
	@Override
	public void run(Context context) throws IOException, InterruptedException {
		setup(context);
		try {
			while (context.nextKey()) {
				reduce(context.getCurrentKey(), context.getValues(), context);
				// If a back up store is used, reset it
				Iterator<NodeWritable> iter = context.getValues().iterator();
				if (iter instanceof ReduceContext.ValueIterator) {
					((ReduceContext.ValueIterator<NodeWritable>) iter).resetBackupStore();
				}
			}
		} finally {
			cleanup(context);
		}
	}
}