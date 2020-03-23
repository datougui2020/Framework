package com.library.adapter.widget;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.library.adapter.listener.OnItemMenuClickListener;
import com.library.adapter.touch.DefaultItemTouchHelper;
import com.library.adapter.touch.OnItemMoveListener;
import com.library.adapter.touch.OnItemMovementListener;
import com.library.adapter.touch.OnItemStateChangedListener;
import com.library.adapter.widget.swipemenu.SwipeMenuBridge;
import com.library.adapter.widget.swipemenu.SwipeMenuCreator;
import com.library.adapter.widget.swipemenu.SwipeMenuLayout;
import com.library.adapter_recyclerview.UniversalAdapter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * 通用recyclerview
 *
 * @author YobertJomi
 * className UniversalRecyclerView
 * created at  2019/6/22  23:42
 */
public class UniversalRecyclerView extends RecyclerView {

    /**
     * Left menu.
     */
    public static final int LEFT_DIRECTION = 1;
    /**
     * Right menu.
     */
    public static final int RIGHT_DIRECTION = -1;

    @IntDef({LEFT_DIRECTION, RIGHT_DIRECTION})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Direction {
    }

    /**
     * Invalid position.
     */
    private static final int INVALID_POSITION = -1;

    protected int mScaleTouchSlop;
    protected SwipeMenuLayout mOldSwipedLayout;
    protected int mOldTouchedPosition = INVALID_POSITION;

    private int mDownX;
    private int mDownY;

    private boolean allowSwipeDelete;

    private DefaultItemTouchHelper mItemTouchHelper;

    private SwipeMenuCreator mSwipeMenuCreator;
    private OnItemMenuClickListener mOnItemMenuClickListener;
    private UniversalAdapter.OnItemClickListener mOnItemClickListener;
    private UniversalAdapter.OnItemLongClickListener mOnItemLongClickListener;
    private UniversalAdapter mAdapterWrapper;

    private boolean mSwipeItemMenuEnable = true;
    private List<Integer> mDisableSwipeItemMenuList = new ArrayList<>();

    public UniversalRecyclerView(Context context) {
        this(context, null);
    }

    public UniversalRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UniversalRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mScaleTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    private void initializeItemTouchHelper() {
        if (mItemTouchHelper == null) {
            mItemTouchHelper = new DefaultItemTouchHelper();
            mItemTouchHelper.attachToRecyclerView(this);
        }
    }

    /**
     * Set OnItemMoveListener.
     *
     * @param listener {@link OnItemMoveListener}.
     */
    public void setOnItemMoveListener(OnItemMoveListener listener) {
        initializeItemTouchHelper();
        this.mItemTouchHelper.setOnItemMoveListener(listener);
    }

    /**
     * Set OnItemMovementListener.
     *
     * @param listener {@link OnItemMovementListener}.
     */
    public void setOnItemMovementListener(OnItemMovementListener listener) {
        initializeItemTouchHelper();
        this.mItemTouchHelper.setOnItemMovementListener(listener);
    }

    /**
     * Set OnItemStateChangedListener.
     *
     * @param listener {@link OnItemStateChangedListener}.
     */
    public void setOnItemStateChangedListener(OnItemStateChangedListener listener) {
        initializeItemTouchHelper();
        this.mItemTouchHelper.setOnItemStateChangedListener(listener);
    }

    /**
     * Set the item menu to enable status.
     *
     * @param enabled true means available, otherwise not available; default is true.
     */
    public void setSwipeItemMenuEnabled(boolean enabled) {
        this.mSwipeItemMenuEnable = enabled;
    }

    /**
     * True means available, otherwise not available; default is true.
     */
    public boolean isSwipeItemMenuEnabled() {
        return mSwipeItemMenuEnable;
    }

    /**
     * Set the item menu to enable status.
     *
     * @param position the position of the item.
     * @param enabled  true means available, otherwise not available; default is true.
     */
    public void setSwipeItemMenuEnabled(int position, boolean enabled) {
        if (enabled) {
            if (mDisableSwipeItemMenuList.contains(position)) {
                mDisableSwipeItemMenuList.remove(Integer.valueOf(position));
            }
        } else {
            if (!mDisableSwipeItemMenuList.contains(position)) {
                mDisableSwipeItemMenuList.add(position);
            }
        }
    }

