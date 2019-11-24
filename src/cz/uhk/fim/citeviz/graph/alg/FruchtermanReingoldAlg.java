package cz.uhk.fim.citeviz.graph.alg;

import java.util.ArrayList;
import java.util.List;

import cz.uhk.fim.citeviz.graph.primitives.Node;

/**
 * T¯Ìda obsahujÌcÌ metody pro rozmÌsùov·nÌ uzl˘ grafu podle Fruchterman-Reingoldova algoritmu
 * @author Ond¯ej Klapka
 *
 */
public class FruchtermanReingoldAlg {
	private int canvas = 1000;
    
	private float gravity = 0.2f;
    
	private float speed = 0.35f;
    
	private float minEnergy = -1;
    
	private int iteration = 0;
    
    private AttractNodesEvaluatorAlg attractNodesEvaluatorAlg = new AttractNodesEvaluatorAlg();
    
	public boolean doIteration(List<Node<?>> nodes){
		nodes = new ArrayList<>(nodes);
		
		float energy = 0;
		float k = (float) Math.sqrt((canvas) / (1f + nodes.size()));
		float maxMovement = (float) (Math.sqrt(canvas) / 10f);
		
		
		for (Node<?> n1 : nodes) {
			for (Node<?> n2 : nodes) {
				
				//same node, skip it
				if (n1 == n2){
					continue;
				}
				
				//nodes distance
				float xD = n1.getTmpX() - n2.getTmpX();  
                float yD = n1.getTmpY() - n2.getTmpY();
                float d = (float) Math.sqrt(xD * xD + yD * yD);  

                if (d > 0) {  
                	
                	//repulsive force
                    n1.setMoveX(n1.getMoveX() + (k * k * xD / (d * d)));    
                    n1.setMoveY(n1.getMoveY() + (k * k * yD / (d * d)));
                    
                    
                    float evaluatorResult = attractNodesEvaluatorAlg.evaluate(n1, n2);
                   
                    //attractive force
                    if (evaluatorResult != 0){
                    	float attractX = xD * d / k * evaluatorResult;
        				float attractY = yD * d / k * evaluatorResult;
        				
        				  
        		        n1.setMoveX(n1.getMoveX() - attractX);
        		        n1.setMoveY(n1.getMoveY() - attractY);
        		        n2.setMoveX(n2.getMoveX() + attractX);
        		        n2.setMoveY(n2.getMoveY() + attractY);
                    }
                }  
			}	
			
		}
		
		//movement speed
		for (Node<?> node : nodes) {
			//fixed node, the position is not changed, skip it
			if (node.isFixed()) {
				continue;
			}
	
            node.setMoveX(node.getMoveX() * speed);
            node.setMoveY(node.getMoveY() * speed);
		
            //final node placement
            float d = (float) Math.sqrt(node.getMoveX() * node.getMoveX() + node.getMoveY() * node.getMoveY());
            if (d > 0) {
                float restrictD = Math.min(maxMovement * speed, d);
                float moveX = node.getMoveX() / d * restrictD;
                float moveY = node.getMoveY() / d * restrictD;
                node.setTmpX(node.getTmpX() + moveX);
                node.setTmpY(node.getTmpY() + moveY);
                
                energy += Math.sqrt(moveX * moveX + moveY * moveY); 
            } 
            
            //gravity
            float gravityF = k * gravity;
            node.setMoveX(node.getMoveX() - gravityF * node.getTmpX());
            node.setMoveY(node.getMoveY() - gravityF * node.getTmpY());
		}
		
		/* ¯ÌzenÌ bÏhu simulace
		 * Simulace se zastavÌ, pokud celkov· suma vzd·lenostÌ o kterou jsou uzly
		 * p¯emÌstÏny v˝znamnÏ nekles·. AlespoÚ 50 iteracÌ musÌ probÏhnout, aby 
		 * se graf trochu rozkmital a zÌskal nÏjakou energii.
		 */
		
		//pokud je minEnerige rovno -1, tak cyklus probÏhnul poprvÈ, dosadÌme aktu·lnÌ energii
		if (minEnergy == -1){
			minEnergy = energy;
		}
		
		iteration++;
		
		if ((Math.abs(energy - minEnergy) < 0.001) && (iteration > 50)){
			return true;
		}
			
		minEnergy = energy;
		return false;
	}
	
	public void synchronizePositions(List<Node<?>> nodes) {
		for (Node<?> node : nodes) {
			node.synchronizePosition();
		}
	}
	
	public void reset(){
		iteration = 0;
		minEnergy = -1;
	}
	
	public int getGravity() {
		return (int)gravity * 10;
	}
	
	public void setGravity(int gravity) {
		this.gravity = gravity / 10f;
		reset();
	}
	
	public void setCanvas(int canvas) {
		this.canvas = canvas;
		reset();
	}
	
	public int getCanvas() {
		return canvas;
	}
	
	public void setAttractNodesEvaluatorAlg(AttractNodesEvaluatorAlg attractNodesEvaluatorAlg) {
		this.attractNodesEvaluatorAlg = attractNodesEvaluatorAlg;
	}
	
	public AttractNodesEvaluatorAlg getAttractNodesEvaluatorAlg() {
		return attractNodesEvaluatorAlg;
	}
}