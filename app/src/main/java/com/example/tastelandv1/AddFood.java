package com.example.tastelandv1;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

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

    public AddFood() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_food, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI elements
        etFoodName = view.findViewById(R.id.ETFoodName);
        btnDueDateValue = view.findViewById(R.id.BtnDueDateValue);
        btnReminderValue = view.findViewById(R.id.BtnReminderValue);
        btnSaveFood = view.findViewById(R.id.BtnSaveFood);

        // Setup click listeners for date and time pickers
        setupDueDateClickListener();
        setupReminderClickListener();

        // TODO: Find and set up the click listener for a "Save Food" button.
        btnSaveFood.setOnClickListener(v -> saveFoodItem());

    }

    private void setupDueDateClickListener() {
        btnDueDateValue.setOnClickListener(v -> {

            // --- Create Calendar Constraints to disable past dates ---
            CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();

            // Sets the validator to only allow dates from today forward            constraintsBuilder.setValidator(DateValidatorPointForward.now());
            // Sets the validator to only allow dates from today forward
            constraintsBuilder.setValidator(DateValidatorPointForward.now());

            // --- Create a MaterialDatePicker builder with the constraints ---
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Due Date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds()) // Set initial selection
                    .setCalendarConstraints(constraintsBuilder.build()) // Apply the constraints
                    .build();

            // Add a listener for when the user picks a date
            datePicker.addOnPositiveButtonClickListener(selection -> {
                selectedDueDateMillis = selection; // Store the selected date in milliseconds

                // The selection is in UTC. Adjust for the local time zone for display.
                TimeZone timeZone = TimeZone.getDefault();
                long offset = timeZone.getOffset(new Date().getTime()) * -1;
                Date selectedDate = new Date(selectedDueDateMillis + offset);

                // Format the date into a readable string like "dd/MM/yyyy"
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String formattedDate = format.format(selectedDate);

                // Update the btnDueDateValue with the selected date
                btnDueDateValue.setText(formattedDate);
            });

            // Show the date picker dialog
            // Use childFragmentManager inside a Fragment
            datePicker.show(getChildFragmentManager(), "DATE_PICKER_TAG");
        });
    }


    private void setupReminderClickListener() {
        btnReminderValue.setOnClickListener(v -> {
            // Get current time for the picker's default
            Calendar calendar = Calendar.getInstance();
            int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
            int currentMinute = calendar.get(Calendar.MINUTE);

            // Create the MaterialTimePicker
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H) // Or CLOCK_24H
                    .setHour(currentHour)
                    .setMinute(currentMinute)
                    .setTitleText("Select Reminder Time")
                    .build();

            // Add a listener for when the user picks a time
            timePicker.addOnPositiveButtonClickListener(dialog -> {
                selectedReminderHour = timePicker.getHour();
                selectedReminderMinute = timePicker.getMinute();

                // Format the time into a user-friendly string (e.g., "10:30 AM")
                String period = (selectedReminderHour < 12) ? "AM" : "PM";
                int displayHour = (selectedReminderHour % 12 == 0) ? 12 : selectedReminderHour % 12;

                String formattedTime = String.format(Locale.getDefault(), "%d:%02d %s",
                        displayHour, selectedReminderMinute, period);

                // Update the button's text
                btnReminderValue.setText(formattedTime);
            });

            // Show the time picker
            timePicker.show(getChildFragmentManager(), "TIME_PICKER_TAG");
        });
    }

    private void saveFoodItem() {
        // --- 1. Validate Input ---
        String foodName = etFoodName.getText().toString().trim();
        if (foodName.isEmpty()) {
            etFoodName.setError("Food name cannot be empty");
            return; // Stop the save process
        }

        if (selectedDueDateMillis == null) {
            Toast.makeText(getContext(), "Please select a due date", Toast.LENGTH_SHORT).show();
            return; // Stop the save process
        }

        // --- 2. Gather Data and Create FoodItem Object ---
        Date dueDate = new Date(selectedDueDateMillis);
        Date reminderDate = createReminderDate(); // Use the helper method we made earlier

        FoodItem newFoodItem = new FoodItem(foodName, dueDate, reminderDate);

        // --- 3. Persist Data (For now, just log it) ---
        // This is where you would call your ViewModel to save to a database.
        // For testing, we'll print it to the Logcat.
        // NOTICE how every line now starts with "Log.d", NOT "androidx.camera..."
        Log.d("AddFoodFragment", "--- New Food Item Saved ---");
        Log.d("AddFoodFragment", "ID: " + newFoodItem.getId());
        Log.d("AddFoodFragment", "Name: " + newFoodItem.getName());
        Log.d("AddFoodFragment", "Due Date: " + newFoodItem.getDueDate().toString());
        if (newFoodItem.getReminderDate() != null) {
            Log.d("AddFoodFragment", "Reminder: " + newFoodItem.getReminderDate().toString());
        } else {
            Log.d("AddFoodFragment", "Reminder: Not set");
        }
        Log.d("AddFoodFragment", "isFinished: " + newFoodItem.isFinished());
        Log.d("AddFoodFragment", "Created At: " + newFoodItem.getCreatedAt().toString());


        // --- 4. Provide User Feedback and Close Fragment ---
        Toast.makeText(getContext(), "Food item saved!", Toast.LENGTH_SHORT).show();

        // This removes the fragment from the screen and returns to the previous page
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    // This helper method should already be in your class from before
    private Date createReminderDate() {
        if (selectedDueDateMillis == null || selectedReminderHour == null || selectedReminderMinute == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selectedDueDateMillis);
        calendar.set(Calendar.HOUR_OF_DAY, selectedReminderHour);
        calendar.set(Calendar.MINUTE, selectedReminderMinute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}