    /**
     * True means available, otherwise not available; default is true.
     *
     * @param position the position of the item.
     */
    public boolean isSwipeItemMenuEnabled(int position) {
        return !mDisableSwipeItemMenuList.contains(position);
    }

    /**
     * Set can long press drag.
     *
     * @param canDrag drag true, otherwise is can't.
     */
    public void setLongPressDragEnabled(boolean canDrag) {
        initializeItemTouchHelper();
        this.mItemTouchHelper.setLongPressDragEnabled(canDrag);
    }

    /**
     * Get can long press drag.
     *
     * @return drag true, otherwise is can't.
     */
    public boolean isLongPressDragEnabled() {
        initializeItemTouchHelper();
        return this.mItemTouchHelper.isLongPressDragEnabled();
    }

    /**
     * Set can swipe delete.
     *
     * @param canSwipe swipe true, otherwise is can't.
     */
    public void setItemViewSwipeEnabled(boolean canSwipe) {
        initializeItemTouchHelper();
        allowSwipeDelete = canSwipe; // swipe and menu conflict.
        this.mItemTouchHelper.setItemViewSwipeEnabled(canSwipe);
    }

    /**
     * Get can long press swipe.
     *
     * @return swipe true, otherwise is can't.
     */
    public boolean isItemViewSwipeEnabled() {
        initializeItemTouchHelper();
        return this.mItemTouchHelper.isItemViewSwipeEnabled();
    }

    /**
     * Start drag a item.
     *
     * @param viewHolder the ViewHolder to start dragging. It must be a direct child of
     *                   RecyclerView.
     */
    public void startDrag(ViewHolder viewHolder) {
        initializeItemTouchHelper();
        this.mItemTouchHelper.startDrag(viewHolder);
    }

    /**
     * Star swipe a item.
     *
     * @param viewHolder the ViewHolder to start swiping. It must be a direct child of RecyclerView.
     */
    public void startSwipe(ViewHolder viewHolder) {
        initializeItemTouchHelper();
        this.mItemTouchHelper.startSwipe(viewHolder);
    }

    /**
     * Check the Adapter and throw an exception if it already exists.
     */
    private void checkAdapterExist(String message) {
        if (mAdapterWrapper != null) throw new IllegalStateException(message);
    }

    /**
     * Set item click listener.
     */
    public void setOnItemClickListener(UniversalAdapter.OnItemClickListener listener) {
        if (listener == null) return;
        checkAdapterExist("Cannot set item click listener, setAdapter has already been called.");
        this.mOnItemClickListener = new ItemClickListener(this, listener);
    }

    private static class ItemClickListener implements UniversalAdapter.OnItemClickListener {

        private UniversalRecyclerView mRecyclerView;
        private UniversalAdapter.OnItemClickListener mListener;

        public ItemClickListener(UniversalRecyclerView recyclerView,
                                 UniversalAdapter.OnItemClickListener listener) {
            this.mRecyclerView = recyclerView;
            this.mListener = listener;
        }

        @Override
        public void onItemClick(View itemView, int position) {
            position -= mRecyclerView.getHeaderCount();
            if (position >= 0) mListener.onItemClick(itemView, position);
        }
    }

    /**
     * Set item click listener.
     */
    public void setOnItemLongClickListener(UniversalAdapter.OnItemLongClickListener listener) {
        if (listener == null) return;
        checkAdapterExist("Cannot set item long click listener, setAdapter has already been " +
                "called.");
        this.mOnItemLongClickListener = new ItemLongClickListener(this, listener);
    }

    private static class ItemLongClickListener implements UniversalAdapter.OnItemLongClickListener {

        private UniversalRecyclerView mRecyclerView;
        private UniversalAdapter.OnItemLongClickListener mListener;

        public ItemLongClickListener(UniversalRecyclerView recyclerView,
                                     UniversalAdapter.OnItemLongClickListener listener) {
            this.mRecyclerView = recyclerView;
            this.mListener = listener;
        }

        @Override
        public void onItemLongClick(View itemView, int position) {
            position -= mRecyclerView.getHeaderCount();
            if (position >= 0) mListener.onItemLongClick(itemView, position);
        }
    }

