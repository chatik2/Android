package com.shifu.user.truechat;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;

public class ListFragment extends Fragment {

    private final String CLASS_TAG = ListFragment.class.getSimpleName();

    //TODO to clear after test
    Integer i = 0;


    // Init layout elements
    private NestedScrollView scrollView;
    private RecyclerView recyclerView;

    // Init search variables
    private boolean isLoading = false;


    // Init Rx variables & functions
    private static CompositeDisposable disposables = new CompositeDisposable();
    private static Disposable update = null;

    private static Disposable getUsers = null;
    public static PublishProcessor<Long> publishProcessorUsers = PublishProcessor.create();

    private static Disposable getMsgs = null;
    public static PublishProcessor<Long> publishProcessorMsgs = PublishProcessor.create();


    // Init program variables
    private static RealmController rc = null;
    private static RealmRVAdapter ra = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_fragment, container, false);

        if (rc == null) rc = RealmController.getInstance();
        if (ra == null) ra = RealmRVAdapter.getInstance();

        // Init RecycleView
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setNestedScrollingEnabled(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);
        recyclerView.setAdapter(ra);

        // Init other Views
        final EditText msgText = view.findViewById(R.id.msg_text);
        msgText.requestFocus();

        scrollView = view.findViewById(R.id.nested_scroll_view);

        update = RxBus.getInstance()
                .getMessages(Event.UPDATE)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    Log.d(Event.UPDATE.toString(), "Received: "+RealmRVAdapter.getInstance().getData());
                    setAdapter();
                });
        disposables.add(update);

//        getUsers =  publishProcessorUsers
//                .onBackpressureDrop()
//                .concatMap((Function <Long, Publisher <Response <List <JsonUser>>>>) s -> RestController.init().getUsers(s))
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .repeatWhen(observable -> observable.timeout(60, TimeUnit.SECONDS))
//                .subscribe(rc::addUsers, t->RestController.handleError(t, "getUsers"));
//
//        getMsgs =  publishProcessorMsgs
//                .onBackpressureDrop()
//                .concatMap((Function <Long, Publisher <Response <List <JsonMsg>>>>) s -> RestController.init().getMsgs(s))
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(rc::addMsgs, t->RestController.handleError(t, "getMsgs"));

//        getUsers =  publishProcessorUsers
//                .onBackpressureDrop()
//                .concatMap((Function <Long, Publisher <Response <List <JsonUser>>>>) s -> FakeRestController.getUsers(getResources().openRawResource(R.raw.fake_users), s))
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
////                .repeatWhen(observable -> observable.timeout(60, TimeUnit.SECONDS))
//                .subscribe(rc::addUsers, t->FakeRestController.handleError(t, "getUsers"));
//
//        getMsgs =  publishProcessorMsgs
//                .onBackpressureDrop()
//                .concatMap((Function <Long, Publisher <Response <List <JsonMsg>>>>) s -> FakeRestController.getMsgs(getResources().openRawResource(R.raw.fake_msgs), s))
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(rc::addMsgs, t->FakeRestController.handleError(t, "getMsgs"));

        getUsers = FakeRestController.getUsers(getResources().openRawResource(R.raw.fake_users), null)
                .onBackpressureDrop()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rc::addUsers, t->FakeRestController.handleError(t, "getUsers"));

        getMsgs = FakeRestController.getMsgs(getResources().openRawResource(R.raw.fake_msgs), null)
                .onBackpressureDrop()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rc::addMsgs, t->FakeRestController.handleError(t, "getUsers"));

        disposables.add(getUsers);
        disposables.add(getMsgs);

        // publishProcessorUsers.onNext(rc.getItem(Author.class, null, null).getUid());
        Button b = view.findViewById(R.id.send_button);
        b.setOnClickListener(view1 -> {
            if (!isLoading) {
                if (msgText.getText().length() > 0) {
                    scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
                    //rc.addMsg(msgText.getText().toString(), h);
                }
            }
        });

        return view;
    }

    public void setAdapter(){
        recyclerView.setAdapter(ra);
    }

    @Override
    public void onResume(){
        super.onResume();

        disposables.add(getUsers);
        disposables.add(getMsgs);
        disposables.add(update);
    }

    @Override
    public void onPause() {
        super.onPause();
        disposables.remove(getUsers);
        disposables.remove(getUsers);
        disposables.remove(update);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }
}
