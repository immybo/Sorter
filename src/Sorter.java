public class Sorter {
	private GUI gui;
	private int[] data;

	// Whether or not a sort is currently being performed
	private boolean isSorting = false;

	public static void main(String[] args){
		Sorter sorter = new Sorter();
        sorter.gui = new GUI(sorter);
	}

	public Sorter(){}

	/**
	 * Returns whether or not this sorter is currently performing a sort.
	 */
	public boolean isSorting(){
		return isSorting;
	}

	/**
	 * Updates the GUI's view of the data and repaints it.
	 * @param considering The current indices of the data which is being considered.
	 */
	private void update(int[] considering){
    	gui.updateData(data);
    	gui.redrawSortPanel(considering);
	}

	/**
	 * Begins a new sort, with the parameters taken directly from this Sorter's GUI.
	 */
	public void beginSort(){
		new Thread(new InternalSorter()).start();
	}

	/**
	 * Internally sorts on a seperate thread to avoid hogging resources
	 * on the only thread, so that we can, for example, constantly increment
	 * the amount of comparisons performed on the GUI.
	 *
	 * @author campberobe1
	 *
	 */
	private class InternalSorter implements Runnable{
		public void run(){
			beginSort(gui.getSortMethod(), gui.getAmount(), gui.getDelay());
		}

		/**
		 * Pauses the program execution for the specified delay (returns after the delay)
		 */
		private void pause(double delay){
			try{
				// Get the value after the decimal place
				double leftOver = (double)delay - (int)delay;

				// Sleep for the number of ms, plus 1000000*numbers after the decimal point in nanoseconds
				Thread.sleep((int)delay, (int)(leftOver*1000000));
			}
			catch(InterruptedException e){
				System.out.println("Thread sleeping interrupted. " + e);
			}
		}

		/**
		 * Begins performing a sort and sending data to the GUI.
		 * @param sortMethod The method of sorting to use. Valid input is as follows:
		 * "Selection Sort" -> doSelectionSort
		 * @param amount The amount of data points to sort.
		 * @param delay The pause, in ms, between each comparison operation.
		 */
		private void beginSort(String sortMethod, int amount, int delay){
			gui.resetComparisons();
			isSorting = true;

			// Create a data set (just ints with even intervals is fine) with the specified amount of elements
			data = new int[amount];
			for(int i = 0; i < data.length; i++)
				data[i] = i+1; // We don't want any data to be 0 because it would have 0 height in the GUI

			// Randomise this data set (not concerned with "true" or even approximate randomness, just jumbledness)
			for(int i = 0; i < data.length; i++){
				int index1 = (int)(Math.random()*data.length);
				int index2 = (int)(Math.random()*data.length);

				int temp = data[index1];
				data[index1] = data[index2];
				data[index2] = temp;
			}

			gui.setData(data);

			// Select the correct method based on the type of sort
			switch(sortMethod){
				case "Selection Sort":
					doSelectionSort(data, delay);
					break;
				case "Insertion Sort":
					doInsertionSort(data, delay);
					break;
				case "Bubble Sort":
					doBubbleSort(data, delay);
					break;
				case "Merge Sort":
					doMergeSort(data, delay);
					break;
				case "Quick Sort":
					doQuickSort(data, delay);
					break;
			}

			for(int i = 0; i < data.length; i++){
				int[] highlight = new int[i+1];
				for(int j = 0; j <= i; j++){
					highlight[j] = j;
				}
				update(highlight);
				pause(delay);
			}
			update(new int[]{});

			isSorting = false;
		}

		/**
		 * Performs selection sort on the given data, updating the
		 * GUI every comparison and returning when finished sorting.
		 * @param data The data to sort.
		 * @param delay The delay to use between each comparison.
		 */
		private void doSelectionSort(int[] data, double delay){
			// Go through the entire length of the data set
			for(int i = 0; i < data.length; i++){
				// For every index, find the lowest data from the data set greater than this index
				int minIndex = i;
				for(int j = i; j < data.length; j++){
					gui.incrementComparisons();
					update(new int[]{j});
					pause(delay);
					if(data[j] < data[minIndex]) minIndex = j;
				}

				// And swap it into that spot
				int temp = data[i];
				data[i] = data[minIndex];
				data[minIndex] = temp;
			}
		}

		/**
		 * Performs insertion sort on the given data, updating the
		 * GUI every comparison and returning when finished sorting.
		 * @param data The data to sort.
		 * @param delay The delay to use between each comparison.
		 */
		private void doInsertionSort(int[] data, double delay){
			// Go through the entire length of the data set
			for(int i = 0; i < data.length; i++){
				// For every index, figure out where it needs to be in the already sorted part of the array
				int newIndex = 0;
				for(int j = i-1; j >= 0; j--){
					if(data[i] >= data[j]){ newIndex = j+1; break; }
					gui.incrementComparisons();
					update(new int[]{i,j});
					pause(delay);
				}

				// Swap the data up to make room for this data
				int newData = data[i];
				for(int j = i; j > newIndex; j--){
					data[j] = data[j-1];
				}
				data[newIndex] = newData;
			}
		}

		/**
		 * Performs insertion sort on a section of the given data,
		 * updating the GUI every comparison and returning when finished sorting.
		 * @param data The data to sort.
		 * @param delay The delay to use between each comparison.
		 * @param min The minimum index to consider (inclusive).
		 * @param max The maximum index to consider (exclusive).
		 */
		private void doInsertionSort(int[] data, double delay, int min, int max){
			// Go through the data between min and max
			for(int i = min; i < max; i++){
				// For every index, figure out where it needs to be in the already sorted part of the array
				int newIndex = min;
				for(int j = i-1; j >= min; j--){
					if(data[i] >= data[j]){ newIndex = j+1; break; }
					gui.incrementComparisons();
					update(new int[]{i,j});
					pause(delay);
				}

				// Swap the data up to make room for this data
				int newData = data[i];
				for(int j = i; j > newIndex; j--){
					data[j] = data[j-1];
				}
				data[newIndex] = newData;
			}
		}

		/**
		 * Performs bubble sort on the given data, updating the
		 * GUI every comparison and returning when finished sorting.
		 * @param data The data to sort.
		 * @param delay The delay to use between each comparison.
		 */
		private void doBubbleSort(int[] data, double delay){
			// Go through the array until the data is sorted, returning when it is sorted
			while(true){
				int swapCount = 0;
				for(int i = 0; i < data.length-1; i++){
					// Consider, for all data points except the last, the point and the next point
					gui.incrementComparisons();
					update(new int[]{i, i+1});
					pause(delay);

					// Are they in the wrong order?
					if(data[i] >= data[i+1]){
						// Swap them if so
						int temp = data[i];
						data[i] = data[i+1];
						data[i+1] = temp;
						swapCount++;
					}
				}

				// If no swaps were made, it is sorted
				if(swapCount == 0) break;
			}
		}

		/**
		 * Wrapper method for MergeSort;
		 * Performs MergeSort on the given data, updating
		 * the GUI every comparison and returning when
		 * finished sorting.
		 * @param data The data to sort.
		 * @param delay The delay to use between each comparison.
		 */
		private void doMergeSort(int[] data, int delay){
			doMergeSort(data, delay, 0, data.length-1);
		}

		/**
		 * Recursive method for MergeSort;
		 * Should be called through doMergeSort(data, delay)
		 *
		 * @param min The minimum index to consider (inclusive).
		 * @param max The maximum index to consider (inclusive).
		 */
		private void doMergeSort(int[] data, int delay, int min, int max){
			// If the data only has one element, return
			if(max == min)
				return;
			// If it has two, sort them and return
			if(max-min == 1){
				gui.incrementComparisons();
				update(new int[]{min, max});
				pause(delay);

				if(data[max] < data[min]){
					int temp = data[max];
					data[max] = data[min];
					data[min] = temp;
				}

				return;
			}

			// Otherwise, call mergeSort on two arrays of half the size.
			int midPoint = (int)((max-min)/2 + min);
			doMergeSort(data, delay, min, midPoint);
			doMergeSort(data, delay, midPoint+1, max);
			// And merge the results together
			merge(data, delay, min, midPoint+1, max);
		}

		/**
		 * Merges two sets of data together (in the same array)
		 * @param min The minimum index to consider (inclusive).
		 * @param max The maximum index to consider (inclusive).
		 * @param mid The first index of the right part of the data.
		 */
		private void merge(int[] data, int delay, int min, int mid, int max){
			int leftPointer = min;
			int rightPointer = mid;
			int i = 0;
			int[] newData = new int[max-min+1];

			// Continue until either we've gone through all the items in the left part, or in the right part
			while(leftPointer < mid && rightPointer <= max){
				gui.incrementComparisons();
				update(new int[]{leftPointer, rightPointer});
				pause(delay);

				if(data[leftPointer] > data[rightPointer]){
					newData[i] = data[rightPointer];
					rightPointer++;
				}
				else{
					newData[i] = data[leftPointer];
					leftPointer++;
				}
				i++;
			}

			// And shove the other one onto the end (only one of these does anything)
			while(leftPointer < mid){
				newData[i] = data[leftPointer];
				leftPointer++;
				i++;
			}
			while(rightPointer <= max){
				newData[i] = data[rightPointer];
				rightPointer++;
				i++;
			}

			for(int j = min; j <= max; j++)
				data[j] = newData[j-min];
		}

		/**
		 * Wrapper method for QuickSort;
		 * Performs QuickSort on the given data, updating
		 * the GUI every comparison and returning when
		 * finished sorting.
		 * @param data The data to sort.
		 * @param delay The delay to use between comparisons.
		 */
		private void doQuickSort(int[] data, int delay){
			doQuickSort(data, delay, 0, data.length);
		}

		/**
		 * Recursive method for QuickSort;
		 * Should be called through doQuickSort(data, delay)
		 *
		 * @param min The minimum index to consider (inclusive).
		 * @param max The maximum index to consider (exclusive).
		 */
		private void doQuickSort(int[] data, int delay, int min, int max){
			// If there are only 8 or less elements, just do insertion sort on them (it's faster)
			if(max - min < 8){
				doInsertionSort(data, delay, min, max);
				return;
			}

			// Otherwise, choose a pivot point and put data below it on the left and data above it on the right
			int pivotIndex = choosePivotPoint(data, delay, min, max);
			int pivotPoint = data[pivotIndex];

			// Indices of where to swap data to
			int leftIndex = min-1;
			int rightIndex = max;

			while(leftIndex <= rightIndex){
				// Check if they're already on the correct side

				// Is the data at the left less than the data at the pivot point?
				do {
					leftIndex++;
					gui.incrementComparisons();
					update(new int[]{leftIndex});
					pause(delay);
				}
				while(leftIndex < max && data[leftIndex] < pivotPoint);

				// Is the data on the right more than the data at the pivot point?
				do{
					rightIndex--;
					gui.incrementComparisons();
					update(new int[]{rightIndex});
					pause(delay);
				}
				while(rightIndex >= min && data[rightIndex] >= pivotPoint);

				if(leftIndex < rightIndex){
					int temp = data[leftIndex];
					data[leftIndex] = data[rightIndex];
					data[rightIndex] = temp;

					gui.incrementComparisons();
					update(new int[]{leftIndex, rightIndex});
					pause(delay);
				}
			}

			doQuickSort(data, delay, min, rightIndex+1);
			doQuickSort(data, delay, rightIndex+1, max);
		}

		/**
		 * Chooses a pivot point in the given data.
		 * Also updates the GUI every comparison, and delays
		 * between comparisons.
		 *
		 * Returns -1 immediately if there are less than 4 items
		 * in the choice area.
		 *
		 * @param data The data to choose the pivot point from.
		 * @param delay The delay to use between comparisons.
		 * @param min The minimum index to consider in the data (inclusive).
		 * @param max The maximum index to consider in the data (exclusive).
		 *
		 * @return The index of the chosen pivot point in the data.
		 */
		private int choosePivotPoint(int[] data, int delay, int min, int max){
			if(max-min < 4) return -1;

			// Grab 3 points: one from the start, one from the middle and one from the end.
			// These place choices are arbitrary, as the data is not yet sorted.
			// However, using 3 is important so as not to run into an infinite loop where
			// we continuously choose the largest or smallest element as the pivot point.
			int mid = (min+max)/2;
			// Take the median of the 3 points to use as our pivot point.
			int median = min;

			gui.incrementComparisons();
			update(new int[]{mid, min});
			pause(delay);
			if(data[mid] > data[min]){
				gui.incrementComparisons();
				update(new int[]{max-1, mid});
				pause(delay);

				if(data[max-1] > data[mid]){
					median = mid;
				}

				else{
					gui.incrementComparisons();
					update(new int[]{max-1, min});
					pause(delay);
					if(data[max-1] > data[min]){
						median = max-1;
					}
				}
			}
			else if(data[min] > data[max-1]){
				gui.incrementComparisons();
				update(new int[]{max-1, min});
				pause(delay);

				gui.incrementComparisons();
				update(new int[]{max-1, mid});
				pause(delay);

				if(data[max-1] > data[mid]){
					median = max-1;
				}
				else{
					median = mid;
				}
			}

			return median;
		}
	}
}
