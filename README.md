# **WGeplant — Calendar & Shared Tasks (Android)**

WGeplant is an Android app for coordinating **events** and **shared tasks** within a group (household/team). Create a group, invite members, plan together, and see everyone’s availability.

---

## **Highlights**
- **Calendar:** month/day views, quick create/edit/delete  
- **Tasks:** assignees, due dates, list & detail views  
- **Groups:** join via invite code, manage members  

---

## **Tech**
Kotlin · Jetpack Compose · ViewModel · Coroutines · Hilt · Retrofit/OkHttp · Kotlinx Serialization · Gradle (KTS)

---

## **Setup**
**Requirements:** Android Studio (current), Android SDK 34+, JDK 17+

**Server URL** — set in your local `gradle.properties`:

    BASE_URL=https://your-server.example.com:443

**Firebase** — add your config file:

    app/google-services.json

**Certificate pinning** — if your environment requires it:

    app/src/main/res/xml/network_security_config.xml
    app/src/main/res/raw/   (place certs here, e.g., server.crt / server.pem)
