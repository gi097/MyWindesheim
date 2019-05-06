/**
 * Copyright (c) 2019 Giovanni Terlingen
 * <p/>
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 **/
package com.giovanniterlingen.windesheim.view.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteConstraintException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.giovanniterlingen.windesheim.ApplicationLoader;
import com.giovanniterlingen.windesheim.R;
import com.giovanniterlingen.windesheim.controllers.ColorController;
import com.giovanniterlingen.windesheim.controllers.DatabaseController;
import com.giovanniterlingen.windesheim.controllers.NotificationController;
import com.giovanniterlingen.windesheim.controllers.WebUntisController;
import com.giovanniterlingen.windesheim.models.ScheduleItem;
import com.giovanniterlingen.windesheim.view.Adapters.ChooseScheduleAdapter;
import com.giovanniterlingen.windesheim.view.ScheduleActivity;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * A schedule app for students and teachers of Windesheim
 *
 * @author Giovanni Terlingen
 */
public class ChooseScheduleFragment extends Fragment {

    private ChooseScheduleAdapter adapter;
    private RecyclerView recyclerView;
    private int type;
    private Context context;
    private ProgressBar spinner;
    private View view;
    private boolean isViewShown = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        if (this.getArguments() == null) {
            return;
        }
        int position = this.getArguments().getInt("position");
        type = position + 1;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getView() != null) {
            isViewShown = true;
            startTask();
            return;
        }
        isViewShown = false;
    }

    private void startTask() {
        adapter = null;
        if (recyclerView != null) {
            recyclerView.setAdapter(null);
        }
        ComponentFetcher componentFetcher = new ComponentFetcher(ChooseScheduleFragment.this);
        componentFetcher.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_choose_schedule, container, false);
        TextView chooseTextview = view.findViewById(R.id.choose_textview);
        TextView descriptionTextview = view.findViewById(R.id.description_textview);
        EditText dataSearch = view.findViewById(R.id.filter_edittext);
        if (type == 1) {
            chooseTextview.setText(getResources().getString(R.string.choose_class));
            descriptionTextview.setText(getResources().getString(R.string
                    .choose_class_description));
            dataSearch.setHint(getResources().getString(R.string.choose_class_hint));
        } else if (type == 2) {
            chooseTextview.setText(getResources().getString(R.string.choose_teacher));
            descriptionTextview.setText(getResources().getString(R.string
                    .choose_teacher_description));
            dataSearch.setHint(getResources().getString(R.string.choose_teacher_hint));
        } else if (type == 3) {
            chooseTextview.setText(getResources().getString(R.string.choose_subject));
            descriptionTextview.setText(getResources().getString(R.string
                    .choose_subject_description));
            dataSearch.setHint(getResources().getString(R.string.choose_subject_hint));
        }
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        spinner = view.findViewById(R.id.progress_bar);
        dataSearch.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                if (adapter != null) {
                    adapter.filter(arg0.toString());
                }
            }

            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            public void afterTextChanged(Editable s) {
            }
        });
        if (!isViewShown) {
            startTask();
        }
        return view;
    }

    private synchronized ArrayList<ScheduleItem> buildClassArray(JSONArray jsonArray) {
        ArrayList<ScheduleItem> scheduleItems = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                if (Thread.interrupted()) {
                    return null;
                }
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                scheduleItems.add(new ScheduleItem(jsonObject.getInt("id"),
                        jsonObject.getString("name") + " - " + jsonObject.getString("longName")));
            } catch (JSONException e) {
                alertConnectionProblem();
                return null;
            }
        }
        return scheduleItems;
    }

    private void alertConnectionProblem() {
        if (!getUserVisibleHint()) {
            return;
        }
        ApplicationLoader.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(context)
                        .setTitle(getResources().getString(R.string.alert_connection_title))
                        .setMessage(getResources().getString(R.string.alert_connection_description))
                        .setPositiveButton(getResources().getString(R.string.connect),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        startTask();
                                        dialog.cancel();
                                    }
                                })
                        .setNegativeButton(getResources().getString(R.string.cancel), new
                                DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                }).show();
            }
        });
    }

    private void alertScheduleExists() {
        new AlertDialog.Builder(context)
                .setTitle(getResources().getString(R.string.duplicate_title))
                .setMessage(getResources().getString(R.string.duplicate_description))
                .setNegativeButton(getResources().getString(R.string.cancel), new
                        DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }).show();
    }

    private static class ComponentFetcher extends AsyncTask<Void, Void, Void> {

        private final WeakReference<ChooseScheduleFragment> weakReference;

        ComponentFetcher(ChooseScheduleFragment fragmentActivity) {
            weakReference = new WeakReference<>(fragmentActivity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            ChooseScheduleFragment fragment = weakReference.get();
            if (fragment == null) {
                return;
            }

            fragment.spinner.setVisibility(View.VISIBLE);
            fragment.adapter = null;
        }

        @Override
        protected Void doInBackground(Void... params) {
            final ChooseScheduleFragment fragment = weakReference.get();
            if (fragment == null) {
                return null;
            }

            try {
                ArrayList<ScheduleItem> scheduleItems = fragment.buildClassArray(
                        new WebUntisController().getListFromServer(fragment.type)
                                .getJSONArray("elements"));
                fragment.adapter = new ChooseScheduleAdapter(fragment.context, scheduleItems) {
                    @Override
                    protected void onContentClick(int id, String name) {
                        try {
                            boolean hasSchedules = DatabaseController.getInstance().hasSchedules();

                            DatabaseController.getInstance().addSchedule(id, name, fragment.type);
                            DatabaseController.getInstance().clearFetched();

                            ColorController.invalidateColorCache();

                            SharedPreferences preferences = PreferenceManager
                                    .getDefaultSharedPreferences(fragment.context);
                            SharedPreferences.Editor editor = preferences.edit();
                            if (!hasSchedules) {
                                editor.putInt("notifications_type",
                                        NotificationController.NOTIFICATION_ALWAYS_ON);
                            }
                            editor.apply();

                            ApplicationLoader.postInitApplication();

                            if (!hasSchedules) {
                                Intent intent = new Intent(fragment.context,
                                        ScheduleActivity.class);
                                fragment.startActivity(intent);
                            }
                            fragment.getActivity().finish();
                        } catch (SQLiteConstraintException e) {
                            fragment.alertScheduleExists();
                        }
                    }
                };
            } catch (InterruptedException e) {
                //
            } catch (Exception e) {
                fragment.alertConnectionProblem();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            super.onPostExecute(param);

            ChooseScheduleFragment fragment = weakReference.get();
            if (fragment == null) {
                return;
            }

            fragment.spinner.setVisibility(View.GONE);
            fragment.recyclerView.setAdapter(fragment.adapter);
            EditText dataSearch = fragment.view.findViewById(R.id.filter_edittext);
            if (fragment.adapter != null && dataSearch.getText() != null &&
                    dataSearch.getText().toString().length() > 0) {
                fragment.adapter.filter(dataSearch.getText().toString());
            }
        }
    }
}
