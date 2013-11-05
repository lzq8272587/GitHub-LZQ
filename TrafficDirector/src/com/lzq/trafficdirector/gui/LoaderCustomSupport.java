/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lzq.trafficdirector.gui;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.IntentCompat;
import android.support.v4.content.Loader;
import android.support.v4.content.pm.ActivityInfoCompat;
import android.support.v4.widget.SearchViewCompat;
import android.support.v4.widget.SearchViewCompat.OnQueryTextListenerCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

import com.lzq.trafficdirector.global.Parameters;

/**
 * Demonstration of the implementation of a custom Loader.
 */
public class LoaderCustomSupport extends SherlockFragmentActivity {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		FragmentManager fm = getSupportFragmentManager();

		// Create the list fragment and add it as our sole content.
		if (fm.findFragmentById(android.R.id.content) == null) {
			AppListFragment list = new AppListFragment();
			fm.beginTransaction().add(android.R.id.content, list).commit();
		}
	}

	/**
	 * This class holds the per-item data in our Loader.
	 */
	public static class AppEntry {
		public AppEntry(AppListLoader loader, ApplicationInfo info) {
			mLoader = loader;
			mInfo = info;
			mApkFile = new File(info.sourceDir);
		}

		public ApplicationInfo getApplicationInfo() {
			return mInfo;
		}

		public String getLabel() {
			return mLabel;
		}

		public Drawable getIcon() {
			if (mIcon == null) {
				if (mApkFile.exists()) {
					mIcon = mInfo.loadIcon(mLoader.mPm);
					return mIcon;
				} else {
					mMounted = false;
				}
			} else if (!mMounted) {
				// If the app wasn't mounted but is now mounted, reload
				// its icon.
				if (mApkFile.exists()) {
					mMounted = true;
					mIcon = mInfo.loadIcon(mLoader.mPm);
					return mIcon;
				}
			} else {
				return mIcon;
			}

			return mLoader.getContext().getResources().getDrawable(android.R.drawable.sym_def_app_icon);
		}

		public String toString() {
			return mLabel;
		}

		void loadLabel(Context context) {
			/**
			 * 写入uid信息，是否被选中wifi或3g
			 */
			uid = mInfo.uid;
			if (Parameters._Wifi_Set.contains(uid))
				selected_wifi = true;
			else
				selected_wifi = false;

			if (Parameters._3G_Set.contains(uid))
				selected_3g = true;
			else
				selected_3g = false;

			if (mLabel == null || !mMounted) {
				if (!mApkFile.exists()) {
					mMounted = false;
					mLabel = mInfo.packageName;
				} else {
					mMounted = true;
					CharSequence label = mInfo.loadLabel(context.getPackageManager());
					mLabel = label != null ? label.toString() : mInfo.packageName;
				}
			}
		}

		private final AppListLoader mLoader;
		private final ApplicationInfo mInfo;
		private final File mApkFile;
		private String mLabel;
		private Drawable mIcon;
		private boolean mMounted;

		int uid;
		boolean selected_wifi;
		boolean selected_3g;
	}

	/**
	 * Perform alphabetical comparison of application entry objects.
	 */
	public static final Comparator<AppEntry> ALPHA_COMPARATOR = new Comparator<AppEntry>() {
		private final Collator sCollator = Collator.getInstance();

		public int compare(AppEntry o1, AppEntry o2) {
			if ((o1.selected_wifi | o1.selected_3g) == (o2.selected_wifi | o2.selected_3g)) {
				return o1.getLabel().compareTo(o2.getLabel());
			}
			if (o1.selected_wifi || o1.selected_3g)
				return -1;
			return 1;
		}
	};

	/**
	 * Helper for determining if the configuration has changed in an interesting
	 * way so we need to rebuild the app list.
	 */
	public static class InterestingConfigChanges {
		final Configuration mLastConfiguration = new Configuration();
		int mLastDensity;

		boolean applyNewConfig(Resources res) {
			int configChanges = mLastConfiguration.updateFrom(res.getConfiguration());
			boolean densityChanged = mLastDensity != res.getDisplayMetrics().densityDpi;
			if (densityChanged
					|| (configChanges & (ActivityInfo.CONFIG_LOCALE | ActivityInfoCompat.CONFIG_UI_MODE | ActivityInfo.CONFIG_SCREEN_LAYOUT)) != 0) {
				mLastDensity = res.getDisplayMetrics().densityDpi;
				return true;
			}
			return false;
		}
	}

	/**
	 * Helper class to look for interesting changes to the installed apps so
	 * that the loader can be updated.
	 */
	public static class PackageIntentReceiver extends BroadcastReceiver {
		final AppListLoader mLoader;

		public PackageIntentReceiver(AppListLoader loader) {
			mLoader = loader;
			IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
			filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
			filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
			filter.addDataScheme("package");
			mLoader.getContext().registerReceiver(this, filter);
			// Register for events related to sdcard installation.
			IntentFilter sdFilter = new IntentFilter();
			sdFilter.addAction(IntentCompat.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
			sdFilter.addAction(IntentCompat.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
			mLoader.getContext().registerReceiver(this, sdFilter);
		}

		public void onReceive(Context context, Intent intent) {
			// Tell the loader about the change.
			mLoader.onContentChanged();
		}
	}

	/**
	 * A custom Loader that loads all of the installed applications.
	 */
	public static class AppListLoader extends AsyncTaskLoader<List<AppEntry>> {
		final InterestingConfigChanges mLastConfig = new InterestingConfigChanges();
		final PackageManager mPm;

		List<AppEntry> mApps;
		PackageIntentReceiver mPackageObserver;

		public AppListLoader(Context context) {
			super(context);

			// Retrieve the package manager for later use; note we don't
			// use 'context' directly but instead the save global application
			// context returned by getContext().
			mPm = getContext().getPackageManager();
		}

		/**
		 * This is where the bulk of our work is done. This function is called
		 * in a background thread and should generate a new set of data to be
		 * published by the loader.
		 */
		public List<AppEntry> loadInBackground() {
			// Retrieve all known applications.
			List<ApplicationInfo> apps = mPm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES
					| PackageManager.GET_DISABLED_COMPONENTS);
			if (apps == null) {
				apps = new ArrayList<ApplicationInfo>();
			}

			final Context context = getContext();

			// Create corresponding array of entries and load their labels.
			List<AppEntry> entries = new ArrayList<AppEntry>(apps.size());
			for (int i = 0; i < apps.size(); i++) {
				AppEntry entry = new AppEntry(this, apps.get(i));
				entry.loadLabel(context);
				entries.add(entry);
			}

			// Sort the list.
			Collections.sort(entries, ALPHA_COMPARATOR);

			// Done!
			return entries;
		}

		/**
		 * Called when there is new data to deliver to the client. The super
		 * class will take care of delivering it; the implementation here just
		 * adds a little more logic.
		 */
		public void deliverResult(List<AppEntry> apps) {
			if (isReset()) {
				// An async query came in while the loader is stopped. We
				// don't need the result.
				if (apps != null) {
					onReleaseResources(apps);
				}
			}
			List<AppEntry> oldApps = apps;
			mApps = apps;

			if (isStarted()) {
				// If the Loader is currently started, we can immediately
				// deliver its results.
				super.deliverResult(apps);
			}

			// At this point we can release the resources associated with
			// 'oldApps' if needed; now that the new result is delivered we
			// know that it is no longer in use.
			if (oldApps != null) {
				onReleaseResources(oldApps);
			}
		}

		/**
		 * Handles a request to start the Loader.
		 */
		protected void onStartLoading() {
			if (mApps != null) {
				// If we currently have a result available, deliver it
				// immediately.
				deliverResult(mApps);
			}

			// Start watching for changes in the app data.
			if (mPackageObserver == null) {
				mPackageObserver = new PackageIntentReceiver(this);
			}

			// Has something interesting in the configuration changed since we
			// last built the app list?
			boolean configChange = mLastConfig.applyNewConfig(getContext().getResources());

			if (takeContentChanged() || mApps == null || configChange) {
				// If the data has changed since the last time it was loaded
				// or is not currently available, start a load.
				forceLoad();
			}
		}

		/**
		 * Handles a request to stop the Loader.
		 */
		protected void onStopLoading() {
			// Attempt to cancel the current load task if possible.
			cancelLoad();
		}

		/**
		 * Handles a request to cancel a load.
		 */
		public void onCanceled(List<AppEntry> apps) {
			super.onCanceled(apps);

			// At this point we can release the resources associated with 'apps'
			// if needed.
			onReleaseResources(apps);
		}

		/**
		 * Handles a request to completely reset the Loader.
		 */
		protected void onReset() {
			super.onReset();

			// Ensure the loader is stopped
			onStopLoading();

			// At this point we can release the resources associated with 'apps'
			// if needed.
			if (mApps != null) {
				onReleaseResources(mApps);
				mApps = null;
			}

			// Stop monitoring for changes.
			if (mPackageObserver != null) {
				getContext().unregisterReceiver(mPackageObserver);
				mPackageObserver = null;
			}
		}

		/**
		 * Helper function to take care of releasing resources associated with
		 * an actively loaded data set.
		 */
		protected void onReleaseResources(List<AppEntry> apps) {
			// For a simple List<> there is nothing to do. For something
			// like a Cursor, we would close it here.
		}
	}

	public static class AppListAdapter extends ArrayAdapter<AppEntry> {
		private final LayoutInflater mInflater;

		public AppListAdapter(Context context) {
			super(context, android.R.layout.simple_list_item_2);
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public void setData(List<AppEntry> data) {
			clear();
			if (data != null) {
				for (AppEntry appEntry : data) {
					/**
					 * 如果是系统程序（uid=1000）则不添加进去
					 */
					if (appEntry.uid != 1000)
						add(appEntry);
				}
			}
		}

		/**
		 * Populate new items in the list.
		 */
		public View getView(int position, View convertView, ViewGroup parent) {
			ListEntry entry;
			if (convertView == null) {
				// Inflate a new view
				convertView = mInflater.inflate(R.layout.list_item_icon_text, parent, false);
				entry = new ListEntry();
				entry.cb = (CheckBox) convertView.findViewById(R.id.app_checkBox);
				entry.iv = (ImageView) convertView.findViewById(R.id.app_icon);
				entry.tv = (TextView) convertView.findViewById(R.id.app_label);
				convertView.setTag(entry);
				entry.cb.setOnCheckedChangeListener(new CheckBoxListener());

			} else {
				// Convert an existing view
				entry = (ListEntry) convertView.getTag();
			}

			final AppEntry app = getItem(position);
			entry.iv.setImageDrawable(app.getIcon());
			entry.tv.setText(app.getLabel());
			final CheckBox cbox_ = entry.cb;
			cbox_.setTag(app);
			cbox_.setChecked(app.selected_wifi);
			return convertView;

		}

		/**
		 * 这个类用来保存List中每个条目的状态，以保证滑动List的过程中，触发的CheckableChanege事件导致勾上 的选项又被取消了。
		 * 
		 * @author LZQ
		 * 
		 */
		class ListEntry {
			ImageView iv;
			TextView tv;
			CheckBox cb;

		}

		class CheckBoxListener implements OnCheckedChangeListener {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				final AppEntry app = (AppEntry) buttonView.getTag();
				if (app != null) {
					switch (buttonView.getId()) {
					case R.id.app_checkBox: {

						app.selected_wifi = isChecked;
						app.selected_3g = isChecked;

						if (isChecked) {
							/**
							 * 有新的应用被选上了，那么要更新被选中的集合
							 */
							System.out.println(app.getLabel() + " checked. " + app.uid);
							Parameters._3G_Set.add(app.uid);
							Parameters._Wifi_Set.add(app.uid);

						} else {
							/**
							 * 有应用被取消了，更新被取消的应用程序
							 */
							System.out.println(app.getLabel() + " canceled.");
							Parameters._3G_Set.remove(app.uid);
							Parameters._Wifi_Set.remove(app.uid);
						}
						break;

					}
					// case R.id.itemcheck_3g: app.selected_3g = isChecked;
					// break;
					}
				}
			}

		}
	}

	public static class AppListFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<List<AppEntry>> {

		// This is the Adapter being used to display the list's data.
		AppListAdapter mAdapter;

		// If non-null, this is the current filter the user has provided.
		String mCurFilter;

		OnQueryTextListenerCompat mOnQueryTextListenerCompat;

		Handler handler = new Handler();

		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);

			// Give some text to display if there is no data. In a real
			// application this would come from a resource.
			setEmptyText("No applications");

			// We have a menu item to show in action bar.
			setHasOptionsMenu(true);

			// Create an empty adapter we will use to display the loaded data.
			mAdapter = new AppListAdapter(getActivity());
			setListAdapter(mAdapter);

			// Start out with a progress indicator.
			setListShown(false);

			// Prepare the loader. Either re-connect with an existing one,
			// or start a new one.
			getLoaderManager().initLoader(0, null, this);
		}

		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
			// Place an action bar item for searching.
			MenuItem item = menu.add("Search");
			item.setIcon(android.R.drawable.ic_menu_search);
			item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			View searchView = SearchViewCompat.newSearchView(getActivity());
			if (searchView != null) {
				SearchViewCompat.setOnQueryTextListener(searchView, new OnQueryTextListenerCompat() {

					public boolean onQueryTextChange(String newText) {
						// Called when the action bar search text has
						// changed. Since this
						// is a simple array adapter, we can just have
						// it do the filtering.
						mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
						mAdapter.getFilter().filter(mCurFilter);
						return true;
					}
				});
				item.setActionView(searchView);
			}

			MenuItem refresh = menu.add("Search");
			refresh.setIcon(R.drawable.ic_menu_refresh_holo_light);
			refresh.setOnMenuItemClickListener(new OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					// TODO Auto-generated method stub
					// Create an empty adapter we will use to display the loaded
					// data.
					// Create an empty adapter we will use to display the loaded
					// data.
					mAdapter = new AppListAdapter(getActivity());
					setListAdapter(mAdapter);
					// Start out with a progress indicator.
					setListShown(false);
					// Prepare the loader. Either re-connect with an existing
					// one,
					// or start a new one.
					AppListFragment.this.getLoaderManager().restartLoader(0, null, AppListFragment.this);
					return false;
				}

			});
			refresh.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		}

		public void onListItemClick(ListView l, View v, int position, long id) {
			// Insert desired behavior here.
			Log.i("LoaderCustom", "Item clicked: " + id);
			/**
			 * 在此处添加每个List选项被选中时的操作
			 */
			// Intent intent=new Intent();
			// Bundle bundle=new Bundle();
			//
			// intent.putExtra("ICON",
			// ((BitmapDrawable)(((ImageView)v.findViewById(R.id.icon)).getDrawable())).getBitmap());
			// intent.putExtra("LABEL",
			// ((TextView)v.findViewById(R.id.text)).getText());
			//
			// // intent.putExtra("PACKAGE", 0);
			// intent.setClass(getActivity(),
			// PreferenceConfigureActivity.class);
			// startActivity(intent);
		}

		public Loader<List<AppEntry>> onCreateLoader(int id, Bundle args) {
			// This is called when a new Loader needs to be created. This
			// sample only has one Loader with no arguments, so it is simple.
			return new AppListLoader(getActivity());
		}

		public void onLoadFinished(Loader<List<AppEntry>> loader, List<AppEntry> data) {
			// Set the new data in the adapter.
			mAdapter.setData(data);

			// The list should now be shown.
			if (isResumed()) {
				setListShown(true);
			} else {
				setListShownNoAnimation(true);
			}
		}

		public void onLoaderReset(Loader<List<AppEntry>> loader) {
			// Clear the data in the adapter.
			mAdapter.setData(null);
		}

	}

}
