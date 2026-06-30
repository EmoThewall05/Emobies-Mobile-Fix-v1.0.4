# Emobies 🔧

**Mobile Repair. Reimagined.**

Built from Dubai 🇦🇪 · Powered by Seven Brains 🧠 · Made for Kerala 🌿

---

## What is Emobies?

Emobies is a full-stack mobile repair ecosystem — not just an app. It connects customers, delivery agents, service centers, supervisors, and admins on one platform.

**The Problem:** Broken phone. No trusted repair shop. No price transparency. No tracking.

**The Solution:** Emobies — register a complaint, get pickup, track repair, pay securely, earn EmoCoins.

---

## Architecture

┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Flutter   │────▶│   Node.js   │────▶│   Supabase  │
│   (Android) │◄────│   (Render)  │◄────│  (Realtime) │
└─────────────┘     └─────────────┘     └─────────────┘
│                   │                   │
▼                   ▼                   ▼
┌─────────┐        ┌─────────┐        ┌─────────┐
│ Emo AI  │        │Razorpay │        │Telegram │
│(Worker) │        │(Payment)│        │(Alerts) │
└─────────┘        └─────────┘        └─────────┘


---

## Roles

| Role | What They Do |
|------|-------------|
| **Customer** | Register complaints, track status, chat, pay, earn EmoCoins |
| **Delivery** | Pickup & drop devices, upload photos, confirm locations |
| **Service Center** | Receive devices, diagnose, repair, update status |
| **Supervisor** | Assign complaints, manage staff, monitor chats |
| **Admin** | Full analytics, staff management, all complaints |

---

## Features

- **🔧 Smart Complaints** — Register with device details, issue type, photos
- **🚚 Live Tracking** — GPS-based pickup & delivery with Google Maps
- **💬 AI-Monitored Chat** — Per-complaint chat with Emo AI oversight
- **🪙 EmoCoins** — Earn 1 coin per ₹100 spent, redeem up to 50% of bill
- **🤖 Emo AI** — Customer support, no hallucination, Malayalam + English
- **🔐 Biometric Auth** — Fingerprint/face unlock for staff
- **💳 Razorpay** — Secure payments with instant verification
- **📊 Admin Analytics** — Real-time dashboards, staff performance

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | Flutter 3.27.0, Dart 3.6 |
| Backend | Node.js + Express (Render) |
| Database | Supabase (PostgreSQL + Realtime) |
| AI | Cloudflare Workers + Llama 3.1 |
| Auth | JWT + SHA-256 + Biometric |
| Payment | Razorpay |
| Maps | Google Maps Flutter |
| Storage | Supabase Storage |

---

## Security

- Passwords hashed with SHA-256 + salt
- Rate limiting (5 attempts / minute)
- JWT tokens with refresh mechanism
- Biometric authentication for staff
- Telegram/Discord alerts for staff logins

---

## Build

```bash
# Clone
git clone https://github.com/EmoThewall05/Emobies-Mobile-Fix-v1.0.4.git
cd Emobies-Mobile-Fix-v1.0.4

# Dependencies
flutter pub get

# Build APK
flutter build apk --release

# Build AAB (Play Store)
flutter build appbundle --release

| Variable            | Purpose             |
| ------------------- | ------------------- |
| `API_BASE`          | Backend URL         |
| `SUPABASE_URL`      | Database URL        |
| `SUPABASE_ANON_KEY` | Database key        |
| `EMO_KEY_BASE`      | EmoKey verification |
| `CF_AI_BASE`        | Emo AI worker       |
| `RAZORPAY_KEY`      | Payment gateway     |

Emo AI
"I don't know" > "I guess"
Emo AI is the customer support brain. Built on Cloudflare Workers with Llama 3.1. It knows:
EmoCoin rules
Complaint status checks
General support
Never makes up prices or repair times

Vision
"Emobies is not just a repair app. It's the foundation of an ecosystem — Emobies + Emowall + TheWall = One Platform."
Creator
Thewin (Dwin 05)
Built from Dubai 🇦🇪 | Powered by Seven Brains 🧠 | Made for Kerala 🌿
GitHub: @EmoThewall05
License
MIT
Follow the flow 🦋
