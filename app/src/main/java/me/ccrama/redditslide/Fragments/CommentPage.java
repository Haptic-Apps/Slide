package me.ccrama.redditslide.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import net.dean.jraw.models.CommentSort;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import me.ccrama.redditslide.Activities.BaseActivity;
import me.ccrama.redditslide.Adapters.CommentAdapter;
import me.ccrama.redditslide.Adapters.SubmissionComments;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Visuals.Pallete;

public class CommentPage extends Fragment {


    public View v;
    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView rv;

    int page;
    SubmissionComments comments;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.fragment_verticalcontenttoolbar, container, false);

        rv = ((RecyclerView) v.findViewById(R.id.vertical_content));
        final LinearLayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(mLayoutManager);

        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        ((BaseActivity)getActivity()).setSupportActionBar(toolbar);
        ((BaseActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((BaseActivity)getActivity()).getSupportActionBar().setTitle(id);
        toolbar.setBackgroundColor(Pallete.getColor(id));





        final List<String> list=new ArrayList<String>();
        list.add("HOT");
        list.add("NEW");
        list.add("CONTROVERSIAL");
        list.add("TOP");

        final Spinner sp=(Spinner) toolbar.findViewById(R.id.spinner_nav);
        ArrayAdapter<String> adp= new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1,list);
        adp.setDropDownViewResource(R.layout.spinneritem);
        sp.setAdapter(adp);

        final SharedPreferences prefs = getActivity().getSharedPreferences("DEFAULTSORT", 0);
        int chosen =prefs.getInt("last_val", 0);
        sp.setSelection(chosen);

        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0,
                                       View arg1, int pos, long arg3) {


                prefs.edit().putInt("last_val", pos).apply();
                CommentSort sort = null;
                switch (pos) {
                    case 0:
                        //HOT
                        sort = CommentSort.HOT;
                        break;
                    case 1:
                        //RISING
                        sort = CommentSort.NEW;

                        break;
                    case 2:
                        //CONT
                        sort = CommentSort.CONTROVERSIAL;

                        break;
                    case 3:
                        sort = CommentSort.TOP;

                        //QA
                        break;
                }
                mSwipeRefreshLayout.setRefreshing(true);
                comments.setSorting(sort);

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.activity_main_swipe_refresh_layout);
        TypedValue typed_value = new TypedValue();
        getActivity().getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typed_value, true);
        mSwipeRefreshLayout.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(typed_value.resourceId));

        mSwipeRefreshLayout.setColorSchemeColors(Pallete.getColors(id));

        mSwipeRefreshLayout.setRefreshing(true);
        if(context.isEmpty()) {

            comments = new SubmissionComments(fullname, this, mSwipeRefreshLayout);
            adapter = new CommentAdapter(getContext(), comments, rv, DataShare.sharedSubreddit.get(page));
            rv.setAdapter(adapter);

        } else {
            if(context.equals("NOTHING")){
                comments = new SubmissionComments(fullname, this, mSwipeRefreshLayout);

            } else {
                comments = new SubmissionComments(fullname, this, mSwipeRefreshLayout, context);
            }


        }

        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        try {
                            comments.loadMore(adapter, true, id);
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //TODO catch errors
                    }
                }
        );
        return v;
    }

    public void doData(Boolean b) {

        if(adapter == null){
            if(context != null && !context.equals("NOTHING")) {
                adapter = new CommentAdapter(getContext(), comments, rv, comments.submission);
                adapter.currentSelectedItem = context;
            } else {
                adapter = new CommentAdapter(getContext(), comments, rv, comments.submission);

            }
            rv.setAdapter(adapter);
            adapter.reset(getContext(), comments, rv, comments.submission, 1);

        } else
        if(b == false) {
            adapter.reset(getContext(), comments, rv, DataShare.sharedSubreddit.get(page), 1);
        } else {
            adapter.reset(getContext(), comments, rv, DataShare.sharedSubreddit.get(page));

        }


    }

    public CommentAdapter adapter;

    public String fullname;
    public String id;
    public String context;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        id = bundle.getString("subreddit", "");
        fullname = bundle.getString("id", "");
        page = bundle.getInt("page", 0);
        context = bundle.getString("context", "");
    }


}