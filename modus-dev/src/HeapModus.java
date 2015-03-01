import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;

import sylladex.CaptchalogueCard;
import sylladex.FetchModus;
import sylladex.Main;
import sylladex.SylladexItem;
import util.Animation;
import util.Animation.AnimationType;
import util.Util;
import util.Util.OpenReason;


public class HeapModus extends FetchModus {

	Heap heap = new Heap();
	
	private JToggleButton rootButton;
	private JToggleButton leafButton;
	
	private static final int PREF_ROOT_ACCESS = 0;
	
	public HeapModus(Main m) {
		super(m);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("heap_animation_complete")) {
			arrangeCards();
		}
		else if (e.getActionCommand().equals("heap_animation_continue")) {
			Object target = ((Animation) e.getSource()).getAnimationTarget();
			if (target instanceof CaptchalogueCard) {
				CaptchalogueCard card = (CaptchalogueCard) target;
				card.setVisible(true);
				card.setLayer(getLayer(card.getX(), card.getY() + settings.get_card_height()));
			}
		}
		else if (e.getActionCommand().equals("heap_eject")) {
			int n = JOptionPane.showOptionDialog(preferences_panel, "EJECT ALL ITEMS FROM SYLLADEX?", "",
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, new Object[]{"Y","N"}, "N");
			if (n == 0) eject(OpenReason.USER_EJECT);
		}
		else if (e.getActionCommand().equals("heap_root")) {
			preferences.set(PREF_ROOT_ACCESS, "true");
			leafButton.setSelected(false);
			arrangeCards();
		}
		else if (e.getActionCommand().equals("heap_leaf")) {
			preferences.set(PREF_ROOT_ACCESS, "false");
			rootButton.setSelected(false);
			arrangeCards();
		}
		else if (e.getActionCommand().equals(Util.ACTION_CARD_MOUSE_ENTER)) {
			((CaptchalogueCard) e.getSource()).setLayer(deck.getCardHolder().getHeight());
		}
		else if (e.getActionCommand().equals(Util.ACTION_CARD_MOUSE_EXIT)) {
			CaptchalogueCard card = (CaptchalogueCard) e.getSource();
			card.setLayer(getLayer(card.getX(), card.getY()));
		}
	}

	@Override
	public void addCard() {
		deck.addCard();
		arrangeCards();
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
			arrangeCards();
		}
		else {
			deck.open(card, reason);
			heap.remove(card);
			arrangeCards();
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
		arrangeCards();
	}
	
	@Override
	public void refreshDock() {
		arrangeCards();
	}
	
	private void arrangeCards() {
		deck.setIcons(getCardOrder());
		foreground.removeAll();
		
		for (CaptchalogueCard card : deck.getCards()) {
			if (heap == null || heap.getNodeWithCard(card) == null)
				card.setVisible(false);
		}
		
		if (deck.isEmpty()) return;
		
		for (Heap.Node node : heap) {
			CaptchalogueCard card = node.card;
			
			card.setLocation(new Point(node.getX(), node.getY()));
			card.setVisible(true);
			card.setAccessible(Boolean.parseBoolean(preferences.get(PREF_ROOT_ACCESS)) && node.isRoot() ||
					!Boolean.parseBoolean(preferences.get(PREF_ROOT_ACCESS)) && node.isLeaf());
			
			card.setLayer(getLayer(node.getX(), node.getY()));
			
			if (node.left != null) {
				Brace b = new Brace();
				b.setAbove(true);
				int width = node.left.card.getDockIcon().getX() - card.getDockIcon().getX() - 3;
				b.setBounds(card.getDockIcon().getX()+16, deck.getDockIconYPosition()-2, width, 8);
				foreground.add(b);
			}
			if (node.right != null) {
				Brace b = new Brace();
				int width = node.right.card.getDockIcon().getX() - card.getDockIcon().getX() - 3;
				b.setBounds(card.getDockIcon().getX()+16, deck.getDockIconYPosition()+52, width, 8);
				foreground.add(b);
			}
			
		}
		
		deck.getCardDisplayManager().unfreeze();
		deck.getCardDisplayManager().freeze();
		
	}
	
	private void eject(OpenReason reason) {
		// open all sub nodes
		for (Heap.Node node : heap)
			if (!node.isRoot()) deck.open(node.card, reason);
		
		// open root node
		deck.open(heap.getRoot().card, reason);
		
		// and clear the heap
		heap.clear();
		
		arrangeCards();
	}
	
	private int getLayer(int x, int y) {
		if (preferences.get(PREF_ROOT_ACCESS).equals("true"))
		{
			return deck.getCardHolder().getHeight() - y - x;
		}
		return y - x;
	}
	
	private void populatePreferencesPanel() {
		preferences_panel.setLayout(null);
		preferences_panel.setPreferredSize(new Dimension(270, 300));
		
		JButton ejectButton = new JButton("EJECT");
			ejectButton.setActionCommand("heap_eject");
			ejectButton.addActionListener(this);
			ejectButton.setBounds(77, 7, 162, 68);
			preferences_panel.add(ejectButton);
			
		rootButton = new JToggleButton("ROOT");
			rootButton.setActionCommand("heap_root");
			rootButton.addActionListener(this);
			rootButton.setBounds(58,181,84,36);
			rootButton.setSelected(Boolean.parseBoolean(preferences.get(PREF_ROOT_ACCESS)));
			preferences_panel.add(rootButton);

		leafButton = new JToggleButton("LEAF");
			leafButton.setActionCommand("heap_leaf");
			leafButton.addActionListener(this);
			leafButton.setBounds(142,181,84,36);
			leafButton.setSelected(!Boolean.parseBoolean(preferences.get(PREF_ROOT_ACCESS)));
			preferences_panel.add(leafButton);
			
		preferences_panel.validate();
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
			
			// apparently we can only remove leafs (laaaaaaaame)
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
			Node insert = nodeToInsertOn();
			nodeToInsertOn().buildAnimation(card);
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
			
			public void buildAnimation(CaptchalogueCard c) {
				// TODO write this
			}
		}
	}
	
	@SuppressWarnings("serial")
	private class Brace extends JLabel {
		
		boolean above = false;
		
		public void setAbove(boolean above) {
			this.above = above;
		}
		
		public void paintComponent(Graphics g) {
			g.setColor(settings.get_secondary_color());
			if (above) {
				g.drawLine(0, getHeight(), 0, 0);
				g.drawLine(0, 0, getWidth(), 0);
				g.drawLine(getWidth()-1, 0, getWidth()-1, getHeight());
			}
			else {
				g.drawLine(0, 0, 0, getHeight()-1);
				g.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
				g.drawLine(getWidth()-1, getHeight()-1, getWidth()-1, 0);
			}
		}
		
	}
}
