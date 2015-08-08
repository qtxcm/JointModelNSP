package microblog;

import java.util.ArrayList;
import java.util.Arrays;

public class HeapSort {
	public static void main(String[] args) {
		int[] array = { 4, 1, 3, 2, 16, 9, 10, 14, 8, 7 };
		System.out.println(Arrays.toString(array));

		HeapSort heapSort = new HeapSort();
		// heapSort.heapSort(array, array.length);
		System.out.println(Arrays.toString(array));
	}

	public State[] heapSortK(State[] array, int n, int k) {
		if (array == null) {
			throw new NullPointerException();
		}
		if (n > array.length) {
			throw new ArrayIndexOutOfBoundsException();
		}

		buildMaxHeap(array, n);

		for (int i = n - 1; i >= n - k; i--) {
			swap(array, 0, i);
			maxHeapify(array, 0, i);
		}

		State[] arrSt = new State[k];
		for (int i = 0; i < k; i++) {
			arrSt[0] = array[n - i - 1];
		}
		return arrSt;

	}

	public void heapSort(State[] array, int n) {
		if (array == null) {
			throw new NullPointerException();
		}
		if (n > array.length) {
			throw new ArrayIndexOutOfBoundsException();
		}

		buildMaxHeap(array, n);

		for (int i = n - 1; i >= 1; i--) {
			swap(array, 0, i);
			maxHeapify(array, 0, i);
		}
	}

	/**
	 * Constructing max heap.
	 * 
	 * @param a
	 * @param n
	 */
	public void buildMaxHeap(State[] a, int n) {

		for (int i = n / 2 - 1; i >= 0; i--) {
			maxHeapify(a, i, n);
		}
	}

	/**
	 * Constructing min heap.
	 * 
	 * @param a
	 * @param n
	 */
	public void buildMinHeap(State[] a, int n) {
		for (int i = n / 2 - 1; i >= 0; i--) {
			minHeapify(a, i, n);
		}
	}

	/**
	 * Inserting a node to min heap
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public State[] BestAgendaSort(State[] a, State b) {
		a[0] = b;
		buildMinHeap(a, a.length);
		// minHeapify(a, 0, a.length);
		return a;
	}

	/**
	 * Constructing min heap by specifying range.
	 * 
	 * @param a
	 * @param i
	 * @param n
	 */
	private void minHeapify(State[] a, int i, int n) {
		int smallest;
		int leftIndex = 2 * i + 1;
		int rightIndex = leftIndex + 1;

		if (leftIndex < n && a[leftIndex].score < a[i].score) {
			smallest = leftIndex;
		} else {
			smallest = i;
		}
		if (rightIndex < n && a[rightIndex].score < a[smallest].score) {
			smallest = rightIndex;
		}
		if (smallest != i) {
			swap(a, i, smallest);
			minHeapify(a, smallest, n);
		}
	}

	/**
	 * Constructing max heap by specifying range.
	 * 
	 * @param a
	 * @param i
	 * @param n
	 */
	private void maxHeapify(State[] a, int i, int n) {

		int largest;

		int leftIndex = 2 * i + 1;
		int rightIndex = leftIndex + 1;

		if (leftIndex < n && a[leftIndex].score > a[i].score) {
			largest = leftIndex;
		} else {
			largest = i;
		}
		if (rightIndex < n && a[rightIndex].score > a[largest].score) {
			largest = rightIndex;
		}
		if (largest != i) {
			swap(a, i, largest);
			maxHeapify(a, largest, n);
		}
	}

	private void swap(State[] pArray, int pX, int pY) {
		State temp = pArray[pX];
		pArray[pX] = pArray[pY];
		pArray[pY] = temp;
	}
}