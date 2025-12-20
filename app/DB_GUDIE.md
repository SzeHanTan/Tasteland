This project uses **Supabase** as our backend database and **Retrofit** to handle network requests in Java.

## 🚀 Setup & Prerequisites

Before working on the backend code, ensure your environment is set up:

1. **Dependencies**: The `build.gradle (app)` file must include:
```groovy
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'

```


2. **Permissions**: `AndroidManifest.xml` must have:
```xml
<uses-permission android:name="android.permission.INTERNET" />

```


3. **API Keys**:
* Go to `RetrofitClient.java`.
* Ensure `SUPABASE_URL` and `SUPABASE_KEY` are populated with our project credentials.



---

## 🛠️ Key Classes Overview

| Class | Purpose |
| --- | --- |
| **`RetrofitClient`** | The singleton "Connection Manager." Use this to get an instance of the API. |
| **`SupabaseAPI`** | An Interface listing all available server commands (Get items, Login, Update Profile). |
| **`SessionManager`** | Handles saving/loading the User's **Access Token** so they stay logged in. |
| **`ShoppingItem`** | Data model for the shopping list (matches `shopping_items` table). |
| **`UserProfile`** | Data model for the user profile (matches `profiles` table). |

---

## 💻 How to Use the Database

### 1. How to Call the API

We don't create new connections manually. Always use the singleton:

```java
SupabaseAPI api = RetrofitClient.getInstance().getApi();

```

### 2. How to Fetch Data (Authentication Required)

Most database calls need the user's **Token**. We assume the user is already logged in.

**Example: Get User Profile**

```java
// 1. Get the token from storage
SessionManager session = new SessionManager(getContext());
String token = session.getToken();

// 2. Call the API (Must add "Bearer " before the token)
api.getMyProfile(RetrofitClient.SUPABASE_KEY, "Bearer " + token)
   .enqueue(new Callback<List<UserProfile>>() {
       @Override
       public void onResponse(Call<List<UserProfile>> call, Response<List<UserProfile>> response) {
           if (response.isSuccessful() && response.body() != null) {
               UserProfile user = response.body().get(0);
               // TODO: Update UI with user.getFullName()
           }
       }

       @Override
       public void onFailure(Call<List<UserProfile>> call, Throwable t) {
           // Handle error
       }
   });

```

### 3. How to Save/Update Data

**Example: Add Item to Shopping List**

```java
ShoppingItem newItem = new ShoppingItem("Milk", false);
String token = session.getToken(); // Don't forget to get the token!

api.addItem(RetrofitClient.SUPABASE_KEY, "Bearer " + token, "return=minimal", newItem)
   .enqueue(new Callback<Void>() {
       @Override
       public void onResponse(Call<Void> call, Response<Void> response) {
           if (response.isSuccessful()) {
               // Success! Refresh the list.
           }
       }
       // ... onFailure ...
   });

```

---

## 🗃️ Database Structure (Supabase Tables)

### Table: `shopping_items`

*Stores the checklist items.*

* `id` (int8): Auto-generated ID.
* `text` (text): The name of the item.
* `is_checked` (bool): True/False status.
* `user_id` (uuid): **Linked to Auth**. Automatically filled by Supabase.

### Table: `profiles`

*Stores user details like name and bio.*

* `id` (uuid): Matches the User ID from Login.
* `full_name` (text): Display name.
* `contact_no` (text): Phone number.
* `description` (text): Bio/About me.

---

## ⚠️ Common Issues & Fixes

1. **`401 Unauthorized` Error**
* **Reason:** The user is not logged in, or the Token is missing/expired.
* **Fix:** Check if `session.getToken()` is null. If it is, redirect the user to `Login.class`.


2. **`Attempt to invoke virtual method on a null object reference`**
* **Reason:** You might be trying to access views (like Buttons) in `onCreate` or before `onViewCreated`.
* **Fix:** Always initialize views and listeners inside `onViewCreated`.


3. **Data not saving?**
* **Reason:** Row Level Security (RLS) might be blocking the write.
* **Fix:** Ensure the database policy is set to "Users can insert their own items" in Supabase Dashboard.