/**
 * Copyright 2012
 *
 * Nicolas Desjardins
 * https://github.com/mrKlar
 *
 * Facilite solutions
 * http://www.facilitesolutions.com/
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
package ca.laplanete.mobile.pageddragdropgrid;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

public class DragDropGrid extends ViewGroup implements OnTouchListener, OnLongClickListener {
	
	private static int ANIMATION_DURATION = 250;
	private static int EGDE_DETECTION_MARGIN = 35;

	private PagedDragDropGridAdapter adapter;
	private OnClickListener onClickListener = null;
	private PagedContainer container;

	private List<View> views = new ArrayList<View>(); 
	private SparseIntArray newPositions = new SparseIntArray();

	private int gridPageWidth = 0;
	private int dragged = -1;
	private View draggedView = null;
	private int columnWidthSize;
	private int rowHeightSize;
	private int biggestChildWidth;
	private int biggestChildHeight;
	private int computedColumnCount;
	private int computedRowCount;
	private int initialX;
	private int initialY;
	private boolean movingView;
	private int lastTarget = -1;
	private boolean wasOnEdgeJustNow = false;
	private Timer edgeScrollTimer;

	final private Handler edgeTimerHandler = new Handler();
	private int lastTouchX;
	private int lastTouchY;
	private int gridPageHeight;
	private DeleteDropZoneView deleteZone;
	
	private OnTouchListener onTouchListener;
	private OnLongClickListener onLongClickListener;
	
	// dhagz -- boolean if items should jiggle or not
	private boolean isJiggleItems = false;

	public DragDropGrid(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public DragDropGrid(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public DragDropGrid(Context context) {
		super(context);
		init();
	}

	public DragDropGrid(Context context, AttributeSet attrs, int defStyle, PagedDragDropGridAdapter adapter, PagedContainer container) {
		super(context, attrs, defStyle);
		this.adapter = adapter;
		this.container = container;
		init();
	}

	public DragDropGrid(Context context, AttributeSet attrs, PagedDragDropGridAdapter adapter, PagedContainer container) {
		super(context, attrs);
		this.adapter = adapter;
		this.container = container;
		init();
	}

	public DragDropGrid(Context context, PagedDragDropGridAdapter adapter, PagedContainer container) {
		super(context);
		this.adapter = adapter;
		this.container = container;
		init();
	}

	private void init() {
	    if (isInEditMode() && adapter == null) {
	        useEditModeAdapter();
	    }
	    
		setOnTouchListener(this);
		setOnLongClickListener(this);
		createDeleteZone();
	}

	private void useEditModeAdapter() {
	    adapter = new PagedDragDropGridAdapter() {
            
            @Override
            public View view(int page, int index) {
                return null;
            }
            
            @Override
            public void swapItems(int pageIndex, int itemIndexA, int itemIndexB) {

            }
            
            @Override
            public int rowCount() {
                return AUTOMATIC;
            }
            
            @Override
            public void printLayout() {
    
            }
            
            @Override
            public int pageCount() {
                return AUTOMATIC;
            }
            
            @Override
            public void moveItemToPreviousPage(int pageIndex, Item item) {

            }
            
            @Override
            public void moveItemToNextPage(int pageIndex, Item item) {

            }
            
            @Override
            public int itemCountInPage(int page) {
                return 0;
            }
            
            @Override
            public void deleteItem(int pageIndex, int itemIndex) {

            }
            
            @Override
            public int columnCount() {
                return 0;
            }

            @Override
            public int deleteDropZoneLocation() {
                return PagedDragDropGridAdapter.BOTTOM;
            }

			@Override
			public void setPages(List<Page> pageList) {
			}

			@Override
			public List<Page> getPages() {
				return null;
			}
			
			@Override
			public void deleteItem(Item item) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setRowCount(int cnt) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setColumnCount(int cnt) {
				// TODO Auto-generated method stub
				
			}

            @Override
            public boolean showRemoveDropZone() {
                return true;
            }

			@Override
			public int getPageWidth(int page) {
				return 0;
			}

			@Override
			public Object getItemAt(int page, int index) {
				return null;
			}

			@Override
			public boolean disableZoomAnimationsOnChangePage() {
				return false;
			}
        };       
    }

    public void setAdapter(PagedDragDropGridAdapter adapter) {
		this.adapter = adapter;
		addChildViews();
	}

	public void setOnClickListener(OnClickListener l) {
	    onClickListener = l;
	}

	public void setLongClickListener(OnLongClickListener l) {
	    onLongClickListener = l;
	}

	public void setTouchListener(OnTouchListener l) {
	    onTouchListener = l;
	}
	
    public View getDeleteZone() {
        return deleteZone;
    }
	
	private void addChildViews() {
		if (adapter == null)
			return ;
		
		for (int page = 0; page < adapter.pageCount(); page++) {
			for (int item = 0; item < adapter.itemCountInPage(page); item++) {
				View v = adapter.view(page, item);
                v.setTag(adapter.getItemAt(page,item));
				removeView(v); 
				addView(v);
				if(v!=deleteZone){
					views.add(v); 
				}
			}
		}
		deleteZone.bringToFront();
	}

    public void reloadViews() {
        if (adapter == null) {
            return;
        }
        for (int page = 0; page < adapter.pageCount(); page++) {
            for (int item = 0; item < adapter.itemCountInPage(page); item++) {
                if(indexOfItem(page, item) == -1) {
                    View v = adapter.view(page, item);
                    v.setTag(adapter.getItemAt(page,item));
                    addView(v);
                    if(views.indexOf(v) == -1){
                        views.add(v); 
                    }
                }
            }
        }
        deleteZone.bringToFront();
    }

    public int indexOfItem(int page, int index) {
        Object item = adapter.getItemAt(page, index);

        for(int i = 0; i<this.getChildCount(); i++) {
            View v = this.getChildAt(i);
            if(item.equals(v.getTag()))
                return i;
        }
        return -1;
    }

    public void removeItem(int page, int index) {
        Object item = adapter.getItemAt(page, index);
        for(int i = 0; i<this.getChildCount(); i++) {
            View v = (View)this.getChildAt(i);
            if(item.equals(v.getTag())) {
                this.removeView(v);
                return;
            }
        }
    }
	
	private void animateMoveAllItems() {
		if (!isJiggleItems) {
			return;
		}
		
		Animation rotateAnimation = createFastRotateAnimation();

		for (int i=0; i < getItemViewCount(); i++) {
			View child = getChildAt(i);
			child.startAnimation(rotateAnimation);
		 }
	}

	private void cancelAnimations() {
		 for (int i=0; i < getItemViewCount()-2; i++) {
			 View child = getChildAt(i);
			 child.clearAnimation();
		 }
	}

	public boolean onInterceptTouchEvent(MotionEvent event) {
	    return onTouch(null, event);
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int action = event.getAction();
		
		// if multi-touch
		if (event.getPointerCount() > 1)
			return true;
		
		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			if (onTouchListener != null)
				onTouchListener.onTouch(getDraggedView(), event);	
			touchDown(event);
			break;
		case MotionEvent.ACTION_MOVE:
			if (onTouchListener != null)
				onTouchListener.onTouch(getDraggedView(), event);
			touchMove(event);
			break;
		case MotionEvent.ACTION_UP:
			if (onTouchListener != null)
				onTouchListener.onTouch(getDraggedView(), event);
			touchUp(event);
			break;
		}
		if (aViewIsDragged())
			return true;
		return super.onTouchEvent(event);
	}

	private void touchUp(MotionEvent event) {
		touchMove(event);
	    if(!aViewIsDragged()) {
	        if(onClickListener != null) {
                View clickedView = getChildAt(getTargetAtCoor((int) event.getX(), (int) event.getY()));
                if(clickedView != null)
                    onClickListener.onClick(clickedView);
            }
	    } else {
	        cancelAnimations();
	        
    		rearrangePageItems();
    		manageChildrenReordering();
    		hideDeleteView();
    		cancelEdgeTimer();

    		movingView = false;
    		dragged = -1;
    		draggedView = null;
    		lastTarget = -1;
    		container.enableScroll();
    		
	    }
	}
	
	/**
	 * This will rearrange the Items per Page if the Items in the current page
	 * exceeds the view. A new Page will be added if the page where the Item is
	 * dropped is full the other pages next to it is also full.
	 * 
	 * This will synchronize the Page Items based from newPositions. In order
	 * not to lose the reference of the items, this will create an array of
	 * Items per page then arrange the new positions from there.
	 */
	private void rearrangePageItems() {
		List<Page> pages = adapter.getPages();
		int viewInd = 0;
		int maxPageItems = adapter.rowCount() * adapter.columnCount();
		int pageCount = pages.size();
		int prevPagesItemCountOffset = 0;
		ArrayList<Item[]> items = new ArrayList<Item[]>();
		
		for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
			Page page = pages.get(pageIndex);
			List<Item> pageItems = page.getItems();
			int itemCount = pageItems.size();
			
			// if item count exceeds the number of items the page can view
			if (itemCount > maxPageItems) {
				// get the last item of the page
				Item lastItem = page.removeItem(itemCount - 1);
				
				boolean isFinishedMovinItems = false;
				int pageNum = pageIndex;
				while (!isFinishedMovinItems) {
					Page nextPage = null;
					// if last page is also full
					if (pageNum + 1 > pageCount) {
						nextPage = new Page();
						pages.add(nextPage);
						// since new page is added
						pageCount++;
					} else {
						// fail safe: if a page is remove because it does not
						// contain any more items during drag event this will
						// create a new page in case the previous pages are full
						// to contain the dragged item
						if (pageNum + 1 == pages.size()) {
							Page p = new Page();
							pages.add(p);
							// since new page is added
							pageCount++;
						}
						nextPage = pages.get(pageNum + 1);
					}
					// add the item to the first position
					nextPage.addItem(0, lastItem);
					// get the item count of the next page
					int nextPageItemCount = nextPage.getItems().size();
					
					if (nextPageItemCount > maxPageItems) {
						lastItem = nextPage.removeItem(nextPageItemCount - 1);
						pageNum++;
					} else {
						isFinishedMovinItems = true;
					}
				}
				
				// since 1 item is removed
				itemCount--;
			}
			// initialize the item array
			items.add(new Item[itemCount]);
		}
		
		// position the items from the adapter based on the new positions
		for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
			Page page = pages.get(pageIndex);
			List<Item> pageItems = page.getItems();
			int itemCount = pageItems.size();
			
			// arrange the new indices of the items in an array
			for (int i = 0; i < itemCount; i++) {
				// the reference of the item to be transferred
				Item item = page.getItems().get(viewInd - prevPagesItemCountOffset);
				// the new index of the item
				int newItemInd = newPositions.get(viewInd, viewInd);
				// since page items starts at zero, deduct the page item count
				// of the previous pages
				int newPageItemInd = newItemInd - prevPagesItemCountOffset;
				
				// put the item in the array
				if (newPageItemInd >= 0 && newPageItemInd < itemCount) {
					// place item in current page
					items.get(pageIndex)[newPageItemInd] = item;
				} else {
					// place item to previous or the next page depending on the page index
					ItemPosition itemPosition = itemInformationAtPosition(newItemInd);
					items.get(itemPosition.pageIndex)[itemPosition.itemIndex] = item;
				}
				viewInd++;
			}
			
			prevPagesItemCountOffset += itemCount;
		}
		
		// updated the adapter pages
		for (int i = 0; i < pageCount; i++) {
			List<Item> newPageItems = new ArrayList<Item>();
			int itemCount = items.get(i).length;
			for (int j = 0; j < itemCount; j++) {
				newPageItems.add(items.get(i)[j]);
			}
			// set the new page items
			adapter.getPages().get(i).setItems(newPageItems);
		}
	}
	
	private void manageChildrenReordering() {
		boolean draggedDeleted = touchUpInDeleteZoneDrop(lastTouchX, lastTouchY);

		if (draggedDeleted) {
			animateDeleteDragged();
			reorderChildrenWhenDraggedIsDeleted();
		} else {
			reorderChildren();
		}
	}

	private void animateDeleteDragged() {
		ScaleAnimation scale = new ScaleAnimation(1.4f, 0f, 1.4f, 0f, biggestChildWidth / 2 , biggestChildHeight / 2);
		scale.setDuration(200);
		scale.setFillAfter(true);
		scale.setFillEnabled(true);

		getDraggedView().clearAnimation();
		getDraggedView().startAnimation(scale);
	}

	private void reorderChildrenWhenDraggedIsDeleted() {
		int newDraggedPosition = newPositions.get(dragged,dragged);

		List<View> children = cleanUnorderedChildren();
		addReorderedChildrenToParent(children);

		tellAdapterDraggedIsDeleted(newDraggedPosition);
		removeViewAt(newDraggedPosition);
		
		children.remove(newDraggedPosition); 
		views.clear(); 
		views.addAll(children); 

		requestLayout();
		invalidate(); 
	}

	private void tellAdapterDraggedIsDeleted(Integer newDraggedPosition) {
		ItemPosition position = itemInformationAtPosition(newDraggedPosition);
		adapter.deleteItem(position.pageIndex,position.itemIndex);
	}

	private void touchDown(MotionEvent event) {
		initialX = (int)event.getRawX();
		initialY = (int)event.getRawY();

		lastTouchX = (int)event.getRawX() + (currentPage() * gridPageWidth);
		lastTouchY = (int)event.getRawY();
	}

	private void touchMove(MotionEvent event) {
		if (movingView && aViewIsDragged()) {
			lastTouchX = (int) event.getX();
			lastTouchY = (int) event.getY();

			ensureThereIsNoArtifact();
			
			moveDraggedView(lastTouchX, lastTouchY);
			manageSwapPosition(lastTouchX, lastTouchY);
			manageEdgeCoordinates(lastTouchX);
			manageDeleteZoneHover(lastTouchX, lastTouchY);
		}
	}

	private void ensureThereIsNoArtifact() {
        invalidate();
    }

	private void manageDeleteZoneHover(int x, int y) {
		Rect zone = new Rect();
		deleteZone.getHitRect(zone);

		if (zone.intersect(x, y, x+1, y+1)) {
			deleteZone.highlight();
		} else {
			deleteZone.smother();
		}
	}

	private boolean touchUpInDeleteZoneDrop(int x, int y) {
		Rect zone = new Rect();
		deleteZone.getHitRect(zone);

		if (zone.intersect(x, y, x+1, y+1)) {
			deleteZone.smother();
			return true;
		}
		return false;
	}

	private void moveDraggedView(int x, int y) {
		View childAt = getDraggedView();		
		
		int width = childAt.getMeasuredWidth();
		int height = childAt.getMeasuredHeight();

		int l = x - (1 * width / 2);
		int t = y - (1 * height / 2);

		childAt.layout(l, t, l + width, t + height);
	}

	private void manageSwapPosition(int x, int y) {
		int target = getTargetAtCoor(x, y);
		if (childHasMoved(target) && target != lastTarget) {
			/*
			 * target - index of the destination
			 * lastTarget - index of the dragged item
			 */
			animateGap(target);
			lastTarget = target;
		}
	}

	private void manageEdgeCoordinates(int x) {
		final boolean onRightEdge = onRightEdgeOfScreen(x);
		final boolean onLeftEdge = onLeftEdgeOfScreen(x);

		if (canScrollToEitherSide(onRightEdge,onLeftEdge)) {
			if (!wasOnEdgeJustNow) {
				startEdgeDelayTimer(onRightEdge, onLeftEdge);
				wasOnEdgeJustNow = true;
			}
		} else {
			if (wasOnEdgeJustNow) {
				stopAnimateOnTheEdge();
			}
			wasOnEdgeJustNow = false;
			cancelEdgeTimer();
		}
	}

	private void stopAnimateOnTheEdge() {
			View draggedView = getDraggedView();
			draggedView.clearAnimation();
			animateDragged();
	}

	private void cancelEdgeTimer() {

		if (edgeScrollTimer != null) {
			edgeScrollTimer.cancel();
			edgeScrollTimer = null;
		}
	}

	private void startEdgeDelayTimer(final boolean onRightEdge, final boolean onLeftEdge) {
		if (canScrollToEitherSide(onRightEdge, onLeftEdge)) {
			animateOnTheEdge();
			if (edgeScrollTimer == null) {
				edgeScrollTimer = new Timer();
				scheduleScroll(onRightEdge, onLeftEdge);
			}
		}
	}

	private void scheduleScroll(final boolean onRightEdge, final boolean onLeftEdge) {
		edgeScrollTimer.schedule(new TimerTask() {
		    @Override
		    public void run() {
		    	if (wasOnEdgeJustNow) {
		    		wasOnEdgeJustNow = false;
		    		edgeTimerHandler.post(new Runnable() {
						@Override
						public void run() {
							hideDeleteView();
							scroll(onRightEdge, onLeftEdge);
							cancelAnimations();
							animateMoveAllItems();
							animateDragged();
							popDeleteView();
						}
					});
		    	}
		    }
		}, 1000);
	}

	private boolean canScrollToEitherSide(final boolean onRightEdge, final boolean onLeftEdge) {
		return (onLeftEdge && container.canScrollToPreviousPage()) || (onRightEdge && container.canScrollToNextPage());
	}

	private void scroll(boolean onRightEdge, boolean onLeftEdge) {
		cancelEdgeTimer();

		if (onLeftEdge && container.canScrollToPreviousPage()) {
			scrollToPreviousPage();
		} else if (onRightEdge && container.canScrollToNextPage()) {
			scrollToNextPage();
		}
		wasOnEdgeJustNow = false;
	}

	private void scrollToNextPage() {
		tellAdapterToMoveItemToNextPage(dragged);
		moveDraggedToNextPage();

		container.scrollRight();
		int currentPage = currentPage();
		int lastItem = adapter.itemCountInPage(currentPage)-1;
		dragged = positionOfItem(currentPage, lastItem);

		requestLayout();
		
		stopAnimateOnTheEdge();
	}

	private void scrollToPreviousPage() {
		tellAdapterToMoveItemToPreviousPage(dragged);
		moveDraggedToPreviousPage();

		container.scrollLeft();
		int currentPage = currentPage();
		int lastItem = adapter.itemCountInPage(currentPage)-1;
		dragged = positionOfItem(currentPage, lastItem);

		requestLayout();
				
		stopAnimateOnTheEdge();
	}

	private void moveDraggedToPreviousPage() {
		List<View> children = cleanUnorderedChildren();

		List<View> reorderedViews = children;

		int draggedEndPosition = newPositions.get(dragged, dragged);

		View draggedView = reorderedViews.get(draggedEndPosition);
		reorderedViews.remove(draggedEndPosition);

		int indexFirstElementInCurrentPage = findTheIndexOfFirstElementInCurrentPage();

		int indexOfDraggedOnNewPage = indexFirstElementInCurrentPage-1;		
		reorderAndAddViews(reorderedViews, draggedView, indexOfDraggedOnNewPage);
	}

    private int findTheIndexOfFirstElementInCurrentPage() {
        int currentPage = currentPage();
		int indexFirstElementInCurrentPage = 0;
		for (int i=0;i<currentPage;i++) {
			indexFirstElementInCurrentPage += adapter.itemCountInPage(i);
		}
        return indexFirstElementInCurrentPage;
    }

    /**
     * Removes the child views. Does not affect the passed parameter.
     * 
     * @param children
     */
	private void removeItemChildren(List<View> children) {
		for (View child : children) {
			removeView(child);
			views.remove(child); 
		}
	}

	private void moveDraggedToNextPage() {
		List<View> children = cleanUnorderedChildren();

		List<View> reorderedViews = children;
		int draggedEndPosition = newPositions.get(dragged, dragged);

		View draggedView = reorderedViews.get(draggedEndPosition);
		reorderedViews.remove(draggedEndPosition);

		int indexLastElementInNextPage = findTheIndexLastElementInNextPage();

		int indexOfDraggedOnNewPage = indexLastElementInNextPage-1;
		reorderAndAddViews(reorderedViews, draggedView, indexOfDraggedOnNewPage);
	}

    private int findTheIndexLastElementInNextPage() {
        int currentPage = currentPage();
		int indexLastElementInNextPage = 0;
		for (int i=0;i<=currentPage+1;i++) {
			indexLastElementInNextPage += adapter.itemCountInPage(i);
		}
        return indexLastElementInNextPage;
    }

	private void reorderAndAddViews(List<View> reorderedViews, View draggedView, int indexOfDraggedOnNewPage) {

		reorderedViews.add(indexOfDraggedOnNewPage,draggedView);
		newPositions.clear();

		for (View view : reorderedViews) {
			if (view != null) {
				removeView(view); 
				addView(view);

				if(view!=deleteZone){
					views.add(view); 
				}
			}
		}

		deleteZone.bringToFront();
	}

	private boolean onLeftEdgeOfScreen(int x) {
		int currentPage = container.currentPage();

		int leftEdgeXCoor = currentPage*gridPageWidth;
		int distanceFromEdge = x - leftEdgeXCoor;
		return (x > 0 && distanceFromEdge <= EGDE_DETECTION_MARGIN);
	}

	private boolean onRightEdgeOfScreen(int x) {
		int currentPage = container.currentPage();

		int rightEdgeXCoor = (currentPage*gridPageWidth) + gridPageWidth;
		int distanceFromEdge = rightEdgeXCoor - x;
		return (x > (rightEdgeXCoor - EGDE_DETECTION_MARGIN)) && (distanceFromEdge < EGDE_DETECTION_MARGIN);
	}

	private void animateOnTheEdge() {
		View v = getDraggedView();

		ScaleAnimation scale = new ScaleAnimation(.667f, 1.5f, .667f, 1.5f, v.getMeasuredWidth() * 3 / 4, v.getMeasuredHeight() * 3 / 4);
		scale.setDuration(200);
		scale.setRepeatMode(Animation.REVERSE);
		scale.setRepeatCount(Animation.INFINITE);

		v.clearAnimation();
		v.startAnimation(scale);
	}
	
	/**
	 * Get the key of the specified value. If there are the same values, the
	 * first key will be returned. If the key-value pair does not exists yet,
	 * the function will return the value passed.
	 * 
	 * @param positions
	 *            the array of positions
	 * @param value
	 *            the value of the key to be returned
	 * @return the key of the value passed.
	 */
	private int getPositionKeyOfValue(SparseIntArray positions, int value) {
		int posCount = positions.size();
		for (int i = 0; i < posCount; i++) {
			int key = positions.keyAt(i);
			int val = positions.get(key);
			if (val == value) {
				return key;
			}
		}
		return value;
	}
	
	/**
	 * @param target the target index of the dragged item
	 */
	private void animateGap(int target) {
		// get the dragged item's index
		int draggedInd = newPositions.get(dragged, dragged);
		
		// if not moved, exit function
		if (target == draggedInd) {
			return;
		}
		
		// get child count in this view group
		int childCount = getChildCount();
		
		// keep a traversal index of the items
		int itemInd = 0;
		
		SparseIntArray modifiedPositions = new SparseIntArray();
		
		
		for (int childInd = 0; childInd < childCount; childInd++, itemInd++) {
			// skip if child is delete zone
			if (getChildAt(childInd) == deleteZone) {
				// decrement item index since delete zone is not part of the items
				itemInd--;
				continue;
			}
			
			// if the item index is the dragged item then skip
			if (itemInd == draggedInd) {
				continue;
			}
			
			// initialize new position the same as item index
			int newPos = itemInd;
			// the offset (+1) is to refer to the next item, without the offset, this could select the dragged index
			// move the items between the dragged index and the target to the left
			// this moves the dragged index down the list
    		if (draggedInd < target && itemInd >= draggedInd + 1 && itemInd <= target) {
    			newPos--;
    		}
    		// move the items between the target and the dragged index to the right
    		// this moved the dragged index up the list
    		else if (target < draggedInd && itemInd >= target && itemInd < draggedInd) {
    			newPos++;
    		}

    		// initialize the old position of the item
    		int oldPos = modifiedPositions.get(itemInd, itemInd);
    		// if the old and the new position are the same don't do anything
    		if (oldPos == newPos) {
    			continue;
    		}
    		
    		View view = views.get(getPositionKeyOfValue(newPositions, oldPos));
    		
    		// prepare for animation
    		Point oldXY = getCoorForIndex(oldPos);
    		Point newXY = getCoorForIndex(newPos);
    		Point oldOffset = computeTranslationEndDeltaRelativeToRealViewPosition(view, oldXY);
    		Point newOffset = computeTranslationEndDeltaRelativeToRealViewPosition(view, newXY);
    		// animate move
    		animateMoveToNewPosition(view, oldOffset, newOffset);
    		
    		// save the position as modified position
			modifiedPositions.put(itemInd, newPos);
		}
		modifiedPositions.put(draggedInd, target);
		
		// fill the new positions if empty
		if (newPositions.size() == 0) {
			for (int i = 0; i < itemInd; i++) {
				newPositions.put(i, i);
			}
		}
		
		int posCount = modifiedPositions.size();
		// put positions in new positions
		SparseIntArray newPositionValuess = new SparseIntArray();
		for (int i = 0; i < posCount; i++) {
			int k = modifiedPositions.keyAt(i);
			int key = getPositionKeyOfValue(newPositions, k);
			int val = modifiedPositions.get(k);
			
			newPositionValuess.put(key, val);
		}
		
		posCount = newPositionValuess.size();
		for (int i = 0; i < posCount; i++) {
			int key = newPositionValuess.keyAt(i);
			int val = newPositionValuess.get(key);
			newPositions.put(key, val);
		}
	}

	private Point computeTranslationEndDeltaRelativeToRealViewPosition(View view, Point newXY) {
		int offsetX = view.getLeft() - ((columnWidthSize - view.getMeasuredWidth()) / 2);
		int offsetY = view.getTop() - ((rowHeightSize - view.getMeasuredHeight()) / 2);
		return new Point(newXY.x - offsetX, newXY.y - offsetY);
	}

	private void animateMoveToNewPosition(View targetView, Point oldOffset, Point newOffset) {
		Animation translate = createTranslateAnimation(oldOffset, newOffset);
		if (isJiggleItems) {
			
			AnimationSet set = new AnimationSet(true);

			Animation rotate = createFastRotateAnimation();
			
			rotate.setInterpolator(new AccelerateDecelerateInterpolator());
			translate.setInterpolator(new AccelerateDecelerateInterpolator());
			
			set.addAnimation(rotate);
			set.addAnimation(translate);

			targetView.clearAnimation();
			targetView.startAnimation(set);
			
		} else {
			targetView.clearAnimation();
			targetView.startAnimation(translate);
		}
	}

	private TranslateAnimation createTranslateAnimation(Point oldOffset, Point newOffset) {
		TranslateAnimation translate = new TranslateAnimation(Animation.ABSOLUTE, oldOffset.x,
															  Animation.ABSOLUTE, newOffset.x,
															  Animation.ABSOLUTE, oldOffset.y,
															  Animation.ABSOLUTE, newOffset.y);
		translate.setDuration(ANIMATION_DURATION);
		translate.setFillEnabled(true);
		translate.setFillAfter(true);
		return translate;
	}

	private Animation createFastRotateAnimation() {
		Animation rotate = new RotateAnimation(-1.5f,
										  1.5f,
										  Animation.RELATIVE_TO_SELF,
										  0.5f,
										  Animation.RELATIVE_TO_SELF,
										  0.5f);

	 	rotate.setRepeatMode(Animation.REVERSE);
        rotate.setRepeatCount(Animation.INFINITE);
        rotate.setDuration(60);
        rotate.setInterpolator(new AccelerateDecelerateInterpolator());

		return rotate;
	}

	private Point getCoorForIndex(int index) {
		ItemPosition page = itemInformationAtPosition(index);

		int row = page.itemIndex / computedColumnCount;
		int col = page.itemIndex - (row * computedColumnCount);
		
		int targetPage = currentPage();
		if (row == adapter.rowCount()) {
			// if the item goes out of the viewable range, instead of moving it below
			// the last row of items, it should move to the first row of the next page.
			
			// set to the first row
			row = 0;
			// set to the next page
			targetPage += 1;
		}
		int x = (targetPage * gridPageWidth) + (columnWidthSize * col);
		int y = rowHeightSize * row;

		return new Point(x, y);
	}

	private int getTargetAtCoor(int x, int y) {
		int page = currentPage();

		int col = getColumnOfCoordinate(x, page);
		int row = getRowOfCoordinate(y);
		int positionInPage = col + (row * computedColumnCount);

		return positionOfItem(page, positionInPage);
	}

	private int getColumnOfCoordinate(int x, int page) {
		int col = 0;
		int pageLeftBorder = (page) * gridPageWidth;
		for (int i = 1; i <= computedColumnCount; i++) {
			int colRightBorder = (i * columnWidthSize) + pageLeftBorder;
			if (x < colRightBorder) {
				break;
			}
			col++;
		}
		return col;
	}

	private int getRowOfCoordinate(int y) {
		int row = 0;
		for (int i = 1; i <= computedRowCount; i++) {
			if (y < i * rowHeightSize) {
				break;
			}
			row++;
		}
		return row;
	}

	private int currentPage() {
		return container.currentPage();
	}

	private void reorderChildren() {
		// get the list of child view based from newPositions
		List<View> children = cleanUnorderedChildren();
		// reorder the list of child views in children except the delete zone
		addReorderedChildrenToParent(children);
		// clear the list of views (cleared a lot of times)
		views.clear();
		// add all children to the list of views
		views.addAll(children);
		requestLayout();
	}
	
	/**
	 * @return the ordered list of children based from newPositions
	 */
	private List<View> cleanUnorderedChildren() {
		// get the list of child views based from newPositions
		List<View> children = saveChildren();
		// remove each child in children in the views
		removeItemChildren(children);
		return children;
	}

	private void addReorderedChildrenToParent(List<View> children) {
		List<View> reorderedViews = children;
		newPositions.clear();
		views.clear();
		for (View view : reorderedViews) {
			if (view != null) {
				removeView(view);
				addView(view);

				if(view!=deleteZone){
					views.add(view); 
				}
			}
		}

		deleteZone.bringToFront();
	}

	/**
	 * @return list of views ordered by the indices of newPositions
	 */
	private List<View> saveChildren() {
		List<View> children = new ArrayList<View>();
		for (int i = 0; i < getItemViewCount(); i++) {
		    View child;
		    
		    int viewPosition = i; 
		    int index = newPositions.indexOfValue(i); 
		    if(index>=0){ 
		    	viewPosition = newPositions.keyAt(index); 
		    } 
		    child = getChildView(viewPosition); 
			
			child.clearAnimation();
			children.add(child);
		}
		return children;
	}

	private boolean childHasMoved(int position) {
		return position != -1;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);

		Display display = wm.getDefaultDisplay();
		widthSize = acknowledgeWidthSize(widthMode, widthSize, display);
		heightSize = acknowledgeHeightSize(heightMode, heightSize, display);

		adaptChildrenMeasuresToViewSize(widthSize, heightSize);
		searchBiggestChildMeasures();
		computeGridMatrixSize(widthSize, heightSize);
		computeColumnsAndRowsSizes(widthSize, heightSize);

		measureChild(deleteZone, MeasureSpec.makeMeasureSpec(gridPageWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec((int)getPixelFromDip(40), MeasureSpec.EXACTLY));

		setMeasuredDimension(widthSize * adapter.pageCount(), heightSize);
	}

	private float getPixelFromDip(int size) {
		Resources r = getResources();
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, r.getDisplayMetrics());
		return px;
	}

	private void computeColumnsAndRowsSizes(int widthSize, int heightSize) {
		columnWidthSize = widthSize / computedColumnCount;
		rowHeightSize = heightSize / computedRowCount;
	}

	private void computeGridMatrixSize(int widthSize, int heightSize) {
		if (adapter.columnCount() != -1 && adapter.rowCount() != -1) {
			computedColumnCount = adapter.columnCount();
			computedRowCount = adapter.rowCount();
		} else {
			if (biggestChildWidth > 0 && biggestChildHeight > 0) {
				computedColumnCount = widthSize / biggestChildWidth;
				computedRowCount = heightSize / biggestChildHeight;
			}
		}

		if (computedColumnCount == 0) {
			computedColumnCount = 1;
		}

		if (computedRowCount == 0) {
			computedRowCount = 1;
		}
	}
	

	private void searchBiggestChildMeasures() {
		biggestChildWidth = 0;
		biggestChildHeight = 0;
		for (int index = 0; index < getItemViewCount(); index++) {
			View child = getChildAt(index);

			if (biggestChildHeight < child.getMeasuredHeight()) {
				biggestChildHeight = child.getMeasuredHeight();
			}

			if (biggestChildWidth < child.getMeasuredWidth()) {
				biggestChildWidth = child.getMeasuredWidth();
			}
		}
	}

	private int getItemViewCount() {
		return views.size(); 
	}

	private void adaptChildrenMeasuresToViewSize(int widthSize, int heightSize) {
		if (adapter.columnCount() != PagedDragDropGridAdapter.AUTOMATIC && adapter.rowCount() != PagedDragDropGridAdapter.AUTOMATIC) {
			int desiredGridItemWidth = widthSize / adapter.columnCount();
			int desiredGridItemHeight = heightSize / adapter.rowCount();
			measureChildren(MeasureSpec.makeMeasureSpec(desiredGridItemWidth, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(desiredGridItemHeight, MeasureSpec.AT_MOST));
		} else {
			measureChildren(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		}
	}

	private int acknowledgeHeightSize(int heightMode, int heightSize, Display display) {
		if (heightMode == MeasureSpec.UNSPECIFIED) {
			heightSize = display.getHeight();
		}
		gridPageHeight = heightSize;
		return heightSize;
	}

	private int acknowledgeWidthSize(int widthMode, int widthSize, Display display) {
		if (widthMode == MeasureSpec.UNSPECIFIED) {
			widthSize = display.getWidth();
		}
		
        if(adapter.getPageWidth(currentPage()) != 0) {
            widthSize = adapter.getPageWidth(currentPage());
        }
		
		gridPageWidth = widthSize;
		return widthSize;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (adapter.pageCount() == 0)
			return ;
		int pageWidth  = (l + r) / adapter.pageCount();

		for (int page = 0; page < adapter.pageCount(); page++) {
			layoutPage(pageWidth, page);
		}
		
		if (weWereMovingDragged()) {
		    bringDraggedToFront();
		}
	}

    private boolean weWereMovingDragged() {
        return dragged != -1;
    }

	private void layoutPage(int pageWidth, int page) {
		int col = 0;
		int row = 0;
		for (int childIndex = 0; childIndex < adapter.itemCountInPage(page); childIndex++) {
			layoutAChild(pageWidth, page, col, row, childIndex);
			col++;
			if (col == computedColumnCount) {
				col = 0;
				row++;
			}
		}
	}

	private void layoutAChild(int pageWidth, int page, int col, int row, int childIndex) {
		int position = positionOfItem(page, childIndex);

		View child = views.get(position);

		int left = 0;
		int top = 0;
		if (position == dragged && lastTouchOnEdge()) {
			left = computePageEdgeXCoor(child);
			top = lastTouchY - (child.getMeasuredHeight() / 2);
		} else {
			left = (page * pageWidth) + (col * columnWidthSize) + ((columnWidthSize - child.getMeasuredWidth()) / 2);
			top = (row * rowHeightSize) + ((rowHeightSize - child.getMeasuredHeight()) / 2);
		}
		child.layout(left, top, left + child.getMeasuredWidth(), top + child.getMeasuredHeight());
	}

	private boolean lastTouchOnEdge() {
		return onRightEdgeOfScreen(lastTouchX) || onLeftEdgeOfScreen(lastTouchX);
	}

	private int computePageEdgeXCoor(View child) {
		int left;
		left = lastTouchX - (child.getMeasuredWidth() / 2);
		if (onRightEdgeOfScreen(lastTouchX)) {
			left = left - gridPageWidth;
		} else if (onLeftEdgeOfScreen(lastTouchX)) {
			left = left + gridPageWidth;
		}
		return left;
	}

	@Override
	public boolean onLongClick(View v) {	    
	    if(positionForView(initialX, initialY) != -1) {
    		container.disableScroll();
    
    		movingView = true;
    		dragged = positionForView(initialX, initialY);
    		// hold a reference of the dragged view
    		if (draggedView == null) {
    			draggedView = getChildAt(dragged);
    		}
    		
    		bringDraggedToFront();
    
    		animateMoveAllItems();
    
    		animateDragged();
    		
    		popDeleteView();
    		
    		if (onLongClickListener != null)
    			onLongClickListener.onLongClick(getDraggedView());

    		return true;
	    }
	    
	    return false;
	}

	private void bringDraggedToFront() {
	    View draggedView = getChildAt(dragged);
	    draggedView.bringToFront();	    
	    deleteZone.bringToFront();	    	    
    }

    private View getDraggedView() {
        try {
            return views.get(dragged);
        } catch (IndexOutOfBoundsException ex) {
            return getChildAt(getChildCount() - 2);
        }
    }

    private void animateDragged() {
		ScaleAnimation scale = new ScaleAnimation(1f, 1.2f, 1f, 1.2f, biggestChildWidth / 2 , biggestChildHeight / 2);
		scale.setDuration(200);
		scale.setFillAfter(true);
		scale.setFillEnabled(true);

		if (aViewIsDragged()) {
			View draggedView = getDraggedView();
//			Log.e("animateDragged", ((TextView)draggedView.findViewWithTag("text")).getText().toString());
			
            draggedView.clearAnimation();
			draggedView.startAnimation(scale);
		}
	}

	private boolean aViewIsDragged() {
		return weWereMovingDragged();
	}

	private void popDeleteView() {
	    
	    if (adapter.showRemoveDropZone()) {
    		showDeleteView();
	    }
		
	}

    private void showDeleteView() {
        deleteZone.setVisibility(View.VISIBLE);
   
        int l = currentPage() * deleteZone.getMeasuredWidth();
        
        int t = computeDropZoneVerticalLocation();
        int b = computeDropZoneVerticalBottom();
        
        deleteZone.layout(l,  t, l + gridPageWidth, b);
    }
	
	private int computeDropZoneVerticalBottom() {
        int deleteDropZoneLocation = adapter.deleteDropZoneLocation();
        if (deleteDropZoneLocation == PagedDragDropGridAdapter.TOP) {
            return deleteZone.getMeasuredHeight();
        } else {
    		return gridPageHeight - deleteZone.getMeasuredHeight() + gridPageHeight;
        }
    }

    private int computeDropZoneVerticalLocation() {        
        int deleteDropZOneLocation = adapter.deleteDropZoneLocation();
        if (deleteDropZOneLocation == PagedDragDropGridAdapter.TOP) {
            return 0;
        } else {        	
            return gridPageHeight - deleteZone.getMeasuredHeight();
        }
    }

	private void createDeleteZone() {
		deleteZone = new DeleteDropZoneView(getContext());
		addView(deleteZone);
	}

	private void hideDeleteView() {
	    deleteZone.setVisibility(View.INVISIBLE);
	}

	private int positionForView(int initialX, int initzlY) {
		for (int index = 0; index < getItemViewCount(); index++) {
			View child = getChildView(index);
				if (isPointInsideView(initialX, initialY, child)) {
					return index;
				}
		}
		return -1;
	}

    private View getChildView(int index) {
        return views.get(index);
    }

	private boolean isPointInsideView(float x, float y, View view) {
		
		
		int location[] = new int[2];
		view.getLocationOnScreen(location);
		int viewX = location[0];
		int viewY = location[1];

		if (pointIsInsideViewBounds(x, y, view, viewX, viewY)) {
			return true;
		} else {
			return false;
		}
	}

	private boolean pointIsInsideViewBounds(float x, float y, View view, int viewX, int viewY) {
		return (x > viewX && x < (viewX + view.getWidth())) && (y > viewY && y < (viewY + view.getHeight()));
	}

	public void setContainer(PagedDragDropGrid container) {
		this.container = container;
	}

	private int positionOfItem(int pageIndex, int childIndex) {
		int currentGlobalIndex = 0;
		for (int currentPageIndex = 0; currentPageIndex < adapter.pageCount(); currentPageIndex++) {
			int itemCount = adapter.itemCountInPage(currentPageIndex);
			for (int currentItemIndex = 0; currentItemIndex < itemCount; currentItemIndex++) {
				if (pageIndex == currentPageIndex && childIndex == currentItemIndex) {
					return currentGlobalIndex;
				}
				currentGlobalIndex++;
			}
		}
		return -1;
	}

	/**
	 * This function will return the ItemPosition of the item of the viewIndex
	 * that is passed.
	 * 
	 * @param position
	 *            the view index based from the list of indices in newPositions.
	 * @return ItemPosition which will contain the page index and the item index
	 *         in the page.
	 */
	private ItemPosition itemInformationAtPosition(int position) {
		int currentGlobalIndex = 0;
		for (int currentPageIndex = 0; currentPageIndex < adapter.pageCount(); currentPageIndex++) {
			int itemCount = adapter.itemCountInPage(currentPageIndex);
			for (int currentItemIndex = 0; currentItemIndex < itemCount; currentItemIndex++) {
				if (currentGlobalIndex == position) {
					return new ItemPosition(currentPageIndex, currentItemIndex);
				}
				currentGlobalIndex++;
			}
		}
		return null;
	}

	private void tellAdapterToMoveItemToPreviousPage(int itemIndex) {
		ItemPosition itemPosition = itemInformationAtPosition(itemIndex);
		System.out.println("pageIndex = " + itemPosition.pageIndex + "itemIndex = " + itemPosition.itemIndex);
		Item item = (Item) draggedView.getTag();
		adapter.moveItemToPreviousPage(itemPosition.pageIndex, item);
	}

	private void tellAdapterToMoveItemToNextPage(int itemIndex) {
		ItemPosition itemPosition = itemInformationAtPosition(itemIndex);
		System.out.println("pageIndex = " + itemPosition.pageIndex + "itemIndex = " + itemPosition.itemIndex);
		Item item = (Item) draggedView.getTag();
		adapter.moveItemToNextPage(itemPosition.pageIndex, item);
	}

	private class ItemPosition {
		public int pageIndex;
		public int itemIndex;

		public ItemPosition(int pageIndex, int itemIndex) {
			super();
			this.pageIndex = pageIndex;
			this.itemIndex = itemIndex;
		}
	}
}
