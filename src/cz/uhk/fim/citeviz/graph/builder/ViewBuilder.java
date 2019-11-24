package cz.uhk.fim.citeviz.graph.builder;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.uhk.fim.citeviz.graph.primitives.Colors;
import cz.uhk.fim.citeviz.graph.primitives.Edge;
import cz.uhk.fim.citeviz.graph.primitives.Graph;
import cz.uhk.fim.citeviz.graph.primitives.Node;
import cz.uhk.fim.citeviz.graph.primitives.NodeShape;
import cz.uhk.fim.citeviz.graph.views.AuthorCitationNetworkView;
import cz.uhk.fim.citeviz.graph.views.CitationNetworkView;
import cz.uhk.fim.citeviz.graph.views.ColaboratorsView;
import cz.uhk.fim.citeviz.graph.views.GraphBasedView;
import cz.uhk.fim.citeviz.graph.views.RefDirectionView;
import cz.uhk.fim.citeviz.graph.views.TreeView;
import cz.uhk.fim.citeviz.graph.views.View;
import cz.uhk.fim.citeviz.gui.AuthorDetailView;
import cz.uhk.fim.citeviz.gui.PaperDetailView;
import cz.uhk.fim.citeviz.model.Author;
import cz.uhk.fim.citeviz.model.IdRecord;
import cz.uhk.fim.citeviz.model.Paper;
import cz.uhk.fim.citeviz.model.RefDirection;
import cz.uhk.fim.citeviz.model.ViewType;
import cz.uhk.fim.citeviz.util.CiteVizUtils;
import cz.uhk.fim.citeviz.ws.connector.DataInterface;

public class ViewBuilder {
	
	public static final String DATA_LOADING_THREAD = "dataLoading";
	
	public static View createView(ViewType viewType, DataInterface dataInterface, Set<IdRecord> rootIds){
		switch (viewType){
			case AUTHOR_CITATION_NETWORK: 
				return new AuthorCitationNetworkView(dataInterface, 
					authorCitationNetworkGraph(dataInterface, RefDirection.CITATION, rootIds, GraphBasedView.DEFAULT_DEPTH, null), 
					rootIds);
			case AUTHOR_COLABORATORS: 
				return new ColaboratorsView(dataInterface,
					colaboratorsGraph(dataInterface, rootIds, GraphBasedView.DEFAULT_DEPTH, null), 
					rootIds);
			case AUTHOR_DETAIL:
				return new AuthorDetailView(dataInterface, CiteVizUtils.asItem(rootIds));
				
			case PAPER_TREE_VIEW: 
				return new TreeView(dataInterface, 
					treeView(dataInterface, RefDirection.CITATION, CiteVizUtils.asItem(rootIds), 1, null), 
					CiteVizUtils.asItem(rootIds));
			case PAPER_CITATION_NETWORK: 
				return new CitationNetworkView(dataInterface, 
					citationNetworkGraph(dataInterface, RefDirection.CITATION, rootIds, GraphBasedView.DEFAULT_DEPTH, null),
					rootIds);
			case PAPER_DETAIL:
				return new PaperDetailView(dataInterface, CiteVizUtils.asItem(rootIds));
			
			default: return null;
		}
	}
	
	public static void updateView(View view, DataInterface dataInterface, Set<IdRecord> rootIds) {
		
		if (view instanceof GraphBasedView){
			GraphBasedView graphView = ((GraphBasedView) view);
			graphView.addRoot(rootIds);
			
			RefDirection selectedDirection = RefDirection.CITATION;
			
			if (view instanceof RefDirectionView){
				selectedDirection = ((RefDirectionView) view).getRefDirection();
			}
			
			switch (view.getViewType()){
				case AUTHOR_CITATION_NETWORK: authorCitationNetworkGraph(dataInterface, selectedDirection, graphView.getRootRecords(), graphView.getSelectedDepth(), graphView.getGraph()); break;
				case AUTHOR_COLABORATORS: colaboratorsGraph(dataInterface, graphView.getRootRecords(), graphView.getSelectedDepth(), graphView.getGraph()); break;
				case PAPER_CITATION_NETWORK: citationNetworkGraph(dataInterface, selectedDirection, graphView.getRootRecords(), graphView.getSelectedDepth(), graphView.getGraph()); break;
				default: throw new IllegalArgumentException("Unsupported view type for update");
			}
		} else {
			throw new IllegalArgumentException("Unsupported view type for update");
		}
	}
	
