package eu.davidea.samples.flexibleadapter.fragments;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AdapterView;

import eu.davidea.fastscroller.FastScroller;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.SelectableAdapter;
import eu.davidea.flexibleadapter.common.DividerItemDecoration;
import eu.davidea.flipview.FlipView;
import eu.davidea.samples.flexibleadapter.ExampleAdapter;
import eu.davidea.samples.flexibleadapter.MainActivity;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.samples.flexibleadapter.services.DatabaseService;
import eu.davidea.utils.Utils;

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class FragmentSelectionModes extends AbstractFragment
	implements FlexibleAdapter.OnItemClickListener, FlexibleAdapter.OnItemLongClickListener {

	public static final String TAG = FragmentSelectionModes.class.getSimpleName();

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	/**
	 * The current activated item position.
	 */
	private int mActivatedPosition = RecyclerView.NO_POSITION;

	/**
	 * Custom implementation of FlexibleAdapter
	 */
	private ExampleAdapter mAdapter;


	@SuppressWarnings("unused")
	public static FragmentSelectionModes newInstance(int columnCount) {
		FragmentSelectionModes fragment = new FragmentSelectionModes();
		Bundle args = new Bundle();
		args.putInt(ARG_COLUMN_COUNT, columnCount);
		fragment.setArguments(args);
		return fragment;
	}

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public FragmentSelectionModes() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			//Previously serialized activated item position
			if (savedInstanceState.containsKey(STATE_ACTIVATED_POSITION))
				setSelection(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//Settings for FlipView
		FlipView.resetLayoutAnimationDelay(true, 1000L);

		//Create New Database and Initialize RecyclerView
		DatabaseService.getInstance().createEndlessDatabase(200);
		initializeRecyclerView(savedInstanceState);

		//Settings for FlipView
		FlipView.stopLayoutAnimation();
	}

	@SuppressWarnings({"ConstantConditions", "NullableProblems"})
	private void initializeRecyclerView(Bundle savedInstanceState) {
		//Initialize Adapter and RecyclerView
		//ExampleAdapter makes use of stableIds, I strongly suggest to implement 'item.hashCode()'
		mAdapter = new ExampleAdapter(DatabaseService.getInstance().getDatabaseList(), getActivity());
		mAdapter.setMode(SelectableAdapter.MODE_SINGLE);

		//Experimenting NEW features (v5.0.0)
		mRecyclerView = (RecyclerView) getView().findViewById(R.id.recycler_view);
		mRecyclerView.setLayoutManager(createNewLinearLayoutManager());
		mRecyclerView.setAdapter(mAdapter);
		mRecyclerView.setHasFixedSize(true); //Size of RV will not change
		//NOTE: Use default item animator 'canReuseUpdatedViewHolder()' will return true if
		// a Payload is provided. FlexibleAdapter is actually sending Payloads onItemChange.
		mRecyclerView.setItemAnimator(new DefaultItemAnimator());
		//Divider item decorator with DrawOver enabled
		mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), R.drawable.divider)
				.withDrawOver(true));
		mRecyclerView.postDelayed(new Runnable() {
			@Override
			public void run() {
				Snackbar.make(getView(), "Selection MODE_SINGLE is enabled", Snackbar.LENGTH_SHORT).show();
			}
		}, 1500L);

		//Add FastScroll to the RecyclerView, after the Adapter has been attached the RecyclerView!!!
		mAdapter.setFastScroller((FastScroller) getActivity().findViewById(R.id.fast_scroller),
				Utils.getColorAccent(getActivity()), (MainActivity) getActivity());

		SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout);
		swipeRefreshLayout.setEnabled(true);
		mListener.onFragmentChange(swipeRefreshLayout, mRecyclerView, SelectableAdapter.MODE_SINGLE);

		//Add sample HeaderView items on the top (not belongs to the library)
		mAdapter.addUserLearnedSelection(savedInstanceState == null);
		mAdapter.showLayoutInfo(savedInstanceState == null);
	}

	@Override
	public void showNewLayoutInfo(MenuItem item) {
		super.showNewLayoutInfo(item);
		mAdapter.showLayoutInfo(true);
	}

	//TODO: Should include setActivatedPosition in the library?
	public void setSelection(final int position) {
		if (mAdapter.getMode() == FlexibleAdapter.MODE_SINGLE) {
			Log.v(TAG, "setSelection called!");
			setActivatedPosition(position);
			mRecyclerView.postDelayed(new Runnable() {
				@Override
				public void run() {
					mRecyclerView.smoothScrollToPosition(position);
				}
			}, 1000L);
		}
	}

	private void setActivatedPosition(int position) {
		Log.d(TAG, "ItemList New mActivatedPosition=" + position);
		mActivatedPosition = position;
	}

	/**
	 * Called when single tap occurs.
	 *
	 * @param position the adapter position of the item clicked
	 * @return true if the click should activate the ItemView, false for no change.
	 */
	@Override
	public boolean onItemClick(int position) {
		if (position != mActivatedPosition)
			setActivatedPosition(position);
		return true;
	}

	/**
	 * Called when long tap occurs.
	 *
	 * @param position the adapter position of the item clicked
	 */
	@Override
	public void onItemLongClick(int position) {
		//TODO: Handling ActionMode
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (mActivatedPosition != AdapterView.INVALID_POSITION) {
			//Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
			Log.d(TAG, STATE_ACTIVATED_POSITION + "=" + mActivatedPosition);
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_selection_modes, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_list_type)
			mAdapter.setAnimationOnScrolling(true);
		return super.onOptionsItemSelected(item);
	}

}