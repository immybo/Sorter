import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class GUI {
	// Initial position/size parameters of the frame
	// Note that frame width is based on the panel widths
	private static final int INIT_X = 400;
	private static final int INIT_Y = 200;
	private static final int INIT_HEIGHT = 500;

	// Initial size parameters of the panels
	// Note that panel height is based on the frame height
	private static final int MENU_INIT_WIDTH = 200;
	private static final int SORT_INIT_WIDTH = 500;

	// The width between the menu section and the sorting section
	private static final int MIDDLE_GAP = 40;

	// The color to draw the data which is currently being considered.
	private static final Color CONSIDERING_COLOR = Color.YELLOW;

	private String sortMethod;

	private JFrame frame;
	private JPanel menuPanel;
	private SortPanel sortPanel;

	// How many items to sort
	private JSpinner amount;
	// The delay between comparison operations
	private JSpinner delay;

	private JLabel comparisons;
	private int numComparisons = 0;

	private Set<JButton> sortButton;
	private ActionListener buttonListener;

	public GUI(Sorter sorter){
		frame = new JFrame();
		frame.setSize(new Dimension(MENU_INIT_WIDTH + MIDDLE_GAP + SORT_INIT_WIDTH, INIT_HEIGHT));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.setTitle("Sorting Demo");

		// We don't really need another class for the menu panel, because it just has a bunch of buttons and a field.
		menuPanel = new JPanel();
		menuPanel.setPreferredSize(new Dimension(MENU_INIT_WIDTH, INIT_HEIGHT));


		JLabel amountLabel = new JLabel("Amount of Data:");
		menuPanel.add(amountLabel);

		amount = new JSpinner(new SpinnerNumberModel(20, 10, 1000, 10));
		amount.setPreferredSize(new Dimension(MENU_INIT_WIDTH, 50));

		menuPanel.add(amount);

		JLabel delayLabel = new JLabel("Delay per Comparison:");
		menuPanel.add(delayLabel);

		delay = new JSpinner(new SpinnerNumberModel(50, 1, 100, 5));
		delay.setPreferredSize(new Dimension(MENU_INIT_WIDTH, 50));

		menuPanel.add(delay);

		comparisons = new JLabel("Comparisons: 0");
		menuPanel.add(comparisons);

		// Add all the sort buttons
		sortButton = new HashSet<JButton>();
		buttonListener = new ActionListener(){
			public void actionPerformed(ActionEvent e){
				// Only do anything if it's from one of the buttons.
				// (Otherwise how did it go off? Still good to check.)
				if(sortButton.contains(e.getSource())){
					// Make sure we're not currently performing a sort first!
					if(!sorter.isSorting()){
						// If we're not, begin a sort with the method that this button references
						sortMethod = ((JButton)e.getSource()).getText();
						sorter.beginSort();
					}
				}
			}
		};

		addSortButton("Selection Sort");
		addSortButton("Insertion Sort");
		addSortButton("Bubble Sort");
		addSortButton("Merge Sort");
		addSortButton("Quick Sort");

		frame.add(menuPanel, BorderLayout.WEST);

		// The sort panel, however, needs to have a special redraw method, so we override it with an inner class.
		sortPanel = new SortPanel();
		sortPanel.setPreferredSize(new Dimension(SORT_INIT_WIDTH, INIT_HEIGHT));
		frame.add(sortPanel, BorderLayout.EAST);

		frame.setLocation(INIT_X, INIT_Y);
		frame.setVisible(true);
	}

	/**
	 * Increments the amount of comparisons
	 */
	public void incrementComparisons(){
		numComparisons++;
		comparisons.setText("Comparisons: " + numComparisons);
		menuPanel.repaint();
	}

	/**
	 * Resets the amount of comparisons to 0
	 */
	public void resetComparisons(){
		numComparisons = 0;
		comparisons.setText("Comparisons: " + numComparisons);
	}

	/**
	 * Sets the array of data that this SortPanel will draw.
	 * This also updates the width and height of each bit of data;
	 * call updateData instead most times you update it.
	 * (Does not support negative values, as there is no need.)
	 */
	public void setData(int[] data){
		sortPanel.setData(data);
	}

	/**
	 * Updates the data, assuming that it has the same maximum
	 * and is of the same length as the previous set of data.
	 */
	public void updateData(int[] data){
		sortPanel.updateData(data);
	}

	/**
	 * Redraws the sort panel with whatever data it currently has.
	 * @param The considering indices to use.
	 */
	public void redrawSortPanel(int[] considering){
		sortPanel.updateConsidering(considering);
		sortPanel.update(sortPanel.getGraphics());
		frame.revalidate();
	}

	public String getSortMethod(){
		return sortMethod;
	}
	public int getAmount(){
		return (int)amount.getValue();
	}
	public int getDelay(){
		return (int)delay.getValue();
	}

	private void addSortButton(String title){
		JButton newButton = new JButton(title);
		newButton.setPreferredSize(new Dimension(MENU_INIT_WIDTH, 30));
		newButton.addActionListener(buttonListener);
		menuPanel.add(newButton);
		sortButton.add(newButton);
	}

	private class SortPanel extends JPanel {
		private int[] data;

		// The amount of height per value in a piece of data; e.g. if heightPer == 2, the height of
		// a piece of data with value 100 will be 200.
		private int heightPer = 0;

		// The width that each piece of data should take
		private int widthPer = 0;

		private int[] considering;

		/**
		 * Sets the array of data that this SortPanel will draw.
		 * This also updates the width and height of each bit of data;
		 * call updateData instead most times you update it.
		 * (Does not support negative values, as there is no need.)
		 */
		public void setData(int[] newData){
			data = newData;

			// We don't want empty space or invisible values due to integer rounding.
			// As such, we may need to change the width of the sort panel.
			int dataCount = data.length;
			int panelPreferredSize = SORT_INIT_WIDTH;
			// We want to get an integer size per data point that will fill the panel up
			int offset = (int) (SORT_INIT_WIDTH % dataCount);
			panelPreferredSize += dataCount - offset;

			// And calculate the width and height per object too
			widthPer = (int)(panelPreferredSize/dataCount);
			heightPer = (int)(INIT_HEIGHT/dataCount);
			if(heightPer == 0){
				heightPer = 1;
				setPreferredSize(new Dimension(panelPreferredSize, dataCount));
			}
			else{
				setPreferredSize(new Dimension(panelPreferredSize, INIT_HEIGHT));
			}

			frame.pack();
		}

		/**
		 * Updates the data, assuming that it has the same maximum
		 * and is of the same length as the previous set of data.
		 */
		public void updateData(int[] newData){
			data = newData;
		}

		/**
		 * Updates the points which are currently being considered.
		 */
		public void updateConsidering(int[] newConsidering){
			considering = newConsidering;
		}

		/**
		 * Draws the data in order, using up the entire area with the width
		 * of each one being based on how many there are. The maximum number in the
		 * data corresponds to the maximum height of this SortPanel.
		 */
		@Override
		public void paintComponent(Graphics g){
			super.paintComponent(g);

			Graphics2D g2d = (Graphics2D)g;

			g2d.setColor(Color.WHITE);
			g2d.fillRect(0, 0, getWidth(), getHeight());

			if(data == null) return;

			// Scroll through it and draw every piece of data
			g2d.setColor(Color.BLACK);
			for(int i = 0; i < data.length; i++){
				int height = heightPer * data[i];

				g2d.fillRect( i * widthPer,
					getHeight() - height,
					widthPer,
					height );
			}

			if(considering == null) return;

			g2d.setColor(CONSIDERING_COLOR);
			for(int i = 0; i < considering.length; i++){
				int height = heightPer * data[considering[i]];
				g2d.fillRect( considering[i]*widthPer, getHeight()-height, (int)widthPer, height);
			}
		}
	}
}
