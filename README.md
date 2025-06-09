# **💪🔥 Project: "Fitlog"**

**25-1 Mobile Programming Term Project Team6**

**Team Member: DongYeon Kim / SeongEun Kim / KyeongMin Nam / DongHyun Lee**

📝 More about project

**[Technical Documentation]:** https://fan-cornflower-ed9.notion.site/MP_Team6_TermProject-Fitlog-20db186c644e8008b5d2ee3c04bf5e16?source=copy_link

**[Running App(YouTube)]:** https://youtu.be/TXfNFGaVhZ8?si=xvTNXFukfZoQXQGe

## **📜 Contents**

1. [Overview](#Overview)
2. [Main Technical](#Main-Technical)
3. [Develop Environment](#Develop-Environment)
4. [References](#References)

# **Overview**

> _Effective diet management app service using calendar_

In order to succeed in dieting, you need to change the habits that have been ingrained in your body for a long time. In order to form the right habits, you need to form and maintain the habits in a more systematic way than you think.

If your motivation is unclear, you are not able to set appropriate goals for the investment period, or you lack consistent motivation, you need a guide on a systematic approach to form the right habits. We planned Fitlog to be a guideline for habit formation for these users. Record your own exercise routine and create healthy diet habits with Fitlog, which provides calendar and sharing functions! 🔥

**App Develop objective:**

- The goal is to conveniently manage various fitness records (diet, exercise, weight, etc.) in one app and visually check changes over time.
- Rather than simply storing and viewing records, it allows users to easily share records they have left on their calendars with others in the form of social media posts, thereby motivating users and effectively maintaining their goals.

**Expected App User:**

People who want to consistently manage their exercise and diet records, share them with others, and get motivated to manage their diet and health.

# **Main Technical**

- `🔐 Login`
  - Access the service home screen through user login/sign-up
  - User authentication and image and data management using Firebase
- `🗓️ Daily Record Calendar`
  - Save and view daily diet, exercise, and weight records based on calendar
  - Automatically recommend ‘smart tags (ex. running, aerobic)’ according to exercise content when saving records
  - Share feed posts based on recorded daily summary
- `📒 Feed Upload`
  - Save daily data with card UI
  - Create and share feed posts including images, tags, and categories
  - Register on profile page with upload button at the bottom of card
- `👥 Profile with sharing`
  - View and manage feed posts uploaded in nX3 grid format
  - Interact with friends through likes, comments, bookmarks, etc.
- `📊 Goal setting and visualization`
  - Set user target weight and get recommended daily intake based on the goal
  - View data visualizations such as weight change

# **Develop Environment**

### 🕹️ IDE: Android Studio

### 🖥️ Language: Java

### 🗂️ DB

-Firebase Authentication: Implementing email/password based user registration and login function.

-Firebase Database(Cloud Firestore): Store and manage key app data, including user information, feed posts, and daily log information.

-Firebase Storage: Store and manage feed post images uploaded by users.

> _Firestore database structure (Collection)_

![Image](https://github.com/user-attachments/assets/9cc81eed-91b9-4cfa-9e5f-5792fc247ae1)

### **🤝 Team-work Platform Tool**

**✍️ Notion** & **🗂️ Github**

# **References**

**[Calendar]:** https://github.com/afsalkodasseri/KalendarView/tree/master

**[Like&Comment(Instagram clone-coding)]:** https://github.com/raushankrjha/Instagram-Clone-in-Android-Studio-With-Firebase