    /**
     * Set to create menu listener.
     */
    public void setSwipeMenuCreator(SwipeMenuCreator menuCreator) {
        if (menuCreator == null) return;
        checkAdapterExist("Cannot set menu creator, setAdapter has already been called.");
        this.mSwipeMenuCreator = menuCreator;
    }

    /**
     * Set to click menu listener.
     */
    public void setOnItemMenuClickListener(OnItemMenuClickListener listener) {
        if (listener == null) return;
        checkAdapterExist("Cannot set menu item click listener, setAdapter has already been " +
                "called.");
        this.mOnItemMenuClickListener = new ItemMenuClickListener(this, listener);
    }

    private static class ItemMenuClickListener implements OnItemMenuClickListener {

        private UniversalRecyclerView mRecyclerView;
        private OnItemMenuClickListener mListener;

        public ItemMenuClickListener(UniversalRecyclerView recyclerView,
                                     OnItemMenuClickListener listener) {
            this.mRecyclerView = recyclerView;
            this.mListener = listener;
        }

        @Override
        public void onItemClick(SwipeMenuBridge menuBridge, int position) {
            position -= mRecyclerView.getHeaderCount();
            if (position >= 0) {
                mListener.onItemClick(menuBridge, position);
            }
        }
    }

    @Override
    public void setLayoutManager(LayoutManager layoutManager) {
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            final GridLayoutManager.SpanSizeLookup spanSizeLookupHolder =
                    gridLayoutManager.getSpanSizeLookup();

            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (mAdapterWrapper.isHeader(position) || mAdapterWrapper.isFooter(position)) {
                        return gridLayoutManager.getSpanCount();
                    }
                    if (spanSizeLookupHolder != null) {
                        return spanSizeLookupHolder.getSpanSize(position - getHeaderCount());
                    }
                    return 1;
                }
            });
        }
        super.setLayoutManager(layoutManager);
    }

    @Override
    public void setAdapter(Adapter adapter) {
//        if (mAdapterWrapper != null) {
//            mAdapterWrapper.unregisterAdapterDataObserver(mAdapterDataObserver);
//        }

        if (adapter == null) {
            mAdapterWrapper = null;
        } else {
//            adapter.registerAdapterDataObserver(mAdapterDataObserver);
            if (adapter instanceof UniversalAdapter) {
                mAdapterWrapper = ((UniversalAdapter) adapter);
                mAdapterWrapper.setOnItemClickListener(mOnItemClickListener);
                mAdapterWrapper.setOnItemLongClickListener(mOnItemLongClickListener);
            }

            if (mHeaderViewList.size() > 0) {
                for (View view : mHeaderViewList) {
                    mAdapterWrapper.addHeaderView(view);
                }
            }
            if (mFooterViewList.size() > 0) {
                for (View view : mFooterViewList) {
                    mAdapterWrapper.addFooterView(view);
                }
            }
        }
        super.setAdapter(mAdapterWrapper);
    }

