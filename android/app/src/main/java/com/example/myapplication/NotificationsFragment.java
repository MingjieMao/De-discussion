package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NotificationsFragment extends Fragment implements RefreshablePage {
    private RecyclerView recyclerNotifications;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerNotifications = view.findViewById(R.id.recyclerNotifications);
        recyclerNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        refreshContent();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshContent();
    }

    @Override
    public void refreshContent() {
        if (!isAdded() || recyclerNotifications == null) {
            return;
        }

        ArrayList<AppData.AppNotification> notifications = AppData.getNotifications(requireContext());
        NotificationAdapter adapter = new NotificationAdapter(notifications);
        adapter.setOnClickListener(this::openNotification);
        recyclerNotifications.setAdapter(adapter);
    }

    private void openNotification(AppData.AppNotification notification) {
        if (!isAdded() || notification == null) {
            return;
        }
        Intent intent = new Intent(requireContext(), PostViewerActivity.class);
        intent.putExtra(PostViewerActivity.EXTRA_POST_ID, notification.postId().toString());
        startActivity(intent);
    }
}
