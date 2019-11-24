package cz.uhk.fim.citeviz.graph.builder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.uhk.fim.citeviz.async.AsyncTask;
import cz.uhk.fim.citeviz.async.TaskManager;
import cz.uhk.fim.citeviz.graph.primitives.Edge;
import cz.uhk.fim.citeviz.graph.primitives.Graph;
import cz.uhk.fim.citeviz.graph.primitives.Node;
import cz.uhk.fim.citeviz.model.IdRecord;
import cz.uhk.fim.citeviz.ws.connector.DataInterfaceErrorHandler;

public abstract class RecurentGraphLoader<D extends IdRecord>{
	private int depth;
	
	private int maxIdsPerTransaction;
	
	private Graph graph;
	
	public RecurentGraphLoader(int depth, int maxIdsPerTransaction, Graph graph) {
		this.depth = depth;
		this.graph = graph;
		this.maxIdsPerTransaction = maxIdsPerTransaction;
	}
	
	protected abstract List<D> readFromInterface(Set<IdRecord> ids) throws Throwable;
	
	protected abstract Set<IdRecord> getRefs(D item);
	
	protected abstract Node<D> getNewNode(int currentDepth);
	
	protected abstract Edge<D, D> getNewEdge(Node<D> from, Node<D> to);
	
	public void load(IdRecord id){
		Set<IdRecord> ids = new HashSet<IdRecord>(1);
		ids.add(id);
		loadInner(0, ids);
	}
	
	public void load(Set<IdRecord> ids){
		loadInner(0, ids);
	}
	
	private void loadInner(int currentDepth, Set<IdRecord> ids){
		TaskManager.getInstance().stopTask(ViewBuilder.DATA_LOADING_THREAD);
		TaskManager.getInstance().startTask(new AsyncTask<Set<IdRecord>>() {
			
			@Override
			protected Set<IdRecord> runTask() throws Throwable{
				if (ids.size() <= maxIdsPerTransaction || maxIdsPerTransaction == 0){
					return processGroupOfItems(ids);
				} else {
					Set<IdRecord> refs = new HashSet<IdRecord>();
					Set<IdRecord> currentLoadSet = new HashSet<IdRecord>();
					
					for (IdRecord idRecord : ids) {
						currentLoadSet.add(idRecord);
						if (currentLoadSet.size() > maxIdsPerTransaction){
							refs.addAll(processGroupOfItems(currentLoadSet));
							currentLoadSet.clear();
						}
					}
					
					refs.addAll(processGroupOfItems(currentLoadSet));
					return refs;
				}
			}
			
			private Set<IdRecord> processGroupOfItems(Set<IdRecord> ids) throws Throwable{
				Set<IdRecord> refs = new HashSet<IdRecord>();
				
				if (!isRunning()){
					return refs;
				}
				
				List<D> data = readFromInterface(ids);
				synchronized (graph){
					for (D item : new ArrayList<>(data)) {
						refs.addAll(getRefs(item));
						Node<D> node = getNewNode(currentDepth);
						node.setData(item);
						
						if (!graph.getNodes().contains(node)){
							createEdges(node);
							graph.addNode(node);
						} else if (currentDepth == 0){
							//refresh color and size of exist root node
							Node<?> existNode = graph.getNodes().get(graph.getNodes().indexOf(node));
							existNode.setColor(node.getColor());
							existNode.setSize(node.getSize());
						}
						
					}
				}
				return refs;
			}

			@Override
			protected void onFinish(Set<IdRecord> refs) {
				if (refs == null){
					return;
				}
				
				if (currentDepth < depth){
					loadInner(currentDepth + 1, refs);
				}
			}
			
			@Override
			protected void onError(Throwable e) {
				DataInterfaceErrorHandler.handleError(e);
			}
			
			@Override
			protected Object getLockObject() {
				return graph;
			}
			
			@Override
			public String getTaskId() {
				return ViewBuilder.DATA_LOADING_THREAD;
			}
			
		}, ViewBuilder.DATA_LOADING_THREAD);
	}
	
	@SuppressWarnings("unchecked")
	private void createEdges(Node<D> node){
		if (graph.getNodes() == null){
			return;
		}
		
		IdRecord currentNodeId = new IdRecord(node.getData().getId());
		
		for (Node<?> item : graph.getNodes()) {	
			Set<IdRecord> refs = getRefs((D)item.getData());
			if (refs.contains(currentNodeId)){
				Edge<D, D> edge = getNewEdge(node, (Node<D>)item);
				graph.addEdge(edge);
			}
		}
	}	
}