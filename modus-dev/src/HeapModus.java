import java.awt.event.ActionEvent;
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
		
		public Heap() {
			
		}

		@Override
		public boolean hasNext() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Node next() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void remove() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Iterator<Node> iterator() {
			return this;
		}
		
		public void add(SylladexCard card) {
			
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
