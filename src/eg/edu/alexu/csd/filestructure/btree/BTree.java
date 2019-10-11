package eg.edu.alexu.csd.filestructure.btree;

import java.util.ArrayList;
import java.util.List;

import javax.management.RuntimeErrorException;

import javafx.util.Pair;

public class BTree<K extends Comparable<K>, V> implements IBTree<K, V> {

	private IBTreeNode<K, V> root = null;
	private int t;
	private List<K> tempk = new ArrayList<K>();
	private List<V> tempv = new ArrayList<V>();
	private List<IBTreeNode<K, V>> tempc = new ArrayList<IBTreeNode<K, V>>();

	public BTree(int t) {
		if (t < 2)
			throw new RuntimeErrorException(new Error());
		this.t = t;
	}

	@Override
	public int getMinimumDegree() {
		// TODO Auto-generated method stub
		return t;
	}

	@Override
	public IBTreeNode<K, V> getRoot() {
		// TODO Auto-generated method stub
		if (root == null) {
			return null;
			// throw new RuntimeErrorException(null);
		}
		return root;
	}

	@Override
	public void insert(K key, V value) {
		// TODO Auto-generated method stub
		if (key == null || value == null)
			throw new RuntimeErrorException(new Error());
		if (root == null)
			root = new Node<>();
		else if (root.getNumOfKeys() == 2 * t - 1) {
			IBTreeNode<K, V> r = root;
			IBTreeNode<K, V> s = new Node<>();
			root = s;
			s.setLeaf(false);
			s.setNumOfKeys(0);
			s.getChildren().add(r);
			split(s, 0);
			insertNonFull(s, key, value);
			return;
		}
		insertNonFull(root, key, value);
	}

	@Override
	public V search(K key) {
		// TODO Auto-generated method stub
		if (key == null)
			throw new RuntimeErrorException(new Error());
		if (root == null)
			return null;
		IBTreeNode<K, V> x = searchByNode(root, key);
		if (x == null) {
			return null;
		} else {
			int i = 0;
			while (i < x.getNumOfKeys() && key.compareTo(x.getKeys().get(i)) > 0) {
				i = i + 1;
			}
			return x.getValues().get(i);
		}
	}

	@Override
	public boolean delete(K key) {
		// TODO Auto-generated method stub
		if (key == null)
			throw new RuntimeErrorException(new Error());
		if (search(key) == null)
			return false;
		deleteByNode(root, key);
		return true;
	}

