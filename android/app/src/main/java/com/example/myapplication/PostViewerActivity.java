package com.example.myapplication;

import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import dao.model.Message;
import dao.model.Post;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class PostViewerActivity extends AppCompatActivity {
    public static final String EXTRA_POST_ID = "post_id";

    private TextView textPostViewerMode;
    private TextView textPostViewerForum;
    private TextView textPostViewerMeta;
    private TextView textPostViewerTitle;
    private TextView textPostViewerBody;
    private TextView textPostViewerState;
    private TextView textPostViewerScore;
    private TextView textPostViewerCommentsCount;
    private TextView textCommentsEmpty;
    private ImageView imagePostViewerCommunityAvatar;
    private ImageView imagePostViewerAttachment;
    private ImageButton buttonPostUpvote;
    private ImageButton buttonPostDownvote;
    private LinearLayout buttonPostComments;
    private Button buttonBack;
    private ImageButton buttonPostMenu;
    private NestedScrollView postViewerScroll;
    private RecyclerView recyclerMessages;
    private Post post;
    private Message rootMessage;
    private UUID pendingScrollMessageId;
    private Uri selectedReplyImageUri;
    private ImageView activeReplyImagePreview;

    private final ActivityResultLauncher<String> pickReplyImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) {
                    return;
                }
                try {
                    selectedReplyImageUri = ImageStorage.copyToLocalImage(this, uri);
                } catch (IOException exception) {
                    Toast.makeText(this, getString(R.string.toast_action_failed), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (activeReplyImagePreview != null) {
                    activeReplyImagePreview.setImageURI(selectedReplyImageUri);
                    activeReplyImagePreview.setVisibility(View.VISIBLE);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiPreferences.applyAppearance(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_post_viewer);
        View postViewerRoot = findViewById(R.id.postViewerRoot);
        int initialPaddingLeft = postViewerRoot.getPaddingLeft();
        int initialPaddingTop = postViewerRoot.getPaddingTop();
        int initialPaddingRight = postViewerRoot.getPaddingRight();
        int initialPaddingBottom = postViewerRoot.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(postViewerRoot, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    initialPaddingLeft,
                    systemBars.top + initialPaddingTop,
                    initialPaddingRight,
                    systemBars.bottom + initialPaddingBottom
            );
            return insets;
        });

        textPostViewerMode = findViewById(R.id.textPostViewerMode);
        textPostViewerForum = findViewById(R.id.textPostViewerForum);
        textPostViewerMeta = findViewById(R.id.textPostViewerMeta);
        textPostViewerTitle = findViewById(R.id.textPostViewerTitle);
        textPostViewerBody = findViewById(R.id.textPostViewerBody);
        textPostViewerState = findViewById(R.id.textPostViewerState);
        textPostViewerScore = findViewById(R.id.textPostViewerScore);
        textPostViewerCommentsCount = findViewById(R.id.textPostViewerCommentsCount);
        textCommentsEmpty = findViewById(R.id.textCommentsEmpty);
        imagePostViewerCommunityAvatar = findViewById(R.id.imagePostViewerCommunityAvatar);
        imagePostViewerAttachment = findViewById(R.id.imagePostViewerAttachment);
        buttonPostComments = findViewById(R.id.textPostViewerComments);
        buttonPostUpvote = findViewById(R.id.buttonPostUpvote);
        buttonPostDownvote = findViewById(R.id.buttonPostDownvote);
        buttonBack = findViewById(R.id.buttonBack);
        buttonPostMenu = findViewById(R.id.buttonPostMenu);
        postViewerScroll = findViewById(R.id.postViewerScroll);
        recyclerMessages = findViewById(R.id.recyclerMessages);

        recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerMessages.setNestedScrollingEnabled(false);
        post = AppData.getPostById(getIntent().getStringExtra(EXTRA_POST_ID));

        buttonBack.setOnClickListener(v -> finish());
        buttonPostUpvote.setOnClickListener(v -> {
            if (AppData.togglePostVote(post, 1)) {
                refreshUi();
                animateVote(buttonPostUpvote);
            }
        });
        buttonPostDownvote.setOnClickListener(v -> {
            if (AppData.togglePostVote(post, -1)) {
                refreshUi();
                animateVote(buttonPostDownvote);
            }
        });
        buttonPostMenu.setOnClickListener(v -> {
            if (rootMessage != null) {
                handleMessageAction(rootMessage);
            }
        });
        buttonPostComments.setOnClickListener(v -> showReplyDialog(rootMessage));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUi();
    }

    private void refreshUi() {
        if (post == null) {
            textPostViewerMode.setText(R.string.thread_unavailable);
            textPostViewerForum.setText(R.string.post_not_found);
            textPostViewerMeta.setText(R.string.post_not_found_body);
            textPostViewerTitle.setText("");
            textPostViewerBody.setText(R.string.post_not_found_summary);
            textPostViewerState.setVisibility(View.GONE);
            textCommentsEmpty.setVisibility(View.VISIBLE);
            recyclerMessages.setAdapter(new MessageAdapter(new ArrayList<>()));
            return;
        }

        rootMessage = AppData.getRootMessage(post);
        textPostViewerMode.setText(AppData.getCurrentModeLabel(this));
        textPostViewerForum.setText(AppData.getPostCommunityLabel(this, post));
        imagePostViewerCommunityAvatar.setImageResource(AppData.getPostCommunityAvatarResId(post));
        textPostViewerMeta.setText(getString(
                R.string.message_author_line,
                AppData.getUsername(post.poster),
                AppData.getPostTimestampLabel(post)
        ));
        textPostViewerTitle.setText(post.topic);
        textPostViewerBody.setText(AppData.getPostBody(post));
        String postImageUri = AppData.getPostImageUri(post);
        if (postImageUri == null || postImageUri.isEmpty()) {
            imagePostViewerAttachment.setImageDrawable(null);
            imagePostViewerAttachment.setOnClickListener(null);
            imagePostViewerAttachment.setVisibility(View.GONE);
        } else {
            Uri attachmentUri = Uri.parse(postImageUri);
            imagePostViewerAttachment.setImageURI(attachmentUri);
            imagePostViewerAttachment.setOnClickListener(v ->
                    ImageAttachmentViewer.show(this, attachmentUri, R.string.post_image_attachment));
            imagePostViewerAttachment.setVisibility(View.VISIBLE);
        }
        textPostViewerCommentsCount.setText(AppData.getPostReplyCountLabel(this, post));
        textPostViewerScore.setText(String.valueOf(AppData.getPostVoteScore(post)));
        updatePostVoteColors();

        String rootState = rootMessage == null ? "" : AppData.getMessageStatus(this, rootMessage);
        textPostViewerState.setText(rootState);
        textPostViewerState.setVisibility(rootState.isEmpty() ? View.GONE : View.VISIBLE);

        ArrayList<Message> messages = AppData.getMessages(post);
        textCommentsEmpty.setVisibility(messages.isEmpty() ? View.VISIBLE : View.GONE);

        MessageAdapter adapter = new MessageAdapter(messages);
        adapter.setOnMessageActionListener(this::handleMessageAction);
        adapter.setOnMessageVoteListener((message, direction) -> {
            AppData.toggleMessageVote(message, direction);
            refreshUi();
        });
        adapter.setOnMessageReplyListener(this::showReplyDialog);
        recyclerMessages.setAdapter(adapter);
        scrollToPendingReply(messages);
    }

    private void updatePostVoteColors() {
        int upvoteColor = ContextCompat.getColor(this, R.color.vote_up);
        int neutralColor = ContextCompat.getColor(this, R.color.ink_secondary);
        int downvoteColor = ContextCompat.getColor(this, R.color.vote_down);
        int primaryColor = ContextCompat.getColor(this, R.color.ink_primary);
        int reportColor = ContextCompat.getColor(this, R.color.danger_ink);

        int voteDirection = AppData.getCurrentUserPostVote(post);
        buttonPostUpvote.setImageResource(voteDirection > 0
                ? R.drawable.ic_vote_up_filled_24
                : R.drawable.ic_vote_up_outline_24);
        buttonPostDownvote.setImageResource(voteDirection < 0
                ? R.drawable.ic_vote_down_filled_24
                : R.drawable.ic_vote_down_outline_24);
        boolean activeAction = AppData.isAdminMode() ? AppData.isHidden(rootMessage) : AppData.hasCurrentUserReported(rootMessage);
        if (AppData.isAdminMode()) {
            buttonPostMenu.setImageResource(R.drawable.ic_hidden_24);
        } else {
            buttonPostMenu.setImageResource(activeAction ? R.drawable.ic_flag_24 : R.drawable.ic_flag_outline_24);
        }
        buttonPostMenu.setColorFilter(activeAction ? reportColor : neutralColor);
        textPostViewerScore.setTextColor(voteDirection > 0
                ? upvoteColor
                : voteDirection < 0 ? downvoteColor : primaryColor);
    }

    private void animateVote(View view) {
        view.animate().cancel();
        view.setScaleX(1.0f);
        view.setScaleY(1.0f);
        view.animate()
                .scaleX(1.16f)
                .scaleY(1.16f)
                .setDuration(110L)
                .withEndAction(() -> view.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(160L)
                        .start())
                .start();
    }

    private void handleMessageAction(Message message) {
        boolean success = AppData.isAdminMode() ? AppData.toggleHidden(message) : AppData.toggleReport(message);
        if (!success) {
            Toast.makeText(this, getString(R.string.toast_action_failed), Toast.LENGTH_SHORT).show();
            return;
        }

        String feedback = AppData.isAdminMode()
                ? (AppData.isHidden(message)
                ? getString(R.string.toast_reply_hidden)
                : getString(R.string.toast_reply_restored))
                : (AppData.hasCurrentUserReported(message)
                ? getString(R.string.toast_reply_reported)
                : getString(R.string.toast_report_removed));
        Toast.makeText(this, feedback, Toast.LENGTH_SHORT).show();
        refreshUi();
    }

    private void showReplyDialog(Message parent) {
        if (post == null || parent == null) {
            Toast.makeText(this, getString(R.string.toast_action_failed), Toast.LENGTH_SHORT).show();
            return;
        }

        selectedReplyImageUri = null;
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
        input.setSingleLine(false);
        input.setMinLines(2);
        input.setMaxLines(4);
        input.setHint(R.string.dialog_reply_body_hint);
        int horizontal = dp(16);
        int vertical = dp(12);
        input.setPadding(horizontal, vertical, horizontal, vertical);

        LinearLayout dialogContent = new LinearLayout(this);
        dialogContent.setOrientation(LinearLayout.VERTICAL);
        int dialogSpacing = dp(12);
        dialogContent.setPadding(0, dialogSpacing, 0, 0);
        dialogContent.addView(input, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        Button buttonAttachImage = new Button(this);
        buttonAttachImage.setText(R.string.action_add_image);
        buttonAttachImage.setAllCaps(false);
        buttonAttachImage.setOnClickListener(v -> pickReplyImageLauncher.launch("image/*"));
        LinearLayout.LayoutParams attachParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        attachParams.topMargin = dialogSpacing;
        dialogContent.addView(buttonAttachImage, attachParams);

        ImageView imagePreview = new ImageView(this);
        imagePreview.setAdjustViewBounds(true);
        imagePreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imagePreview.setVisibility(View.GONE);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(180)
        );
        imageParams.topMargin = dp(8);
        dialogContent.addView(imagePreview, imageParams);
        activeReplyImagePreview = imagePreview;

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_reply_title)
                .setView(dialogContent)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_reply, null)
                .create();

        dialog.setOnShowListener(unused -> {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> publishReply(input, parent, dialog));
        });
        dialog.setOnDismissListener(unused -> {
            activeReplyImagePreview = null;
            selectedReplyImageUri = null;
        });
        dialog.show();
        input.requestFocus();
    }

    private void publishReply(EditText input, Message parent, androidx.appcompat.app.AlertDialog dialog) {
        String content = input.getText().toString().trim();
        String imageUri = selectedReplyImageUri == null ? null : selectedReplyImageUri.toString();
        if (content.isEmpty() && imageUri == null) {
            input.setError(getString(R.string.dialog_reply_body_hint));
            return;
        }

        Message reply = AppData.createReply(parent, content, imageUri);
        if (reply == null) {
            Toast.makeText(this, getString(R.string.toast_action_failed), Toast.LENGTH_SHORT).show();
            return;
        }

        pendingScrollMessageId = reply.id();
        Toast.makeText(this, getString(R.string.toast_reply_created), Toast.LENGTH_SHORT).show();
        dialog.dismiss();
        refreshUi();
    }

    private void scrollToPendingReply(ArrayList<Message> messages) {
        if (pendingScrollMessageId == null) {
            return;
        }

        int targetPosition = -1;
        for (int i = 0; i < messages.size(); i++) {
            if (pendingScrollMessageId.equals(messages.get(i).id())) {
                targetPosition = i;
                break;
            }
        }
        pendingScrollMessageId = null;

        if (targetPosition >= 0) {
            int position = targetPosition;
            recyclerMessages.post(() -> {
                RecyclerView.ViewHolder holder = recyclerMessages.findViewHolderForAdapterPosition(position);
                if (holder == null) {
                    postViewerScroll.smoothScrollTo(0, recyclerMessages.getBottom());
                    return;
                }

                int targetY = recyclerMessages.getTop() + holder.itemView.getTop();
                postViewerScroll.smoothScrollTo(0, Math.max(0, targetY - dp(12)));
            });
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
