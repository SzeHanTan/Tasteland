package com.example.tastelandv1.Food.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tastelandv1.Food.database.FoodItem;
import com.example.tastelandv1.Food.database.FoodRepository;
import com.example.tastelandv1.R;
import com.example.tastelandv1.Notification.ReminderReceiver;
import com.example.tastelandv1.Backend.SessionManager;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class AddFood extends Fragment {

    private EditText etFoodName;
    private TextView btnDueDateValue;
    private Button btnReminderValue;
    private Button btnSaveFood;

    // Data holders
    private Long selectedDueDateMillis = null;
    private Integer selectedReminderHour = null;
    private Integer selectedReminderMinute = null;

    private FoodRepository repository;
    private static final int PERMISSION_REQUEST_CODE = 101;

    public AddFood() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_food, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new FoodRepository(getContext());

        etFoodName = view.findViewById(R.id.ETFoodName);
        btnDueDateValue = view.findViewById(R.id.BtnDueDateValue);
        btnReminderValue = view.findViewById(R.id.BtnReminderValue);
        btnSaveFood = view.findViewById(R.id.BtnSaveFood);

        setupDueDateClickListener();
        setupReminderClickListener();

        btnSaveFood.setOnClickListener(v -> saveFoodItem());

        checkNotificationPermission();
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void setupDueDateClickListener() {
        btnDueDateValue.setOnClickListener(v -> {
            CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
            constraintsBuilder.setValidator(DateValidatorPointForward.now());

            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Due Date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .setCalendarConstraints(constraintsBuilder.build())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                selectedDueDateMillis = selection;
                TimeZone timeZone = TimeZone.getDefault();
                long offset = timeZone.getOffset(new Date().getTime()) * -1;
                Date selectedDate = new Date(selectedDueDateMillis + offset);

                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String formattedDate = format.format(selectedDate);
                btnDueDateValue.setText(formattedDate);
            });

            datePicker.show(getChildFragmentManager(), "DATE_PICKER_TAG");
        });
    }

    private void setupReminderClickListener() {
        btnReminderValue.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
            int currentMinute = calendar.get(Calendar.MINUTE);

            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(currentHour)
                    .setMinute(currentMinute)
                    .setTitleText("Select Reminder Time")
                    .build();

            timePicker.addOnPositiveButtonClickListener(dialog -> {
                selectedReminderHour = timePicker.getHour();
                selectedReminderMinute = timePicker.getMinute();

                String period = (selectedReminderHour < 12) ? "AM" : "PM";
                int displayHour = (selectedReminderHour % 12 == 0) ? 12 : selectedReminderHour % 12;
                String formattedTime = String.format(Locale.getDefault(), "%d:%02d %s",
                        displayHour, selectedReminderMinute, period);

                btnReminderValue.setText(formattedTime);
            });

            timePicker.show(getChildFragmentManager(), "TIME_PICKER_TAG");
        });
    }

    private void saveFoodItem() {
        // 1. Validate Input
        String foodName = etFoodName.getText().toString().trim();
        if (foodName.isEmpty()) {
            etFoodName.setError("Food name cannot be empty");
            return;
        }

        if (selectedDueDateMillis == null) {
            Toast.makeText(getContext(), "Please select a due date", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Check Alarm Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                Toast.makeText(getContext(), "Please allow Alarms & Reminders for this app", Toast.LENGTH_LONG).show();
                return;
            }
        }

        // 3. Create the FoodItem Object
        String currentUserId = getCurrentUserId();
        Date dueDate = new Date(selectedDueDateMillis);
        Date reminderDate = createReminderDate();

        FoodItem newItem = new FoodItem(currentUserId, foodName, dueDate, reminderDate);

        // 4. Save via Repository
        repository.addFoodItem(newItem, new FoodRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                // Schedule local notification only after successful API save
                scheduleLocalNotification(newItem);

                // Notify parent fragment to refresh list
                Bundle result = new Bundle();
                result.putBoolean("refresh", true);
                getParentFragmentManager().setFragmentResult("refresh_request", result);

                if (getActivity() != null) {
                    Toast.makeText(getContext(), "Saved successfully!", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                }
            }

            @Override
            public void onError(String error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String getCurrentUserId() {
        SessionManager session = new SessionManager(getContext());
        return session.getUserId();
    }

    private Date createReminderDate() {
        Calendar calendar = Calendar.getInstance();

        if (selectedReminderHour == null || selectedReminderMinute == null) {
            calendar.setTimeInMillis(selectedDueDateMillis);
            calendar.set(Calendar.HOUR_OF_DAY, 8); // Default 8 AM
            calendar.set(Calendar.MINUTE, 0);
        } else {
            calendar.setTimeInMillis(selectedDueDateMillis);
            calendar.set(Calendar.HOUR_OF_DAY, selectedReminderHour);
            calendar.set(Calendar.MINUTE, selectedReminderMinute);
        }

        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    @SuppressLint("ScheduleExactAlarm")
    private void scheduleLocalNotification(FoodItem food) {
        if (food.dueDate == null) return;

        Date triggerDate;
        if (food.reminderDate != null) {
            triggerDate = food.reminderDate;
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(food.dueDate);
            cal.add(Calendar.DAY_OF_MONTH, -3);
            triggerDate = cal.getTime();
        }

        if (triggerDate.getTime() < System.currentTimeMillis()) return;

        Intent intent = new Intent(requireContext(), ReminderReceiver.class);
        intent.putExtra("food_name", food.name);

        int notificationId = (food.id != null) ? food.id.hashCode() : (int) System.currentTimeMillis();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerDate.getTime(),
                    pendingIntent);
        }
    }
}