	public static Set<Paper> treeView(DataInterface dataInterface, RefDirection direction, IdRecord rootId, int depth, Set<Paper> papers) {
		if (papers == null){
			papers = new HashSet<Paper>();
		}
		
		List<Paper> currentLevelPapers = new ArrayList<Paper>();
		currentLevelPapers.add(dataInterface.getPaperDetails(rootId));
		papers.addAll(currentLevelPapers);
		if (depth > 0){
			return treeView(dataInterface, direction, currentLevelPapers, depth - 1, papers);
		} else {
			return papers;
		}
	}
	
	private static Set<Paper> treeView(DataInterface dataInterface, RefDirection direction, List<Paper> currentLevelPapers, int depth, Set<Paper> papers) {
		if (papers == null){
			papers = new HashSet<Paper>();
		}
		
		List<Paper> nextLevelPapers = new ArrayList<Paper>();
		
		if (RefDirection.CITATION.equals(direction)){
			for (Paper paper : currentLevelPapers) {
				nextLevelPapers.addAll(dataInterface.getPaperDetails(paper.getChilds()));
			}
		} else {
			for (Paper paper : currentLevelPapers) {
				nextLevelPapers.addAll(dataInterface.getPaperDetails(paper.getParents()));
			}
		}
		
		papers.addAll(nextLevelPapers);
		
		if (depth > 0){
			treeView(dataInterface, direction, nextLevelPapers, depth - 1, papers);
		}
		
		return papers;
	}

	public static Graph citationNetworkGraph(DataInterface dataInterface, RefDirection direction, Set<IdRecord> rootIds, int depth, Graph g){
		if (g == null){
			g = new Graph();
		}
		
		RecurentGraphLoader<Paper> loader = new RecurentGraphLoader<Paper>(depth, dataInterface.getMaxIdsPerTransaction(), g) {

			@Override
			protected List<Paper> readFromInterface(Set<IdRecord> ids) throws Throwable{
				return dataInterface.getPaperDetails(ids);
			}

			@Override
			protected Set<IdRecord> getRefs(Paper item) {
				if (RefDirection.CITATION.equals(direction)){
					return item.getChilds();
				} else {
					return item.getParents();
				}
			}

			@Override
			protected Node<Paper> getNewNode(int currentDepth) {
				Color color; 
				int size;
				if (currentDepth == 0){
					color = RefDirection.REFERENCE.equals(direction) ? Colors.getCitationColor() : Colors.getReferenceColor();
					size = 5;
				} else {
					color = RefDirection.REFERENCE.equals(direction) ? Colors.getReferenceColor() : Colors.getCitationColor();
					size = 3;
				}
				
				return new Node<Paper>(NodeShape.CIRCLE, color, size);
			}

			@Override
			protected Edge<Paper, Paper> getNewEdge(Node<Paper> from, Node<Paper> to) {
				if (RefDirection.REFERENCE.equals(direction)){
					return new Edge<Paper, Paper>(to, from, Colors.getEdgeColor());
				} else {
					return new Edge<Paper, Paper>(from, to, Colors.getEdgeColor());
				}
			}

			
		};
		loader.load(rootIds);
		
		return g;
	}
	
	public static Graph colaboratorsGraph(DataInterface dataInterface, Set<IdRecord> rootIds, int depth, Graph g){
		if (g == null){
			g = new Graph();
		}
		
		RecurentGraphLoader<Author> loader = new RecurentGraphLoader<Author>(depth, dataInterface.getMaxIdsPerTransaction(), g) {

			@Override
			protected List<Author> readFromInterface(Set<IdRecord> ids)  throws Throwable{
				return dataInterface.getAuthorDetails(ids);
			}

			@Override
			protected Set<IdRecord> getRefs(Author item) {
				return item.getCollaborators();
			}

			@Override
			protected Node<Author> getNewNode(int currentDepth) {
				int size;
				if (currentDepth == 0){
					size = 5;
				} else {
					size = 3;
				}
				return new Node<Author>(NodeShape.CIRCLE, Colors.getColaboratorColor(), size);
			}

			@Override
			protected Edge<Author, Author> getNewEdge(Node<Author> from, Node<Author> to) {
				return new Edge<Author, Author>(from, to, Colors.getEdgeColor(), true);
			}

			
		};
		loader.load(rootIds);
		
		return g;
	}
	
