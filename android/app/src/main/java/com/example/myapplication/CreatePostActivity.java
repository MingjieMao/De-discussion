package com.example.myapplication;

import android.net.Uri;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.io.IOException;
import java.util.Locale;

public class CreatePostActivity extends AppCompatActivity {
    private EditText inputPostTitle;
    private EditText inputPostBody;
    private ImageView imagePostPreview;
    private Button buttonPublishPost;
    private Uri selectedPostImageUri;

    private final ActivityResultLauncher<String> pickPostImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) {
                    return;
                }
                try {
                    selectedPostImageUri = ImageStorage.copyToLocalImage(this, uri);
                    imagePostPreview.setImageURI(selectedPostImageUri);
                    imagePostPreview.setVisibility(android.view.View.VISIBLE);
                } catch (IOException exception) {
                    Toast.makeText(this, getString(R.string.toast_action_failed), Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiPreferences.applyAppearance(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_post);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.createPostRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    v.getPaddingLeft(),
                    systemBars.top + v.getPaddingTop(),
                    v.getPaddingRight(),
                    systemBars.bottom + v.getPaddingBottom()
            );
            return insets;
        });

        TextView buttonCancelPost = findViewById(R.id.buttonCancelPost);
        TextView textCreatePostAvatar = findViewById(R.id.textCreatePostAvatar);
        inputPostTitle = findViewById(R.id.inputPostTitle);
        inputPostBody = findViewById(R.id.inputPostBody);
        imagePostPreview = findViewById(R.id.imagePostPreview);
        Button buttonAddPostImage = findViewById(R.id.buttonAddPostImage);
        buttonPublishPost = findViewById(R.id.buttonPublishPost);

        String nickname = UiPreferences.getProfileNickname(this);
        textCreatePostAvatar.setText(getAvatarLetter(nickname));
        textCreatePostAvatar.setBackground(makeAvatarBackground());

        buttonCancelPost.setOnClickListener(v -> finish());
        buttonAddPostImage.setOnClickListener(v -> pickPostImageLauncher.launch("image/*"));
        buttonPublishPost.setOnClickListener(v -> publishPost());
        inputPostTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                refreshPublishState();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        inputPostTitle.requestFocus();
        refreshPublishState();
    }

    private void refreshPublishState() {
        buttonPublishPost.setEnabled(!inputPostTitle.getText().toString().trim().isEmpty());
        buttonPublishPost.setAlpha(buttonPublishPost.isEnabled() ? 1.0f : 0.45f);
    }

    private void publishPost() {
        String title = inputPostTitle.getText().toString().trim();
        if (title.isEmpty()) {
            inputPostTitle.setError(getString(R.string.dialog_post_title_hint));
            return;
        }

        String imageUri = selectedPostImageUri == null ? null : selectedPostImageUri.toString();
        AppData.createPost(title, inputPostBody.getText().toString().trim(), imageUri);
        Toast.makeText(this, getString(R.string.toast_post_created), Toast.LENGTH_SHORT).show();
        finish();
    }

    private GradientDrawable makeAvatarBackground() {
        GradientDrawable drawable = (GradientDrawable) ContextCompat.getDrawable(this, R.drawable.bg_avatar_circle).mutate();
        drawable.setColor(UiPreferences.getAvatarColor(this));
        return drawable;
    }

    private String getAvatarLetter(String nickname) {
        String trimmed = nickname == null ? "" : nickname.trim();
        if (trimmed.isEmpty()) {
            return "?";
        }
        return trimmed.substring(0, 1).toUpperCase(Locale.getDefault());
    }
}