//    private AdapterDataObserver mAdapterDataObserver = new AdapterDataObserver() {
//        @Override
//        public void onChanged() {
//            mAdapterWrapper.notifyDataSetChanged();
//        }
//
//        @Override
//        public void onItemRangeChanged(int positionStart, int itemCount) {
//            positionStart += getHeaderCount();
//            mAdapterWrapper.notifyItemRangeChanged(positionStart, itemCount);
//        }
//
//        @Override
//        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
//            positionStart += getHeaderCount();
//            mAdapterWrapper.notifyItemRangeChanged(positionStart, itemCount, payload);
//        }
//
//        @Override
//        public void onItemRangeInserted(int positionStart, int itemCount) {
//            positionStart += getHeaderCount();
//            mAdapterWrapper.notifyItemRangeInserted(positionStart, itemCount);
//        }
//
//        @Override
//        public void onItemRangeRemoved(int positionStart, int itemCount) {
//            positionStart += getHeaderCount();
//            mAdapterWrapper.notifyItemRangeRemoved(positionStart, itemCount);
//        }
//
//        @Override
//        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
//            fromPosition += getHeaderCount();
//            toPosition += getHeaderCount();
//            mAdapterWrapper.notifyItemMoved(fromPosition, toPosition);
//        }
//    };

    private List<View> mHeaderViewList = new ArrayList<>();
    private List<View> mFooterViewList = new ArrayList<>();

    /**
     * Add view at the headers.
     */
    public void addHeaderView(View view) {
        mHeaderViewList.add(view);
        if (mAdapterWrapper != null) {
            mAdapterWrapper.addHeaderViewAfterSetAdapter(view);
        }
    }

    /**
     * Remove view from header.
     */
    public void removeHeaderView(View view) {
        mHeaderViewList.remove(view);
        if (mAdapterWrapper != null) {
            mAdapterWrapper.removeHeaderView(view);
        }
    }

    /**
     * Add view at the footer.
     */
    public void addFooterView(View view) {
        mFooterViewList.add(view);
        if (mAdapterWrapper != null) {
            mAdapterWrapper.addFooterViewAfterSetAdapter(view);
        }
    }

    public void removeFooterView(View view) {
        mFooterViewList.remove(view);
        if (mAdapterWrapper != null) {
            mAdapterWrapper.removeFooterView(view);
        }
    }

    /**
     * Get size of headers.
     */
    public int getHeaderCount() {
        if (mAdapterWrapper == null) return 0;
        return mAdapterWrapper.getHeaderCount();
    }

    /**
     * Get size of footer.
     */
    public int getFooterCount() {
        if (mAdapterWrapper == null) return 0;
        return mAdapterWrapper.getFooterCount();
    }

    /**
     * Get ViewType of item.
     */
    public int getItemViewType(int position) {
        if (mAdapterWrapper == null) return 0;
        return mAdapterWrapper.getItemViewType(position);
    }

    /**
     * open menu on left.
     *
     * @param position position.
     */
    public void smoothOpenLeftMenu(int position) {
        smoothOpenMenu(position, LEFT_DIRECTION, SwipeMenuLayout.DEFAULT_SCROLLER_DURATION);
    }

    /**
     * open menu on left.
     *
     * @param position position.
     * @param duration time millis.
     */
    public void smoothOpenLeftMenu(int position, int duration) {
        smoothOpenMenu(position, LEFT_DIRECTION, duration);
    }

    /**
     * open menu on right.
     *
     * @param position position.
     */
    public void smoothOpenRightMenu(int position) {
        smoothOpenMenu(position, RIGHT_DIRECTION, SwipeMenuLayout.DEFAULT_SCROLLER_DURATION);
    }

    /**
     * open menu on right.
     *
     * @param position position.
     * @param duration time millis.
     */
    public void smoothOpenRightMenu(int position, int duration) {
        smoothOpenMenu(position, RIGHT_DIRECTION, duration);
    }

    /**
     * open menu.
     *
     * @param position  position.
     * @param direction use {@link #LEFT_DIRECTION}, {@link #RIGHT_DIRECTION}.
     * @param duration  time millis.
     */
    public void smoothOpenMenu(int position, @Direction int direction, int duration) {
        if (mOldSwipedLayout != null) {
            if (mOldSwipedLayout.isMenuOpen()) {
                mOldSwipedLayout.smoothCloseMenu();
            }
        }
        position += getHeaderCount();
        ViewHolder vh = findViewHolderForAdapterPosition(position);
        if (vh != null) {
            View itemView = getSwipeMenuView(vh.itemView);
            if (itemView instanceof SwipeMenuLayout) {
                mOldSwipedLayout = (SwipeMenuLayout) itemView;
                if (direction == RIGHT_DIRECTION) {
                    mOldTouchedPosition = position;
                    mOldSwipedLayout.smoothOpenRightMenu(duration);
                } else if (direction == LEFT_DIRECTION) {
                    mOldTouchedPosition = position;
                    mOldSwipedLayout.smoothOpenLeftMenu(duration);
                }
            }
        }
    }

    /**
     * Close menu.
     */
    public void smoothCloseMenu() {
        if (mOldSwipedLayout != null && mOldSwipedLayout.isMenuOpen()) {
            mOldSwipedLayout.smoothCloseMenu();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        boolean isIntercepted = super.onInterceptTouchEvent(e);
        if (allowSwipeDelete || mSwipeMenuCreator == null) {
            return isIntercepted;
        } else {
            if (e.getPointerCount() > 1) return true;
            int action = e.getAction();
            int x = (int) e.getX();
            int y = (int) e.getY();

            int touchPosition = getChildAdapterPosition(findChildViewUnder(x, y));
            ViewHolder touchVH = findViewHolderForAdapterPosition(touchPosition);
            SwipeMenuLayout touchView = null;
            if (touchVH != null) {
                View itemView = getSwipeMenuView(touchVH.itemView);
                if (itemView instanceof SwipeMenuLayout) {
                    touchView = (SwipeMenuLayout) itemView;
                }
            }

            boolean touchMenuEnable =
                    mSwipeItemMenuEnable && !mDisableSwipeItemMenuList.contains(touchPosition);
            if (touchView != null) {
                touchView.setSwipeEnable(touchMenuEnable);
            }
            if (!touchMenuEnable) return isIntercepted;

            switch (action) {
                case MotionEvent.ACTION_DOWN: {
                    mDownX = x;
                    mDownY = y;

                    isIntercepted = false;
                    if (touchPosition != mOldTouchedPosition && mOldSwipedLayout != null &&
                            mOldSwipedLayout.isMenuOpen()) {
                        mOldSwipedLayout.smoothCloseMenu();
                        isIntercepted = true;
                    }

                    if (isIntercepted) {
                        mOldSwipedLayout = null;
                        mOldTouchedPosition = INVALID_POSITION;
                    } else if (touchView != null) {
                        mOldSwipedLayout = touchView;
                        mOldTouchedPosition = touchPosition;
                    }
                    break;
                }
                // They are sensitive to retain sliding and inertia.
                case MotionEvent.ACTION_MOVE: {
                    isIntercepted = handleUnDown(x, y, isIntercepted);
                    if (mOldSwipedLayout == null) break;
                    ViewParent viewParent = getParent();
                    if (viewParent == null) break;

                    int disX = mDownX - x;
                    // 向左滑，显示右侧菜单，或者关闭左侧菜单。
                    boolean showRightCloseLeft = disX > 0 &&
                            (mOldSwipedLayout.hasRightMenu() || mOldSwipedLayout.isLeftCompleteOpen());
                    // 向右滑，显示左侧菜单，或者关闭右侧菜单。
                    boolean showLeftCloseRight = disX < 0 &&
                            (mOldSwipedLayout.hasLeftMenu() || mOldSwipedLayout.isRightCompleteOpen());
                    viewParent.requestDisallowInterceptTouchEvent(showRightCloseLeft || showLeftCloseRight);
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {
                    isIntercepted = handleUnDown(x, y, isIntercepted);
                    break;
                }
            }
        }
        return isIntercepted;
    }

    private boolean handleUnDown(int x, int y, boolean defaultValue) {
        int disX = mDownX - x;
        int disY = mDownY - y;

        // swipe
        if (Math.abs(disX) > mScaleTouchSlop && Math.abs(disX) > Math.abs(disY)) return false;
        // click
        if (Math.abs(disY) < mScaleTouchSlop && Math.abs(disX) < mScaleTouchSlop) return false;
        return defaultValue;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int action = e.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                if (mOldSwipedLayout != null && mOldSwipedLayout.isMenuOpen()) {
                    mOldSwipedLayout.smoothCloseMenu();
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return super.onTouchEvent(e);
    }

    private View getSwipeMenuView(View itemView) {
        if (itemView instanceof SwipeMenuLayout) return itemView;
        List<View> unvisited = new ArrayList<>();
        unvisited.add(itemView);
        while (!unvisited.isEmpty()) {
            View child = unvisited.remove(0);
            if (!(child instanceof ViewGroup)) { // view
                continue;
            }
            if (child instanceof SwipeMenuLayout) return child;
            ViewGroup group = (ViewGroup) child;
            final int childCount = group.getChildCount();
            for (int i = 0; i < childCount; i++) unvisited.add(group.getChildAt(i));
        }
        return itemView;
    }

    private int mScrollState = -1;

    private boolean isLoadMore = false;
    private boolean isAutoLoadMore = true;
    private boolean isLoadError = false;

    private boolean mDataEmpty = true;
    private boolean mHasMore = false;

    private LoadMoreView mLoadMoreView;
    private OnLoadMoreListener mLoadMoreListener;

    @Override
    public void onScrollStateChanged(int state) {
        this.mScrollState = state;
    }

    @Override
    public void onScrolled(int dx, int dy) {
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;

            int itemCount = layoutManager.getItemCount();
            if (itemCount <= 0) return;

            int lastVisiblePosition = linearLayoutManager.findLastVisibleItemPosition();

            if (itemCount == lastVisiblePosition + 1 &&
                    (mScrollState == SCROLL_STATE_DRAGGING || mScrollState == SCROLL_STATE_SETTLING)) {
                dispatchLoadMore();
            }
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager staggeredGridLayoutManager =
                    (StaggeredGridLayoutManager) layoutManager;

            int itemCount = layoutManager.getItemCount();
            if (itemCount <= 0) return;

            int[] lastVisiblePositionArray =
                    staggeredGridLayoutManager.findLastCompletelyVisibleItemPositions(null);
            int lastVisiblePosition = lastVisiblePositionArray[lastVisiblePositionArray.length - 1];

            if (itemCount == lastVisiblePosition + 1 &&
                    (mScrollState == SCROLL_STATE_DRAGGING || mScrollState == SCROLL_STATE_SETTLING)) {
                dispatchLoadMore();
            }
        }
    }

    private void dispatchLoadMore() {
        if (isLoadError) return;

        if (!isAutoLoadMore) {
            if (mLoadMoreView != null) mLoadMoreView.onWaitToLoadMore(mLoadMoreListener);
        } else {
            if (isLoadMore || mDataEmpty || !mHasMore) return;

            isLoadMore = true;

            if (mLoadMoreView != null) mLoadMoreView.onLoading();

            if (mLoadMoreListener != null) mLoadMoreListener.onLoadMore();
        }
    }

    /**
     * Use the default to load more View.
     */
    public void useDefaultLoadMore() {
        DefaultLoadMoreView defaultLoadMoreView = new DefaultLoadMoreView(getContext());
        addFooterView(defaultLoadMoreView);
//        setLoadMoreView(defaultLoadMoreView);
    }

    /**
     * Load more view.
     */
    public void setLoadMoreView(LoadMoreView view) {
        mLoadMoreView = view;
    }

    /**
     * Load more listener.
     */
    public void setLoadMoreListener(OnLoadMoreListener listener) {
        mLoadMoreListener = listener;
    }

    /**
     * Automatically load more automatically.
     * <p>
     * Non-auto-loading mode, you can to click on the item to load.
     * </p>
     *
     * @param autoLoadMore you can use false.
     * @see LoadMoreView#onWaitToLoadMore(OnLoadMoreListener)
     */
    public void setAutoLoadMore(boolean autoLoadMore) {
        isAutoLoadMore = autoLoadMore;
    }

    /**
     * Load more done.
     *
     * @param dataEmpty data is empty ?
     * @param hasMore   has more data ?
     */
    public final void loadMoreFinish(boolean dataEmpty, boolean hasMore) {
        isLoadMore = false;
        isLoadError = false;

        mDataEmpty = dataEmpty;
        mHasMore = hasMore;

        if (mLoadMoreView != null) {
            mLoadMoreView.onLoadFinish(dataEmpty, hasMore);
        }
    }

    /**
     * Called when data is loaded incorrectly.
     *
     * @param errorCode    Error code, will be passed to the LoadView, you can according to it to
     *                    customize the prompt
     *                     information.
     * @param errorMessage Error message.
     */
    public void loadMoreError(int errorCode, String errorMessage) {
        isLoadMore = false;
        isLoadError = true;

        if (mLoadMoreView != null) {
            mLoadMoreView.onLoadError(errorCode, errorMessage);
        }
    }

    public interface LoadMoreView {

        /**
         * Show progress.
         */
        void onLoading();

        /**
         * Load finish, handle result.
         */
        void onLoadFinish(boolean dataEmpty, boolean hasMore);

        /**
         * Non-auto-loading mode, you can to click on the item to load.
         */
        void onWaitToLoadMore(OnLoadMoreListener loadMoreListener);

        /**
         * Load error.
         */
        void onLoadError(int errorCode, String errorMessage);
    }

    public interface OnLoadMoreListener {

        /**
         * More data should be requested.
         */
        void onLoadMore();
    }
}