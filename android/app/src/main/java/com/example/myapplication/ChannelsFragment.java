package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import dao.model.Post;

public class ChannelsFragment extends Fragment implements RefreshablePage {
    private TextView textChannelsMode;
    private TextView textChannelsSubtitle;
    private TextView textChannelsHighlightBody;
    private RecyclerView recyclerPosts;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_channels, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        textChannelsMode = view.findViewById(R.id.textChannelsMode);
        textChannelsSubtitle = view.findViewById(R.id.textChannelsSubtitle);
        textChannelsHighlightBody = view.findViewById(R.id.textChannelsHighlightBody);
        recyclerPosts = view.findViewById(R.id.recyclerPosts);
        ImageButton buttonChannelsDrawer = view.findViewById(R.id.buttonChannelsDrawer);
        ImageButton buttonCreatePost = view.findViewById(R.id.buttonCreatePost);

        recyclerPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        buttonChannelsDrawer.setOnClickListener(v -> host().openDrawer());
        buttonCreatePost.setOnClickListener(v -> startActivity(new Intent(requireContext(), CreatePostActivity.class)));
        refreshContent();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshContent();
    }

    @Override
    public void refreshContent() {
        if (!isAdded() || getView() == null || textChannelsMode == null || recyclerPosts == null) {
            return;
        }

        textChannelsMode.setText(AppData.getCurrentModeLabel(requireContext()));
        textChannelsSubtitle.setText(AppData.getMainSubtitle(requireContext()));
        textChannelsHighlightBody.setText(AppData.isAdminMode()
                ? getString(R.string.channels_highlight_admin)
                : getString(R.string.channels_highlight_member));

        ArrayList<Post> posts = AppData.getPosts();
        PostAdapter adapter = new PostAdapter(posts);
        adapter.setOnClickListener(this::openPost);
        adapter.setOnLikeClickListener(post -> AppData.togglePostLike(post));
        recyclerPosts.setAdapter(adapter);
    }

    private void openPost(Post post) {
        if (!isAdded()) {
            return;
        }

        Intent intent = new Intent(requireContext(), PostViewerActivity.class);
        intent.putExtra(PostViewerActivity.EXTRA_POST_ID, post.id.toString());
        startActivity(intent);
    }

    private MainActivity host() {
        return (MainActivity) requireActivity();
    }
}