	private void deleteByNode(IBTreeNode<K, V> x, K key) {
		// case 1
		if (x.isLeaf()) {
			for (int i = 0; i < x.getNumOfKeys(); i++) {
				if (key.compareTo(x.getKeys().get(i)) == 0) {
					x.getKeys().remove(i);
					x.getValues().remove(i);
					x.setNumOfKeys(x.getNumOfKeys() - 1);
					return;
				}
			}
		}
		// case 2
		int i = 0;
		while (i < x.getNumOfKeys() && x.getKeys().get(i).compareTo(key) < 0)
			i++;
		if (i < x.getNumOfKeys() && x.getKeys().get(i).compareTo(key) == 0) {
			if (x.getChildren().get(i).getNumOfKeys() > t - 1) {
				Pair<K, V> pred = predecessor(x, key, i);
				delete(pred.getKey());
				x.getKeys().set(i, pred.getKey());
				x.getValues().set(i, pred.getValue());
				return;
			}
			if (x.getChildren().get(i + 1).getNumOfKeys() > t - 1) {
				Pair<K, V> succ = successor(x, key, i);
				delete(succ.getKey());
				x.getKeys().set(i, succ.getKey());
				x.getValues().set(i, succ.getValue());
				return;
			}
			IBTreeNode<K, V> y = x.getChildren().get(i);
			IBTreeNode<K, V> z = x.getChildren().get(i + 1);
			y.getKeys().add(key);
			y.getValues().add(x.getValues().get(i));
			for (int j = 0; j < z.getNumOfKeys(); j++) {
				y.getKeys().add(z.getKeys().get(j));
				y.getValues().add(z.getValues().get(j));
			}
			y.setNumOfKeys(y.getNumOfKeys() + z.getNumOfKeys() + 1);
			if (!y.isLeaf()) {
				for (int j = 0; j < z.getChildren().size(); j++) {
					y.getChildren().add(z.getChildren().get(j));
				}
			}
			for (int j = i; j < x.getNumOfKeys(); j++) {
				if (j > i)
					x.getChildren().set(j, x.getChildren().get(j + 1));
				if (j < x.getNumOfKeys() - 1) {
					x.getKeys().set(j, x.getKeys().get(j + 1));
					x.getValues().set(j, x.getValues().get(j + 1));
				}
			}
			x.getKeys().remove(x.getNumOfKeys()-1);
			x.getValues().remove(x.getNumOfKeys()-1);
			x.getChildren().remove(x.getNumOfKeys());
			x.setNumOfKeys(x.getNumOfKeys() - 1);
			deleteByNode(y, key);
			return;
		}
		// case 3
		if (x.getChildren().get(i).getNumOfKeys() >= t) {
			deleteByNode(x.getChildren().get(i), key);
			return;
		}
		if (x.getNumOfKeys() >= i+1) {
			if (x.getChildren().get(i+1).getNumOfKeys() >= t) {
				K pKey = x.getKeys().get(i);
				V pValue = x.getValues().get(i);
				IBTreeNode<K, V> rightSibling = x.getChildren().get(i + 1);
				K rightKey = rightSibling.getKeys().get(0);
				V rightValue = rightSibling.getValues().get(0);
				if (!x.getChildren().get(i).isLeaf()) {
					IBTreeNode<K, V> child = rightSibling.getChildren().get(0);
					x.getChildren().get(i).getChildren().add(child);
					rightSibling.getChildren().remove(0);
				}
				x.getKeys().set(i, rightKey);
				x.getValues().set(i, rightValue);
				x.getChildren().get(i).getKeys().add(pKey);
				x.getChildren().get(i).getValues().add(pValue);
				x.getChildren().get(i).setNumOfKeys(x.getChildren().get(i).getNumOfKeys() + 1);
				rightSibling.getKeys().remove(0);
				rightSibling.getValues().remove(0);
				rightSibling.setNumOfKeys(rightSibling.getNumOfKeys() - 1);
				deleteByNode(x.getChildren().get(i), key);
				return;
			}
		} else if (i > 0) {
			if (x.getChildren().get(i-1).getNumOfKeys() >= t) {
				K pKey = x.getKeys().get(i - 1);
				V pValue = x.getValues().get(i - 1);
				IBTreeNode<K, V> leftSibling = x.getChildren().get(i - 1);
				K leftKey = leftSibling.getKeys().get(leftSibling.getNumOfKeys() - 1);
				V leftValue = leftSibling.getValues().get(leftSibling.getNumOfKeys() - 1);
				if (!x.getChildren().get(i).isLeaf()) {
					IBTreeNode<K, V> child = leftSibling.getChildren().get(leftSibling.getNumOfKeys());
					x.getChildren().get(i).getChildren().add(0, child);
					leftSibling.getChildren().remove(leftSibling.getNumOfKeys());
				}
				x.getKeys().set(i - 1, leftKey);
				x.getValues().set(i - 1, leftValue);
				x.getChildren().get(i).getKeys().add(0, pKey);
				x.getChildren().get(i).getValues().add(0, pValue);
				x.getChildren().get(i).setNumOfKeys(x.getChildren().get(i).getNumOfKeys() + 1);
				leftSibling.getKeys().remove(leftSibling.getNumOfKeys() - 1);
				leftSibling.getValues().remove(leftSibling.getNumOfKeys() - 1);
				leftSibling.setNumOfKeys(leftSibling.getNumOfKeys() - 1);
				deleteByNode(x.getChildren().get(i), key);
				return;
			}
		}
		if (x == root && x.getNumOfKeys() == 1) {
			//System.out.println("true");
			IBTreeNode<K, V> left = root.getChildren().get(0);
			IBTreeNode<K, V> right = root.getChildren().get(1);
			left.getKeys().add(root.getKeys().get(0));
			left.getValues().add(root.getValues().get(0));
			for (int j = 0; j < right.getNumOfKeys(); j++) {
				left.getKeys().add(right.getKeys().get(j));
				left.getValues().add(right.getValues().get(j));
			}
			if (!left.isLeaf()) {
				for (int j = 0; j <= right.getNumOfKeys(); j++) {
				left.getChildren().add(right.getChildren().get(j));
			}
			}
			left.setNumOfKeys(left.getNumOfKeys() + right.getNumOfKeys() + 1);
			root = left;;
			deleteByNode(root, key);
		}
		else if (x.getNumOfKeys() >= i+1) {
			K pKey = x.getKeys().get(i);
			V pValue = x.getValues().get(i);
			x.getChildren().get(i).getKeys().add(pKey);
			x.getChildren().get(i).getValues().add(pValue);
			IBTreeNode<K, V> rightSibling = x.getChildren().get(i+ 1);
			for (int j = 0; j < rightSibling.getNumOfKeys(); j++) {
				x.getChildren().get(i).getKeys().add(rightSibling.getKeys().get(j));
				x.getChildren().get(i).getValues().add(rightSibling.getValues().get(j));
			}
			if (!x.getChildren().get(i).isLeaf()) {
				for (int j = 0; j <= rightSibling.getNumOfKeys(); j++) {
				x.getChildren().get(i).getChildren().add(rightSibling.getChildren().get(j));
			}
			}
			x.getChildren().get(i).setNumOfKeys(x.getChildren().get(i).getNumOfKeys() + rightSibling.getNumOfKeys() + 1);
			for (int j = i; j < x.getNumOfKeys()-1; j++) {
				x.getKeys().set(j, x.getKeys().get(j+1));
				x.getValues().set(j, x.getValues().get(j+1));
			}
			for (int j = i+1; j < x.getNumOfKeys(); j++) {
				x.getChildren().set(j, x.getChildren().get(j + 1));
			}
			x.getKeys().remove(x.getNumOfKeys()-1);
			x.getValues().remove(x.getNumOfKeys()-1);
			x.getChildren().remove(x.getNumOfKeys());
			x.setNumOfKeys(x.getNumOfKeys() - 1);
			deleteByNode(x.getChildren().get(i), key);
		} else if (i > 0) {
			K pKey = x.getKeys().get(i - 1);
			V pValue = x.getValues().get(i - 1);
			IBTreeNode<K, V> leftSibling = x.getChildren().get(i - 1);
			leftSibling.getKeys().add(pKey);
			leftSibling.getValues().add(pValue);
			for (int j = 0; j < x.getChildren().get(i).getNumOfKeys(); j++) {
				leftSibling.getKeys().add(x.getChildren().get(i).getKeys().get(j));
				leftSibling.getValues().add(x.getChildren().get(i).getValues().get(j));
			}
			if (!x.getChildren().get(i).isLeaf()) {
				for (int j = 0; j <= x.getChildren().get(i).getNumOfKeys(); j++) {
					leftSibling.getChildren().add(x.getChildren().get(i).getChildren().get(j));
				}
			}
			leftSibling.setNumOfKeys(x.getChildren().get(i).getNumOfKeys() + leftSibling.getNumOfKeys() + 1);
			for (int j = i + 1; j < x.getNumOfKeys()-1; j++) {
				x.getKeys().set(j, x.getKeys().get(j+1));
				x.getValues().set(j, x.getValues().get(j+1));
			}
			for (int j = i; j < x.getNumOfKeys(); j++) {
				x.getChildren().set(j, x.getChildren().get(j + 1));
			}
			x.getKeys().remove(x.getNumOfKeys()-1);
			x.getValues().remove(x.getNumOfKeys()-1);
			x.getChildren().remove(x.getNumOfKeys());
			x.setNumOfKeys(x.getNumOfKeys() - 1);
			deleteByNode(leftSibling, key);
		}
	}