	public static Graph authorCitationNetworkGraph(DataInterface dataInterface, RefDirection direction, Set<IdRecord> rootIds, int depth, Graph g){
		if (g == null){
			g = new Graph();
		}
		
		RecurentGraphLoader<Author> loader = new RecurentGraphLoader<Author>(depth, dataInterface.getMaxIdsPerTransaction(), g) {

			@Override
			protected List<Author> readFromInterface(Set<IdRecord> ids)  throws Throwable{
				return dataInterface.getAuthorDetails(ids);
			}

			@Override
			protected Set<IdRecord> getRefs(Author item) {
				if (RefDirection.CITATION.equals(direction)){
					return item.getChilds();
				} else {
					return item.getParents();
				}
			}

			@Override
			protected Node<Author> getNewNode(int currentDepth) {
				Color color; 
				int size;
				if (currentDepth == 0){
					color = RefDirection.REFERENCE.equals(direction) ? Colors.getCitationColor() : Colors.getReferenceColor();
					size = 5;
				} else {
					color = RefDirection.REFERENCE.equals(direction) ? Colors.getReferenceColor() : Colors.getCitationColor();
					size = 3;
				}
				
				return new Node<Author>(NodeShape.CIRCLE, color, size);
			}

			@Override
			protected Edge<Author, Author> getNewEdge(Node<Author> from, Node<Author> to) {
				if (RefDirection.REFERENCE.equals(direction)){
					return new Edge<Author, Author>(to, from, Colors.getEdgeColor());
				} else {
					return new Edge<Author, Author>(from, to, Colors.getEdgeColor());
				}
			}

			
		};
		loader.load(rootIds);
		
		return g;
	}
	
	
	
	
	
	
	private static Graph graph;
	public static Graph testGraph(){
		 Graph g = new Graph();
			
		 Node<IdRecord> n1 = new Node<IdRecord>(2f, 2f, NodeShape.RECTANGLE, Color.red, 3);
		 n1.setData(new IdRecord("1 - cervena"));
		 Node<IdRecord> n2 = new Node<IdRecord>(2f, 20f, NodeShape.RECTANGLE, Color.green, 3);
		 n2.setData(new IdRecord("2 - zelena"));
		 Edge<IdRecord, IdRecord> e = new Edge<IdRecord, IdRecord>(n1, n2, Color.orange);

		 g.addNode(n1);
		 g.addNode(n2);
		 g.addEdge(e);
		 
		 Node<IdRecord> n3 = new Node<IdRecord>(8f, 20f, NodeShape.CIRCLE, Color.blue, 3);
		 n3.setData(new IdRecord("2 - modra"));
		 Edge<IdRecord, IdRecord> e2 = new Edge<IdRecord, IdRecord>(n1, n3, Color.DARK_GRAY);
		 g.addNode(n3);
		 g.addEdge(e2);
		 
		 
		 Node<IdRecord> n4 = new Node<IdRecord>(8f, 20f, NodeShape.TRIANGLE, Color.pink, 3);
		 n4.setData(new IdRecord("3 - ruzova"));
		 g.addNode(n4);
		 Edge<IdRecord, IdRecord> e3 = new Edge<IdRecord, IdRecord>(n3, n4, Color.DARK_GRAY);
		 g.addEdge(e3);
		 
		 Node<IdRecord> n5 = new Node<IdRecord>(10f, 20f, NodeShape.DIAMOND, Color.magenta, 8);
		 n5.setData(new IdRecord("4 - vinova"));
		 g.addNode(n5);
		 Edge<IdRecord, IdRecord> e4 = new Edge<IdRecord, IdRecord>(n3, n5, Color.DARK_GRAY);
		 g.addEdge(e4);
		
		 if (graph == null){
			 graph = g;
		 }
		
		 
		 return graph;
	}	
}