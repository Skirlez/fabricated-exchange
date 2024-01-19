package com.skirlez.fabricatedexchange.emc;

import java.util.ArrayList;
import java.util.List;

/** A barebones queue with several layers. Lower layers are processed first when polling. */
public class LayeredQueue<T> {

	private List<SimpleQueue<T>> queues;
	private int size;

	public LayeredQueue(int layersAmount) {
		queues = new ArrayList<SimpleQueue<T>>(layersAmount);
		for (int i = 0; i < layersAmount; i++)
			queues.add(new SimpleQueue<T>());
		
		size = 0;
	}

	public int size() {
		return size;
	}
	
	public boolean isEmpty() {
		return size == 0;
	}
	
	public void clear() {
		queues.clear();
	}
	
	public boolean add(T obj, int layer) {
		SimpleQueue<T> queue = queues.get(layer);
		size++;
		queue.add(obj);
		return true;
	}
	

	public T poll() {
		for (int i = 0; i < queues.size(); i++) {
			SimpleQueue<T> queue = queues.get(i);
			if (!queue.isEmpty()) {
				size--;
				return queue.poll();
			}
		}
		
		return null;
	}
	
	
	/** Incredibly simple queue. Always use isEmpty() before polling. */
	private class SimpleQueue<E> {
		private Node<E> start = null;
		private Node<E> end = null;

		public SimpleQueue() {
		}
		public boolean isEmpty() {
			return start == null;
		}
		public void add(E obj) {
			if (isEmpty()) {
				start = new Node<E>(obj);
				end = start;
				return;
			}
			end.next = new Node<E>(obj);
			end = end.next;
		}
		public E poll() {
			E value = start.value;
			start = start.next;
			return value;
		}
	}
	private class Node<C> {
		public Node<C> next;
		public C value;
		private Node(C value) {
			this.value = value;
		}
	}
	
}

