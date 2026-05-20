# 🚌 JTMS — Jeepney Terminal Management System

> An Android mobile application for managing jeepney terminal operations in the Philippines.

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
![Backend](https://img.shields.io/badge/Backend-PHP%208.2-777BB4?logo=php&logoColor=white)
![Database](https://img.shields.io/badge/Database-MariaDB%2010.4-003545?logo=mariadb&logoColor=white)
![Version](https://img.shields.io/badge/Version-2.5.0-1B4332)

---

## 📋 Overview

JTMS enables terminal staff and administrators to:

- Track jeepney departures and returns in real time
- Manage the full fleet (drivers, plate numbers, capacity)
- Record trip fares and generate analytics
- View detailed trip reports filtered by date and jeepney
- Manage staff accounts with secure login
- View personal profile and change passwords

---

## 🛠️ Technology Stack

| Layer | Technology |
|---|---|
| **Mobile App** | Android (Java), XML Layouts, Material Components |
| **Navigation** | Fragment-based with BottomNavigationView |
| **Networking** | Volley HTTP Library |
| **Backend** | PHP 8.2 via XAMPP Apache |
| **Database** | MariaDB 10.4.32 via XAMPP (`jtms_db`) |
| **Session** | Custom `SessionManager` singleton (in-memory) |
| **Build** | Android Studio, Gradle |

---

## 📁 Project Structure

```
jtms30032026/
├── app/src/main/java/com/example/jtms30032026/
│   ├── MainActivity.java           # Host activity, bottom nav controller
│   ├── AppConfig.java              # BASE_URL config for XAMPP server IP
│   ├── SessionManager.java         # Singleton: stores username + isAdmin flag
│   ├── LoginFragment.java          # Authentication screen
│   ├── HomeFragment.java           # Dashboard — fleet overview & stats
│   ├── JeepneyFragment.java        # Fleet management & trip dispatch
│   ├── AnalyticsFragment.java      # Charts and fare visualizations
│   ├── ReportFragment.java         # Trip reports with date/jeepney filter
│   ├── StaffFragment.java          # Staff CRUD (admin only)
│   └── ProfileFragment.java        # Profile, change password, log out
│
├── app/src/main/res/
│   ├── layout/
│   │   ├── fragment_profile.xml
│   │   ├── dialog_change_password.xml
│   │   └── dialog_confirm_logout.xml
│   ├── drawable/
│   │   ├── ic_logout.xml
│   │   ├── btn_outline_green_rounded.xml
│   │   └── circle_avatar_bg.xml
│   ├── xml/
│   │   └── network_security_config.xml  # Whitelists server IP for HTTP traffic
│   └── menu/
│       └── bottom_nav_menu.xml
│
└── htdocs/crud-android-jtms/       # PHP backend (XAMPP)
    ├── logincheck.php              # Staff authentication
    ├── profileread.php             # Fetch staff profile by username
    ├── profilepassword.php         # Change password endpoint
    ├── staffcreate.php             # Create new staff account
    └── ...                         # Other CRUD endpoints
```

---

## 🗄️ Database Schema

**Database:** `jtms_db` on MariaDB 10.4.32

### `jeepney`
| Column | Type | Description |
|---|---|---|
| `jeepney_id` | INT PK AUTO | Unique jeepney ID |
| `driver_name` | VARCHAR(100) | Driver full name |
| `plate_number` | VARCHAR(100) | Plate number |
| `capacity` | INT | Passenger capacity |

### `jeepney_staff`
| Column | Type | Description |
|---|---|---|
| `staff_id` | INT PK AUTO | Unique staff ID |
| `staff_fname` | VARCHAR(100) | First name |
| `staff_lname` | VARCHAR(100) | Last name |
| `staff_username` | VARCHAR(50) | Login username |
| `staff_password` | VARCHAR(255) | **bcrypt hashed** password |
| `created_at` | DATETIME | Account creation timestamp |

### `jeepney_status`
| Column | Type | Description |
|---|---|---|
| `status_id` | INT PK AUTO | Status record ID |
| `jeepney_id` | INT FK | References `jeepney` |
| `status` | VARCHAR(20) | `IN TERMINAL` or `ON ROAD` |
| `last_activity` | VARCHAR(100) | Human-readable last activity |
| `total_trips` | INT | Cumulative trip count |
| `total_fare` | DECIMAL(10,2) | Cumulative fare collected |

### `jeepney_trips`
| Column | Type | Description |
|---|---|---|
| `trip_id` | INT PK AUTO | Unique trip ID |
| `jeepney_id` | INT FK | References `jeepney` |
| `depart_time` | DATETIME | Departure timestamp |
| `return_time` | DATETIME | Return timestamp (nullable) |
| `trip_date` | DATE | Date of trip |
| `departed_by` | VARCHAR(100) | Username of staff who dispatched |
| `departed_by_staff_id` | INT FK | References `jeepney_staff` |
| `returned_by` | VARCHAR(100) | Username of staff who returned |
| `returned_by_staff_id` | INT FK | References `jeepney_staff` |

### `fare_settings`
| Column | Type | Description |
|---|---|---|
| `id` | INT PK AUTO | Settings ID |
| `fare_price` | DECIMAL(10,2) | Current base fare (default ₱20.00) |

---

## ⚙️ Setup & Installation

### Prerequisites
- [Android Studio](https://developer.android.com/studio) (latest stable)
- [XAMPP](https://www.apachefriends.org/) with Apache + MySQL enabled
- Android device or emulator (API 26+)

### 1. Database Setup

1. Open **phpMyAdmin** → `http://localhost/phpmyadmin`
2. Create a database named `jtms_db`
3. Import `jtms_db.sql` (Export tab → Go to get a backup, or use the provided SQL file)
4. Run `migration_v2_5_0.sql` in the SQL tab to apply schema hardening

### 2. Backend Setup

1. Copy all PHP files to `C:\xampp\htdocs\crud-android-jtms\`
2. Make sure Apache and MySQL are running in the XAMPP control panel
3. Test an endpoint: `http://localhost/crud-android-jtms/logincheck.php`

### 3. Android App Setup

> ⚠️ **You must update your server IP in TWO places every time it changes.**

#### Step 1 — Find your machine's local IP
Open Command Prompt and run:
```
ipconfig
```
Look for **IPv4 Address** under your active network adapter (e.g. `192.168.100.61`).

#### Step 2 — Update `AppConfig.java`
```java
public static final String BASE_URL = "http://YOUR_IP_HERE/crud-android-jtms/";
// Example:
public static final String BASE_URL = "http://192.168.100.61/crud-android-jtms/";
```

#### Step 3 — Update `res/xml/network_security_config.xml`
Android blocks unencrypted HTTP traffic by default (API 28+). This file whitelists your server IP so Volley can connect over plain HTTP. **It must match the IP in AppConfig.java exactly.**

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">YOUR_IP_HERE</domain>
    </domain-config>
</network-security-config>
```

> If you skip this step the app will silently fail to connect — Volley will return a network error with no clear explanation why.

#### Step 4 — Run the app
1. Connect your Android device (enable USB Debugging) or start an emulator
2. Make sure your device is on the **same Wi-Fi network** as the XAMPP machine
3. Click **Run** in Android Studio

### 4. Default Login

| Account | Username | Password |
|---|---|---|
| Admin | `admin` | `admin123` |
| Staff | See `jeepney_staff` table | Your set password |

> **Note:** The admin account is hardcoded in `LoginFragment.java` and is not stored in the database.

---

## 🔒 Security

As of v2.5.0 the following security measures are in place:

- **Password hashing** — all staff passwords are stored as bcrypt hashes using PHP's `password_hash()`. Login uses `password_verify()`.
- **Prepared statements** — all database queries use `$conn->prepare()` with `bind_param()` to prevent SQL injection.
- **Auto-upgrade** — if any legacy plain-text password is detected on login, it is automatically hashed and saved.
- **No credentials in APK** — the admin password lives only in `LoginFragment.java` (consider moving to a secure config for production).

---

## 📱 App Features

| Fragment | Access | Description |
|---|---|---|
| **Login** | Public | Username + password authentication |
| **Home** | Admin + Staff | Dashboard — fleet statuses, trip counts, fare totals |
| **Jeepney** | Admin + Staff | Dispatch jeepneys (depart / return), view fleet |
| **Analytics** | Admin + Staff | Charts for trips and fare data |
| **Reports** | Admin + Staff | Trip history, filterable by date and jeepney |
| **Staff** | Admin only | Create, edit, delete staff accounts |
| **Profile** | Admin + Staff | View account info, change password, log out |

---

## 🚧 Known Issues

- Debug toast messages may still appear in `ProfileFragment` — remove before release
- Admin cannot change password from the Profile screen (no DB record exists)
- No internet/server connectivity check before Volley requests — shows generic error if XAMPP is offline
- Bottom nav visual selection may not reset correctly after logout on all devices
- **IP mismatch** — if `AppConfig.java` and `network_security_config.xml` have different IPs, the app connects to the wrong address or gets blocked silently. Always update both together.

---

## 🗺️ Roadmap

- [ ] Profile photo upload
- [ ] Session timeout after 30 minutes of inactivity
- [ ] Remember Me option via SharedPreferences
- [ ] Failed login attempt lockout (5 attempts → 15 min lock)
- [ ] Passenger count per trip
- [ ] Shift/schedule system for staff
- [ ] MVVM architecture refactor
- [ ] Centralized Volley singleton queue
- [ ] Dark mode support
- [ ] Push notifications for long ON ROAD jeepneys

---

## 👥 Development

**Package:** `com.example.jtms30032026`  
**PHP files:** `C:\xampp\htdocs\crud-android-jtms\`  
**Always test PHP endpoints in browser first:** `http://localhost/crud-android-jtms/[file].php`

### Changing the server IP checklist
When your machine IP changes (new network, router restart, etc.):
- [ ] Run `ipconfig` to get the new IPv4 address
- [ ] Update `BASE_URL` in `AppConfig.java`
- [ ] Update `<domain>` in `res/xml/network_security_config.xml`
- [ ] Rebuild and reinstall the app

---

## 📄 License

This project is developed for academic/operational use. All rights reserved.

---

*JTMS v2.5.0 — May 2026*
