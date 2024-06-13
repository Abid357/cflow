package helpers;

import java.util.Comparator;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;

/*
 *  Custom model to make sure the items are stored in a sorted order.
 *  The default is to sort in the natural order of the item, but a
 *  Comparator can be used to customize the sort order.
 */
//class SortedComboBoxModel extends DefaultComboBoxModel
public class SortedComboBoxModel<E> extends DefaultComboBoxModel<E> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Comparator<E> comparator;
	private boolean isItem;

	@SuppressWarnings("unchecked")
	private E getItemElement(E element) {
		String string = element.toString();
		string = string.substring(string.indexOf(" ") + 1);
		E duplicate = (E) string;
		return duplicate;
	}

	public SortedComboBoxModel(boolean isItem) {
		super();
		this.isItem = isItem;
	}

	/*
	 * Create an empty model that will use the natural sort order of the item
	 */
	public SortedComboBoxModel() {
		super();
		this.isItem = false;
	}

	/*
	 * Create an empty model that will use the specified Comparator
	 */
	public SortedComboBoxModel(Comparator<E> comparator) {
		super();
		this.comparator = comparator;
	}

	/*
	 * Create a model with data and use the nature sort order of the items
	 */
	public SortedComboBoxModel(E items[]) {
		this(items, null);
	}

	/*
	 * Create a model with data and use the specified Comparator
	 */
	public SortedComboBoxModel(E items[], Comparator<E> comparator) {
		this.comparator = comparator;

		for (E item : items) {
			addElement(item);
		}
	}

	/*
	 * Create a model with data and use the nature sort order of the items
	 */
	public SortedComboBoxModel(Vector<E> items) {
		this(items, null);
	}

	/*
	 * Create a model with data and use the specified Comparator
	 */

	public SortedComboBoxModel(Vector<E> items, Comparator<E> comparator) {
		this.comparator = comparator;

		for (E item : items) {
			addElement(item);
		}
	}

	private boolean isDefaultItem(E element) {
		return element.toString().equals("ANY");
	}

	@Override
	public void addElement(E element) {
		insertElementAt(element, 0);
	}

	@SuppressWarnings("unchecked")
	public void insertItemElementAt(E element, int index) {
		int size = getSize();

		E duplicate = getItemElement(element);

		// Determine where to insert element to keep model in sorted order
		for (index = 0; index < size; index++) {
			if (comparator != null) {
				E o = getItemElement(getElementAt(index));

				if (comparator.compare(o, duplicate) > 0)
					break;
			} else {
				Comparable<E> c = (Comparable<E>) getItemElement(getElementAt(index));

				if (c.compareTo(duplicate) > 0)
					break;
			}
		}

		super.insertElementAt(element, index);

		// Select an element when it is added to the beginning of the model

		if (index == 0 && element != null) {
			setSelectedItem(element);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void insertElementAt(E element, int index) {
		int size = getSize();

		if (isItem) {
			insertItemElementAt(element, index);
			return;
		}

		// Determine where to insert element to keep model in sorted order
		if (isDefaultItem(element)) {
			super.insertElementAt(element, index);
			return;
		}
		for (index = 0; index < size; index++) {
			if (comparator != null) {
				E o = getElementAt(index);

				if (comparator.compare(o, element) > 0 && !isDefaultItem(o))
					break;
			} else {
				Comparable<E> c = (Comparable<E>) getElementAt(index);

				if (c.compareTo(element) > 0 && !isDefaultItem(getElementAt(index)))
					break;
			}
		}

		super.insertElementAt(element, index);

		// Select an element when it is added to the beginning of the model

		if (index == 0 && element != null) {
			setSelectedItem(element);
		}
	}
}
