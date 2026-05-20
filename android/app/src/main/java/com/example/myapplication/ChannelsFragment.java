package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private TextView textChannelsTitle;
    private TextView textChannelsSubtitle;
    private TextView textFeedEmptyTitle;
    private TextView textFeedEmptyBody;
    private TextView textSearchAssistantBody;
    private ImageView imageChannelsForumAvatar;
    private LinearLayout layoutSearchAssistant;
    private EditText inputChannelSearch;
    private ImageButton buttonClearSearch;
    private RecyclerView recyclerPosts;
    private String currentForumKey;
    private String searchQuery = "";

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
        textChannelsTitle = view.findViewById(R.id.textChannelsTitle);
        textChannelsSubtitle = view.findViewById(R.id.textChannelsSubtitle);
        textFeedEmptyTitle = view.findViewById(R.id.textFeedEmptyTitle);
        textFeedEmptyBody = view.findViewById(R.id.textFeedEmptyBody);
        textSearchAssistantBody = view.findViewById(R.id.textSearchAssistantBody);
        imageChannelsForumAvatar = view.findViewById(R.id.imageChannelsForumAvatar);
        layoutSearchAssistant = view.findViewById(R.id.layoutSearchAssistant);
        inputChannelSearch = view.findViewById(R.id.inputChannelSearch);
        buttonClearSearch = view.findViewById(R.id.buttonClearSearch);
        recyclerPosts = view.findViewById(R.id.recyclerPosts);
        ImageButton buttonChannelsDrawer = view.findViewById(R.id.buttonChannelsDrawer);
        ImageButton buttonCreatePost = view.findViewById(R.id.buttonCreatePost);

        recyclerPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        buttonChannelsDrawer.setOnClickListener(v -> host().openDrawer());
        buttonCreatePost.setOnClickListener(v -> startActivity(new Intent(requireContext(), CreatePostActivity.class)));
        buttonClearSearch.setOnClickListener(v -> inputChannelSearch.setText(""));
        inputChannelSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s == null ? "" : s.toString();
                refreshContent();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
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
        String selectedForumKey = AppData.getSelectedForumKey();
        if (currentForumKey == null || !currentForumKey.equals(selectedForumKey)) {
            currentForumKey = selectedForumKey;
            if (!searchQuery.isEmpty()) {
                inputChannelSearch.setText("");
                return;
            }
        }
        textChannelsTitle.setText(AppData.getSelectedForumLabel(requireContext()));
        imageChannelsForumAvatar.setImageResource(AppData.getSelectedForumAvatarResId());
        textChannelsSubtitle.setVisibility(View.GONE);
        inputChannelSearch.setHint(getString(R.string.search_channel_hint, AppData.getSelectedForumLabel(requireContext())));
        buttonClearSearch.setVisibility(searchQuery.trim().isEmpty() ? View.GONE : View.VISIBLE);
        refreshSearchAssistant();

        ArrayList<Post> posts = AppData.searchPosts(requireContext(), searchQuery);
        PostAdapter adapter = new PostAdapter(posts);
        adapter.setOnClickListener(this::openPost);
        adapter.setOnVoteClickListener((post, direction) -> AppData.togglePostVote(post, direction));
        recyclerPosts.setAdapter(adapter);

        boolean empty = posts.isEmpty();
        recyclerPosts.setVisibility(empty ? View.GONE : View.VISIBLE);
        textFeedEmptyTitle.setVisibility(empty ? View.VISIBLE : View.GONE);
        textFeedEmptyBody.setVisibility(empty ? View.VISIBLE : View.GONE);
        if (empty && !searchQuery.trim().isEmpty()) {
            textFeedEmptyTitle.setText(R.string.search_empty_title);
            textFeedEmptyBody.setText(R.string.search_empty_body);
        } else {
            textFeedEmptyTitle.setText(AppData.getFeedEmptyTitle(requireContext()));
            textFeedEmptyBody.setText(AppData.getFeedEmptyBody(requireContext()));
        }
    }

    private void refreshSearchAssistant() {
        String trimmedQuery = searchQuery.trim();
        boolean showAssistant = looksLikeQuestion(trimmedQuery);
        layoutSearchAssistant.setVisibility(showAssistant ? View.VISIBLE : View.GONE);
        if (showAssistant) {
            textSearchAssistantBody.setText(getString(
                    R.string.search_ai_answer_body,
                    AppData.getSelectedForumLabel(requireContext()),
                    trimmedQuery
            ));
        }
    }

    private boolean looksLikeQuestion(String query) {
        if (query.length() < 4) {
            return false;
        }
        String lower = query.toLowerCase();
        return query.contains("?")
                || query.contains("？")
                || lower.startsWith("how ")
                || lower.startsWith("why ")
                || lower.startsWith("what ")
                || lower.startsWith("where ")
                || lower.startsWith("when ")
                || lower.startsWith("can ")
                || lower.startsWith("should ")
                || lower.contains("怎么")
                || lower.contains("为什么")
                || lower.contains("如何")
                || lower.contains("吗")
                || lower.contains("难不难");
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
