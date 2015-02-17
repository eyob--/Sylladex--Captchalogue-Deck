import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;

import sylladex.FetchModus;
import sylladex.Main;
import sylladex.SylladexCard;
import sylladex.SylladexItem;


public class HeapModus extends FetchModus {

	Heap heap = new Heap();
	
	public HeapModus(Main m) {
		super(m);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addCard() {
		deck.addCard();
		arrangeCards(false);
	}

	@Override
	public boolean captchalogue(SylladexItem item) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object[] getCardOrder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initialSettings() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void open(SylladexCard arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void prepare() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ready() {
		// TODO Auto-generated method stub
		
	}
	
	private void arrangeCards(boolean animate) {
		// TODO write this method
	}
	
	public class Heap implements Iterable<Heap.Node>, Iterator<Heap.Node> {
		
		private Node root;
		private ArrayList<Node> visited = new ArrayList<Node>();
		
		public Heap() {
			root = null;
		}

		@Override
		public boolean hasNext() {
			for (SylladexCard card : deck.getCards()) {
				Node node = getNodeWithCard(card);
				// if there's no root, why are you iterating???
				if (node == null) break;
				else if (!visited.contains(node))
					return true;
			}
			visited.clear();
			return false;
		}

		@Override
		public Node next() {
			if (visited.size() == 0) {
				visited.add(root);
				return root;
			}
			while (true) {	// The danger is real
				Node lastVisited = visited.get(visited.size()-1);
				// if we haven't been to the left of the last node and there's a node there
				if (visited.contains(lastVisited.left) && lastVisited.left != null) {
					visited.add(lastVisited.left);
					return lastVisited.left;
				}
				// ditto for the right
				else if(visited.contains(lastVisited.right) && lastVisited.right != null) {
					visited.add(lastVisited.right);
					return lastVisited.right;
				}
				// if we've visited both children, go back to the parent of this node
				else {
					visited.add(lastVisited.parent);
				}
			}
		}

		@Override
		public void remove() {
		}

		@Override
		public Iterator<Node> iterator() {
			return this;
		}
		
		public Node getRoot() {
			return root;
		}
		
		public void clear() {
			root = null;
		}
		
		public void add(SylladexCard card) {
			Node node = new Node(card);
			if (root == null) {
				root = node;
				return;
			}
			
			Node insertOn = nodeToInsertOn();	// "but doesn't that make it an O(n) insertion time?" yes. it does. got a problem?
			if (insertOn.left == null) insertOn.left = node;
			else insertOn.right = node;
			
			while (node.card.getItem().getName().toLowerCase().compareTo(node.parent.card.getItem().getName().toLowerCase()) > 0) {
				node.card = node.parent.card;
				node.parent.card = card;
				node = node.parent;
				if (node == root) return;
			}
		}
		
		/**
		 * Returns the node which the next inserted node should be a child of. IMPORTANT: MAKE SURE ROOT IS NOT NULL WHEN YOU USE THIS!
		 * @return parent of the next inserted node
		 */
		@SuppressWarnings("unchecked")
		public Node nodeToInsertOn() {
			ArrayList<Node> level = new ArrayList<Node>();
			level.add(root);
			if(root.left != null && root.right != null) return root;
			while (true) {		// Oh god this is so dangerous...
				ArrayList<Node> oldLevel = (ArrayList<Node>) level.clone();
				level = new ArrayList<Node>();
				for (Node n : oldLevel) {
					level.add(n.left);
					level.add(n.right);
				}
				for (Node n : level)
					if (n.left == null || n.right == null) return n;
			}
		}
		
		public void remove(SylladexCard card) {
			Node node = getNodeWithCard(card);
			
			// apparently we can only remove leaves (laaaaaaaame)
			if (!node.isLeaf()) return;
			
			if (node.parent != null) {
				if (node == node.parent.left) node.parent.left = null;
				else node.parent.right = null;
			}
		}
		
		public Node getNodeWithCard(SylladexCard card) {
			if (root != null) return root.getNodeWithCard(card);
			return null;
		}
		
		public void buildAnimation(SylladexCard card) {
			card.setLocation(new Point(0, 0));
			root.buildAnimation(card);
		}
		
		public class Node {
			
			private SylladexCard card;
			private Node left = null;
			private Node right = null;
			private Node parent = null;
			
			private int card_height = settings.get_card_height();
			
			public Node(SylladexCard card) {
				this.card = card;
			}
			
			public int getX() {
				if (parent == null) return 0;
				if (this == parent.left)
					return parent.getLeftChildX();
				return parent.getRightChildX();
			}
			
			public int getY() {
				if (parent == null) return 0;
				return parent.getChildY();
			}
			
			public int getChildY() {
				return getY() + 3*card_height/4;
			}
			
			public int getLeftChildX() {
				return getX() - 75/(2*getY()/card_height + 1);
			}
			
			public int getRightChildX() {
				return getX() + 75/(2*getY()/card_height + 1);
			}
			
			public boolean isRoot() {
				return this == root;
			}
			
			public boolean isLeaf() {
				return left == null && right == null;
			}
			
			public Node getNodeWithCard(SylladexCard card) {
				if (this.card == card) return this;
				if (left != null && left.getNodeWithCard(card) != null)
					return left.getNodeWithCard(card);
				if (right != null && right.getNodeWithCard(card) != null)
					return right.getNodeWithCard(card);
				return null;
			}
			
			public void buildAnimation(SylladexCard card) {
				// TODO write this
			}
		}
	}
}
