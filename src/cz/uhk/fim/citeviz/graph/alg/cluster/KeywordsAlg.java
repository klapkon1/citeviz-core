package cz.uhk.fim.citeviz.graph.alg.cluster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.linguistic.rake.KeywordsExtractor;

import cz.uhk.fim.citeviz.graph.primitives.Node;
import cz.uhk.fim.citeviz.model.PaperFullDetail;

public class KeywordsAlg extends ClusterAlg{

	private KeywordsExtractor extractor = new KeywordsExtractor();
	
	
//	public KeywordsAlg() {
//		super(null);
//		System.out.println(extractor.analyzeText("The Graham scan is a fundamental backtracking technique in computational geometry which was originally designed to compute the convex hull of a set of points in the plane and has since found application in several different contexts. In this note we show how to use the Graham scan to triangulate a simple polygon. The resulting algorithm triangulates an n vertex polygon P in O(kn) time where k-1 is the number of concave vertices in P. Although the worst case running time of the algorithm is O(n 2 ), it is easy to implement and is therefore of practical interest. 1. Introduction A polygon P is a closed path of straight line segments. A polygon is represented by a sequence of vertices P = (p 0 ,p 1 ,...,p n-1 ) where p i has real-valued x,y-coordinates. We assume that no three vertices of P are collinear. The line segments (p i ,p i+1 ), 0 i n-1, (subscript arithmetic taken modulo n) are the edges of P. A polygon is simple if no two nonconsecutive edges intersect. A simple polygon part..."));
//	}
	
	public KeywordsAlg(List<Node<?>> nodes) {
		super(nodes);
	}

	@Override
	public void computeGroups(int thresold) {
		Map<Node<?>, Map<String, Double>> computedKeywords = new HashMap<>();
		
		for (Node<?> node : getNodes()) {
			PaperFullDetail paper = (PaperFullDetail)node.getData();
			
			computedKeywords.put(node, extractor.analyzeText(paper.getAbstract()));
			
			
			System.out.println(computedKeywords.get(node));
		}
	}

	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return 0;
	}

}
