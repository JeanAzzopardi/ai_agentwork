package search;

import agent.Action;
import agent.Actions;

public class BreadthFirstSearch extends GeneralSearch {

	public BreadthFirstSearch(State startState, NodeInfo nodeInfo) {
		super(startState, nodeInfo);
	}

	private boolean isVisited(Node cur) {
		boolean isinvisited = false;
		
		// Check the visited list.
		if(!this.visited.isEmpty() && cur != null) {
			for(int x = 0; x < this.visited.size(); x++) {
				if(this.visited.get(x).getState().toString().compareTo(cur.getState().toString()) == 0) {
					isinvisited = true;
				}
			}
		}
		
		return isinvisited;
	}
	
	@Override
	public void insert(Node node) {
				
		Actions children = node.getState().getActions();
		
		String result = "";

		if(!children.isEmpty()) {
			
			for(int z = 0; z < children.size(); z++) {
		
				Node clone = ((Node)node.clone());
				clone.update((Action)children.get(z));
				if(!isVisited(clone)) {
					this.unvisited.add(clone);
					
					// Debug.
					result += " : " + clone.getState().toString();
				}		
			}
			System.out.println("Adding" + result);
		}
	}

	@Override
	public Node select() {
		
		if(!this.unvisited.isEmpty()) {
			Node temp = this.unvisited.get(0);
			this.visited.add(this.unvisited.remove(0));
			return temp;
		}
		return null;
	}
	
	public String toString() {
		return "BreadthFirstSearch";
	}

}
