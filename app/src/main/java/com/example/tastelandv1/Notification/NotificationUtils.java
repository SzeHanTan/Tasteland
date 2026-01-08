package com.example.tastelandv1.Notification;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NotificationUtils {

    private static boolean isSameDay(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Group notifications into header buckets and return a flattened list of
     * NotificationListItem where each header appears only once and all items
     * for that header follow it.
     */
    public static List<NotificationListItem> groupNotificationsByDate(List<NotificationItem> notifications) {
        List<NotificationListItem> result = new ArrayList<>();
        if (notifications == null || notifications.isEmpty()) return result;

        // Use a LinkedHashMap to preserve insertion order when we iterate later.
        Map<String, List<NotificationItem>> buckets = new LinkedHashMap<>();

        Calendar now = Calendar.getInstance();
        Calendar today = (Calendar) now.clone();
        Calendar yesterday = (Calendar) now.clone();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);

        // 1) Bucket every notification by header
        for (NotificationItem ni : notifications) {
            if (ni == null || ni.getCreatedAt() == null) continue;

            Date created = ni.getCreatedAt();
            Calendar createdCal = Calendar.getInstance();
            createdCal.setTime(created);

            long diffMillis = today.getTimeInMillis() - createdCal.getTimeInMillis();
            long diffDays = diffMillis / (24L * 60L * 60L * 1000L);

            String header;
            if (isSameDay(createdCal, today)) {
                header = "Today";
            } else if (isSameDay(createdCal, yesterday)) {
                header = "Yesterday";
            } else if (diffDays <= 3) {
                header = "Last 3 days";
            } else if (diffDays <= 7) {
                header = "Last 7 days";
            } else if (diffDays <= 30) {
                header = "Last month";
            } else {
                header = "Older";
            }

            List<NotificationItem> list = buckets.get(header);
            if (list == null) {
                list = new ArrayList<>();
                buckets.put(header, list);
            }
            list.add(ni);
        }

        // 2) Define the output order for the headers you want to show.
        //    If a header has no items it will simply be skipped.
        String[] outputOrder = new String[] {
                "Today",
                "Yesterday",
                "Last 3 days",
                "Last 7 days",
                "Last month",
                "Older"
        };

        // 3) For each bucket in desired order: sort inner items and add header + items
        for (String header : outputOrder) {
            List<NotificationItem> bucket = buckets.get(header);
            if (bucket == null || bucket.isEmpty()) continue;

            // Sort bucket items newest -> oldest (remove or change comparator if you want different order)
            Collections.sort(bucket, new Comparator<NotificationItem>() {
                @Override
                public int compare(NotificationItem a, NotificationItem b) {
                    // defensive null checks
                    if (a == null || a.getCreatedAt() == null) return 1;
                    if (b == null || b.getCreatedAt() == null) return -1;
                    return b.getCreatedAt().compareTo(a.getCreatedAt()); // newest first
                }
            });

            // Add header once
            result.add(new NotificationListItem(header));

            // Add each notification wrapped into NotificationListItem
            for (NotificationItem ni : bucket) {
                result.add(new NotificationListItem(ni));
            }
        }

        return result;
    }
}