	private Pair<K, V> predecessor(IBTreeNode<K, V> x, K key, int i) {
		IBTreeNode<K, V> y = x.getChildren().get(i);
		while (!y.isLeaf()) {
			y = y.getChildren().get(y.getNumOfKeys());
		}
		K predKey = y.getKeys().get(y.getNumOfKeys() - 1);
		V predValue = y.getValues().get(y.getNumOfKeys() - 1);
		Pair<K, V> p = new Pair<>(predKey, predValue);
		return p;
	}

	private Pair<K, V> successor(IBTreeNode<K, V> x, K key, int i) {
		IBTreeNode<K, V> y = x.getChildren().get(i + 1);
		while (!y.isLeaf()) {
			y = y.getChildren().get(0);
		}
		K succKey = y.getKeys().get(0);
		V succValue = y.getValues().get(0);
		Pair<K, V> p = new Pair<>(succKey, succValue);
		return p;
	}

	private IBTreeNode<K, V> searchByNode(IBTreeNode<K, V> x, K key) {
		int i = 0;
		while (i < x.getNumOfKeys() && key.compareTo(x.getKeys().get(i)) > 0) {
			i = i + 1;
		}
		if (i < x.getNumOfKeys() && key.compareTo(x.getKeys().get(i)) == 0) {
			return x;
		} else if (x.isLeaf()) {
			return null;
		} else {
			return searchByNode(x.getChildren().get(i), key);
		}
	}

