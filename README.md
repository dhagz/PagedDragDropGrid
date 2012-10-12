PagedDragDropGrid
=================

An Android ViewGroup that implements a paged grid with drag'n'drop movable items

Example video: http://youtu.be/iCXZtPvP5Ec

Usage:

Define an adapter conforming to interface PagedDragDropGridAdapter.java

    /**
	 * Used to create the paging
	 * 
	 * @return the page count
	 */
	public int pageCount();
	

	/**
	 * Returns the count of item in a page
	 * 
	 * @param page index
	 * @return item count for page
	 */
	public int itemCountInPage(int page);
	
	/**
	 * Returns the view for the item in the page
	 * 
	 * @param page index
	 * @param item index
	 * @return the view 
	 */
	public View view(int page, int index);
	
	/**
	 * The fixed row count (-1 for automatic computing)
	 * 
	 * @return row count or -1
	 */
	public int rowCount();
	
	/**
	 * The fixed column count (-1 for automatic computing)
	 * 
	 * @return column count or -1
	 */
	public int columnCount();

	
	/**
	 * Prints the layout in Log.d();
	 */
	public void printLayout();

	/**
	 * Swaps two items in he item list in a page
	 * 
	 * @param pageIndex
	 * @param itemIndexA
	 * @param itemIndexB
	 */
	public void swapItems(int pageIndex, int itemIndexA, int itemIndexB);

	/**
	 * Moves an item in the page on the left of provided the page
	 * 
	 * @param pageIndex
	 * @param itemIndex
	 */
	public void moveItemToPageOnLeft(int pageIndex, int itemIndex);

	/**
	 * Moves an item in the page on the right of provided the page
	 * 
	 * @param pageIndex
	 * @param itemIndex
	 */
	public void moveItemToPageOnRight(int pageIndex, int itemIndex);
	

layout example.xml
----------
	<ca.laplanete.mobile.pageddragdropgrid.PagedDragDropGrid xmlns:android="http://schemas.android.com/apk/res/android"
    	android:id="@+id/gridview"
    	android:layout_width="fill_parent"
    	android:layout_height="fill_parent"/>
    
    
ExampleActivity.java
-------------
	setContentView(R.layout.example);
	PagedDragDropGrid gridview = (PagedDragDropGrid) findViewById(R.id.gridview);		
	gridview.setAdapter(new ExamplePagedDragDropGridAdapter(this));

License
-------

	/**
 	  * Copyright 2012 Nicolas Desjardins  
 	  * https://github.com/laplanete79
 	  * 
 	  * Permission is hereby granted, free of charge, to any person obtaining
 	  * a copy of this software and associated documentation files (the
 	  * "Software"), to deal in the Software without restriction, including
 	  * without limitation the rights to use, copy, modify, merge, publish,
 	  * distribute, sublicense, and/or sell copies of the Software, and to
 	  * permit persons to whom the Software is furnished to do so, subject to
 	  * the following conditions:
 	  * 
 	  * The above copyright notice and this permission notice shall be
 	  * included in all copies or substantial portions of the Software.
 	  * 
 	  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 	  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 	  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
	  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 	  * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 	  * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 	  * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
	 */