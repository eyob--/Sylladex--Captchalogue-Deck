import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;

import sylladex.CaptchalogueCard;
import sylladex.FetchModus;
import sylladex.Main;
import sylladex.SylladexItem;
import util.Animation;
import util.Animation.AnimationType;
import util.Util.OpenReason;


public class HeapModus extends FetchModus {

	Heap heap = new Heap();
	
	private static final int PREF_ROOT_ACCESS = 0;
	
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
		if (deck.isFull()) return false;
		
		CaptchalogueCard card = deck.captchalogueItemAndReturnCard(item);
		heap.add(card);
		
		heap.buildAnimation(card);
		Animation a = new Animation(AnimationType.WAIT, 10, this, "heap_animation_complete");
		a.setAnimationTarget(card);
		deck.addAnimation(a);
		return true;
	}
	
	@Override
	public Object[] getCardOrder() {
		ArrayList<CaptchalogueCard> cards = new ArrayList<CaptchalogueCard>();
		if (heap != null && heap.root != null) {
			for (Heap.Node n : heap)
				cards.add(n.card);
		}
		return cards.toArray();
	}

	@Override
	public void initialSettings() {
		settings.set_dock_text_image("modi/canon/heap/text.png");
		settings.set_dock_card_image("modi/canon/heap/dockcard.png");
		settings.set_card_image("modi/canon/heap/card.png");
		settings.set_card_back_image("modi/canon/heap/back.png");
		
		settings.set_modus_image("modi/canon/heap/modus.png");
		settings.set_name("Heap");
		settings.set_author("eyob--");
		
		settings.set_preferences_file("modi/prefs/heapprefs.txt");
		
		settings.set_background_color(15, 80, 219);
		settings.set_secondary_color(33, 78, 137);
		settings.set_text_color(255, 242, 0);
		
		settings.set_initial_card_number(8);
		settings.set_origin(20, 120);
		
		settings.set_card_size(94, 119);
		
		settings.set_shade_inaccessible_cards(false);
		
	}

	@Override
	public void open(CaptchalogueCard card, OpenReason reason) {
		if (heap.getNodeWithCard(card).isRoot()) {
			eject(OpenReason.MODUS_DEFAULT);
			arrangeCards(false);
		}
		else {
			deck.open(card, reason);
			heap.remove(card);
			arrangeCards(false);
		}
	}

	@Override
	public void prepare() {
		if (preferences.size() == 0) {
			preferences.add("true");
		}
		populatePreferencesPanel();
		deck.getCardDisplayManager().setMargin(settings.get_card_height());
		heap = new Heap();
	}

	@Override
	public void ready() {
		arrangeCards(false);
	}
	
	private void arrangeCards(boolean animate) {
		// TODO write this method
	}
	
	private void eject(OpenReason reason) {
		// open all sub nodes
		for (Heap.Node node : heap)
			if (!node.isRoot()) deck.open(node.card, reason);
		
		// open root node
		deck.open(heap.getRoot().card, reason);
		
		// and clear the heap
		heap.clear();
		
		arrangeCards(false);
	}
	
	private int getLayer(int x, int y) {
		if (preferences.get(PREF_ROOT_ACCESS).equals("true"))
		{
			return deck.getCardHolder().getHeight() - y - x;
		}
		return y - x;
	}
	
	private void populatePreferencesPanel() {
		// TODO actually populate the prefs panel
	}
	
	public class Heap implements Iterable<Heap.Node>, Iterator<Heap.Node> {
		
		private Node root;
		private ArrayList<Node> visited = new ArrayList<Node>();
		
		public Heap() {
			root = null;
		}

		@Override
		public boolean hasNext() {
			for (CaptchalogueCard card : deck.getCards()) {
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
		
		public void add(CaptchalogueCard card) {
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
		
		public void remove(CaptchalogueCard card) {
			Node node = getNodeWithCard(card);
			
			// apparently we can only remove leaves (laaaaaaaame)
			if (!node.isLeaf()) return;
			
			if (node.parent != null) {
				if (node == node.parent.left) node.parent.left = null;
				else node.parent.right = null;
			}
		}
		
		public Node getNodeWithCard(CaptchalogueCard card) {
			if (root != null) return root.getNodeWithCard(card);
			return null;
		}
		
		public void buildAnimation(CaptchalogueCard card) {
			card.setLocation(new Point(0, 0));
			root.buildAnimation(card);
		}
		
		public class Node {
			
			private CaptchalogueCard card;
			private Node left = null;
			private Node right = null;
			private Node parent = null;
			
			private int card_height = settings.get_card_height();
			
			public Node(CaptchalogueCard card) {
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
			
			public Node getNodeWithCard(CaptchalogueCard card) {
				if (this.card == card) return this;
				if (left != null && left.getNodeWithCard(card) != null)
					return left.getNodeWithCard(card);
				if (right != null && right.getNodeWithCard(card) != null)
					return right.getNodeWithCard(card);
				return null;
			}
			
			public void buildAnimation(CaptchalogueCard card) {
				// TODO write this
			}
		}
	}
}