	private IBTreeNode<K, V> split(IBTreeNode<K, V> x, int i) {
		IBTreeNode<K, V> z = new Node<K, V>();
		IBTreeNode<K, V> y = x.getChildren().get(i);
		z.setLeaf(y.isLeaf());
		z.setNumOfKeys(t - 1);
		z.setKeys(new ArrayList<>(y.getKeys().subList(t, 2 * t - 1)));
		z.setValues(new ArrayList<>(y.getValues().subList(t, 2 * t - 1)));
		if (!y.isLeaf()) {
			z.setChildren(new ArrayList<>(y.getChildren().subList(t, 2 * t)));
		}
		y.setNumOfKeys(t - 1);
		tempk = x.getKeys();
		tempk.add(i, y.getKeys().get(t - 1));
		x.setKeys(tempk);
		tempv = x.getValues();
		tempv.add(i, y.getValues().get(t - 1));
		x.setValues(tempv);
		tempc = x.getChildren();
		tempc.add(i + 1, z);
		x.setChildren(tempc);
		y.setKeys(new ArrayList<>(y.getKeys().subList(0, t - 1)));
		y.setValues(new ArrayList<>(y.getValues().subList(0, t - 1)));
		if (!y.isLeaf()) {
			y.setChildren(new ArrayList<>(y.getChildren().subList(0, t)));
		}
		x.setNumOfKeys(x.getNumOfKeys() + 1);
		return x;
	}

	private void insertNonFull(IBTreeNode<K, V> x, K key, V value) {
		if (x.isLeaf()) {
			int i = 0;
			for (i = 0; i < x.getNumOfKeys(); i++) {
				if (key.compareTo(x.getKeys().get(i)) < 0) {
					break;
				} else if (key.equals(x.getKeys().get(i))) {
					return;
				}
			}
			x.getKeys().add(i, key);
			x.getValues().add(i, value);
			x.setNumOfKeys(x.getNumOfKeys() + 1);
			return;
		}
		int i = 0;
		for (i = 0; i < x.getNumOfKeys(); i++) {
			if (key.compareTo(x.getKeys().get(i)) < 0) {
				break;
			} else if (key.equals(x.getKeys().get(i))) {
				return;
			}
		}
		if (x.getChildren().get(i).getNumOfKeys() == 2 * t - 1) {
			x = split(x, i);
			if (x.getKeys().get(i).compareTo(key) < 0) {
				i++;
			} else if (key.equals(x.getKeys().get(i))) {
				return;
			}
		}
		insertNonFull(x.getChildren().get(i), key, value);
		return;
	}

	private class Node<L extends Comparable<L>, M> implements IBTreeNode<L, M> {

		private int nOfKeys = 0;
		private boolean leaf = true;
		private List<L> keys = new ArrayList<L>();
		private List<M> values = new ArrayList<M>();
		private List<IBTreeNode<L, M>> children = new ArrayList<IBTreeNode<L, M>>();

		@Override
		public int getNumOfKeys() {
			// TODO Auto-generated method stub
			return nOfKeys;
		}

		@Override
		public void setNumOfKeys(int numOfKeys) {
			// TODO Auto-generated method stub
			nOfKeys = numOfKeys;
		}

		@Override
		public boolean isLeaf() {
			// TODO Auto-generated method stub
			return leaf;
		}

		@Override
		public void setLeaf(boolean isLeaf) {
			// TODO Auto-generated method stub
			leaf = isLeaf;
		}

		@Override
		public List<L> getKeys() {
			// TODO Auto-generated method stub
			return keys;
		}

		@Override
		public void setKeys(List<L> keys) {
			// TODO Auto-generated method stub
			this.keys = keys;
		}

		@Override
		public List<M> getValues() {
			// TODO Auto-generated method stub
			return values;
		}

		@Override
		public void setValues(List<M> values) {
			// TODO Auto-generated method stub
			this.values = values;
		}

		@Override
		public List<IBTreeNode<L, M>> getChildren() {
			// TODO Auto-generated method stub
			return children;
		}

		@Override
		public void setChildren(List<IBTreeNode<L, M>> children) {
			// TODO Auto-generated method stub
			this.children = children;
		}

	}
}
