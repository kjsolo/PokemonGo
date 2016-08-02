package cn.soloho.pokemongo;

import android.app.Fragment;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import cn.soloho.pokemongo.databinding.MainListBinding;
import cn.soloho.pokemongo.databinding.MainListItemBinding;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by solo on 16/8/1.
 */
public class MainListFragment extends Fragment {

    private static final String TAG = "MainListFragment";
    private Subscription subscription;
    private MainListBinding binding;
    private List<DocumentModel> documentModels;

    public static MainListFragment newInstance() {
        Bundle args = new Bundle();
        MainListFragment fragment = new MainListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.main_list, container, false);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.recyclerView.setAdapter(new MyAdapter());
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        subscription = DataManager.getInstance().getPokemonDocs()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MySubscriber());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

    public void setDocumentModels(List<DocumentModel> documentModels) {
        this.documentModels = documentModels;
        binding.recyclerView.getAdapter().notifyDataSetChanged();
    }

    private class MySubscriber extends Subscriber<List<DocumentModel>> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            Log.e(TAG, e.getMessage(), e);
        }

        @Override
        public void onNext(List<DocumentModel> documentModels) {
            setDocumentModels(documentModels);
        }
    }

    private class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MyViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.main_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final DocumentModel doc = documentModels.get(position);
            holder.binding.setDoc(doc);
            holder.binding.executePendingBindings();
            holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getActivity(), DetailActivity.class)
                            .putExtra(DetailActivity.KEY_URL, doc.getLink())
                            .putExtra(DetailActivity.KEY_TITLE, doc.getTitle()));
                }
            });
        }

        @Override
        public int getItemCount() {
            return documentModels != null ? documentModels.size() : 0;
        }
    }

    private static class MyViewHolder extends RecyclerView.ViewHolder {

        private final MainListItemBinding binding;

        public MyViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }
    }
}
