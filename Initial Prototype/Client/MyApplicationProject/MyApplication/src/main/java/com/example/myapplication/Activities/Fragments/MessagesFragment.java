package com.example.myapplication.activities.fragments;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.Loader;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;

import com.example.myapplication.R;

/**
 * Created by Michal on 04/01/14.
 */
public class MessagesFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private OnFragmentInteractionListener mListener;
    private SimpleCursorAdapter adapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new SimpleCursorAdapter(getActivity().getApplicationContext(),
                R.layout.message_fragment,
                new Cursor() {
                    @Override
                    public int getCount() {
                        return 0;
                    }

                    @Override
                    public int getPosition() {
                        return 0;
                    }

                    @Override
                    public boolean move(int i) {
                        return false;
                    }

                    @Override
                    public boolean moveToPosition(int i) {
                        return false;
                    }

                    @Override
                    public boolean moveToFirst() {
                        return false;
                    }

                    @Override
                    public boolean moveToLast() {
                        return false;
                    }

                    @Override
                    public boolean moveToNext() {
                        return false;
                    }

                    @Override
                    public boolean moveToPrevious() {
                        return false;
                    }

                    @Override
                    public boolean isFirst() {
                        return false;
                    }

                    @Override
                    public boolean isLast() {
                        return false;
                    }

                    @Override
                    public boolean isBeforeFirst() {
                        return false;
                    }

                    @Override
                    public boolean isAfterLast() {
                        return false;
                    }

                    @Override
                    public int getColumnIndex(String s) {
                        return 0;
                    }

                    @Override
                    public int getColumnIndexOrThrow(String s) throws IllegalArgumentException {
                        return 0;
                    }

                    @Override
                    public String getColumnName(int i) {
                        return null;
                    }

                    @Override
                    public String[] getColumnNames() {
                        return new String[0];
                    }

                    @Override
                    public int getColumnCount() {
                        return 0;
                    }

                    @Override
                    public byte[] getBlob(int i) {
                        return new byte[0];
                    }

                    @Override
                    public String getString(int i) {
                        return null;
                    }

                    @Override
                    public void copyStringToBuffer(int i, CharArrayBuffer charArrayBuffer) {

                    }

                    @Override
                    public short getShort(int i) {
                        return 0;
                    }

                    @Override
                    public int getInt(int i) {
                        return 0;
                    }

                    @Override
                    public long getLong(int i) {
                        return 0;
                    }

                    @Override
                    public float getFloat(int i) {
                        return 0;
                    }

                    @Override
                    public double getDouble(int i) {
                        return 0;
                    }

                    @Override
                    public int getType(int i) {
                        return 0;
                    }

                    @Override
                    public boolean isNull(int i) {
                        return false;
                    }

                    @Override
                    public void deactivate() {

                    }

                    @Override
                    public boolean requery() {
                        return false;
                    }

                    @Override
                    public void close() {

                    }

                    @Override
                    public boolean isClosed() {
                        return false;
                    }

                    @Override
                    public void registerContentObserver(ContentObserver contentObserver) {

                    }

                    @Override
                    public void unregisterContentObserver(ContentObserver contentObserver) {

                    }

                    @Override
                    public void registerDataSetObserver(DataSetObserver dataSetObserver) {

                    }

                    @Override
                    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

                    }

                    @Override
                    public void setNotificationUri(ContentResolver contentResolver, Uri uri) {

                    }

                    @Override
                    public boolean getWantsAllOnMoveCalls() {
                        return false;
                    }

                    @Override
                    public Bundle getExtras() {
                        return null;
                    }

                    @Override
                    public Bundle respond(Bundle bundle) {
                        return null;
                    }
                },
                new String[]{"message body", "date & time"},
                new int[]{R.id.MessageFragmentMessageBodyTextView, R.id.MessageFragmentDateTimeTextView},
                0);

        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                switch(view.getId()) {
                    case R.id.MessageFragmentMessageBodyTextView:
                        LinearLayout root = (LinearLayout) view.getParent().getParent();
                        if (cursor.getString(1) == null) {
                            root.setGravity(Gravity.RIGHT);
                            root.setPadding(50, 10, 10, 10);
                        } else {
                            root.setGravity(Gravity.LEFT);
                            root.setPadding(10, 10, 50, 10);
                        }
                        break;
                }
                return false;
            }
        });

        setListAdapter(adapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = new Bundle();
        //args.putString(DataProvider.COL_EMAIL, mListener.getProfileEmail());
        getLoaderManager().initLoader(0, args, this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    public interface OnFragmentInteractionListener {
        public String getProfileEmail();
    }
}
