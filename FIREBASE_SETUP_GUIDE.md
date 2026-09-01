# Al-Chat: Firebase & Backend Setup Guide

Welcome to **Al-Chat**! This guide walks you through connecting your Firebase backend to Al-Chat and generating your APK or Google Play App Bundle (AAB).

---

## 1. Create a Firebase Project

1. Open the [Firebase Console](https://console.firebase.google.com/).
2. Click **Add Project** and name it **Al-Chat**.
3. (Optional) Enable Google Analytics and click **Create Project**.

---

## 2. Register Android App in Firebase

1. In the Firebase Project Dashboard, click the **Android** icon.
2. Enter the Package Name:
   ```
   com.aistudio.alchat.messaging
   ```
3. Enter App Nickname: `Al-Chat`
4. Click **Register App**.
5. Download the `google-services.json` file.
6. Copy `google-services.json` into the `/app/` directory of this project:
   ```
   Al-Chat/
   ├── app/
   │   ├── google-services.json   <-- Place it here
   │   ├── build.gradle.kts
   │   └── src/
   ```

---

## 3. Enable Authentication (Email / Password Only)

> ⚠️ **Zero Phone Number Policy**: Al-Chat uses email authentication only for privacy.

1. In Firebase Console, go to **Build** → **Authentication**.
2. Click **Get Started**.
3. Under the **Sign-in method** tab, select **Email/Password**.
4. Toggle **Enable** on and click **Save**.
5. Under the **Templates** tab, customize your **Email address verification** and **Password reset** email copy.

---

## 4. Enable Cloud Firestore Database

1. Go to **Build** → **Firestore Database** → **Create Database**.
2. Choose **Start in production mode** and select your closest cloud region.
3. In the **Rules** tab, paste the following security rules:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Users collection
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Conversations collection
    match /conversations/{conversationId} {
      allow read, write: if request.auth != null && 
        (request.auth.uid in resource.data.participants || request.auth.uid in request.resource.data.participants);
      
      // Messages sub-collection
      match /messages/{messageId} {
        allow read, create: if request.auth != null;
        allow update: if request.auth != null; // For emoji reactions & status
        allow delete: if request.auth != null && resource.data.senderId == request.auth.uid;
      }
    }
    
    // Groups collection
    match /groups/{groupId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update, delete: if request.auth != null && request.auth.uid in resource.data.adminIds;
    }
  }
}
```

---

## 5. Enable Firebase Storage (Media, Photos & Audio)

1. Go to **Build** → **Storage** → **Get Started**.
2. Set the following Storage Security Rules:

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /chat_media/{conversationId}/{allPaths=**} {
      allow read, write: if request.auth != null;
    }
    match /user_avatars/{userId}/{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

---

## 6. Push Notifications with FCM (Firebase Cloud Messaging)

1. In Firebase Console, go to **Project Settings** (gear icon) → **Cloud Messaging**.
2. FCM tokens will register automatically upon user sign in.
3. You can send test push notifications from the **Messaging** panel or using Firebase Cloud Functions on message creation.
