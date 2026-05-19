package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dao.model.Post;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private final List<Post> posts;
    private OnClickListener onClickListener;
    private OnLikeClickListener onLikeClickListener;

    public PostAdapter(List<Post> posts) {
        this.posts = posts;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setOnLikeClickListener(OnLikeClickListener onLikeClickListener) {
        this.onLikeClickListener = onLikeClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.display(post);
        holder.itemView.setOnClickListener(v -> {
            if (onClickListener != null) {
                onClickListener.onClick(post);
            }
        });
        holder.buttonPostLike.setOnClickListener(v -> {
            if (onLikeClickListener != null) {
                onLikeClickListener.onLikeClick(post);
                holder.display(post);
                animateLike(holder.buttonPostLike);
            }
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public interface OnClickListener {
        void onClick(Post post);
    }

    public interface OnLikeClickListener {
        void onLikeClick(Post post);
    }

    private void animateLike(View view) {
        view.animate().cancel();
        view.setScaleX(1.0f);
        view.setScaleY(1.0f);
        view.animate()
                .scaleX(1.22f)
                .scaleY(1.22f)
                .setDuration(110L)
                .withEndAction(() -> view.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(160L)
                        .start())
                .start();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textPostTitle;
        private final TextView textPostAuthor;
        private final TextView textPostCount;
        private final TextView textPostLikeCount;
        private final ImageView iconPostLike;
        private final LinearLayout buttonPostLike;

        ViewHolder(View view) {
            super(view);
            textPostTitle = view.findViewById(R.id.textPostTitle);
            textPostAuthor = view.findViewById(R.id.textPostAuthor);
            textPostCount = view.findViewById(R.id.textPostReplyCount);
            textPostLikeCount = view.findViewById(R.id.textPostLikeCount);
            iconPostLike = view.findViewById(R.id.iconPostLike);
            buttonPostLike = view.findViewById(R.id.buttonPostLike);
        }

        void display(Post post) {
            textPostTitle.setText(post.topic);
            textPostAuthor.setText(AppData.getPostMeta(itemView.getContext(), post));
            textPostCount.setText(AppData.getPostReplyCountLabel(itemView.getContext(), post));
            textPostLikeCount.setText(AppData.getPostLikeCountLabel(itemView.getContext(), post));
            iconPostLike.setImageResource(AppData.hasCurrentUserLikedPost(post)
                    ? R.drawable.ic_like_filled_24
                    : R.drawable.ic_like_outline_24);
        }
    }
}
