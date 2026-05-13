# 🔧 Emobies
**Your Trusted Mobile Repair Partner**

Built from Dubai 🇦🇪 · Powered by Seven Brains 🧠 · Made for Kerala 🌿

[![Flutter](https://img.shields.io/badge/Flutter-3.27.0-02569B?logo=flutter)](https://flutter.dev)
[![Dart](https://img.shields.io/badge/Dart-3.6.0-blue?logo=dart)](https://dart.dev)
[![Node.js](https://img.shields.io/badge/Node.js-Express-green?logo=node.js)](https://nodejs.org)
[![MongoDB](https://img.shields.io/badge/MongoDB-Atlas-green?logo=mongodb)](https://www.mongodb.com)
[![Google Play](https://img.shields.io/badge/Google%20Play-Publishing-orange?logo=google-play)](https://play.google.com)
[![Vercel](https://img.shields.io/badge/Deployed-Vercel-black?logo=vercel)](https://vercel.com)

---

## 📱 What is Emobies?

Emobies is a **full-stack mobile repair service platform** — connecting customers with trusted repair technicians, delivery boys, and service centres.

**Built entirely on a phone 📱** using Termux + Acode, backend deployed on Railway.

---

## ✨ Features

| Feature | Description |
|---------|-------------|
| 🔧 **Repair Complaints** | Submit, track, and manage repair requests |
| 💬 **Live Chat** | Chat per complaint with technician |
| 🚚 **Delivery Boy** | Pickup & delivery tracking |
| 🏪 **Service Centre** | Assign complaints to service centres |
| 🪙 **EmoCoins** | Loyalty rewards system |
| 🤖 **AI Chatbot** | Gemini-powered repair assistant |
| 👑 **Admin Panel** | Full superadmin + staff management |
| 🔐 **Secure Auth** | JWT-based login + register |

---

## 🏗️ Architecture

```
Flutter App (Android)
      ↓
Node.js + Express API (Railway)
      ↓
MongoDB (In-memory / Atlas)
      ↓
Gemini AI (Google)
```

---

## 🚀 Backend API

**Base URL:** `https://emobies-ap-135-production.up.railway.app`

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/auth/register` | POST | Register user |
| `/api/auth/login` | POST | Login user |
| `/api/complaints` | GET/POST | Manage complaints |
| `/api/chat/:id` | GET/POST | Chat per complaint |
| `/api/emocoins/balance` | GET | EmoCoins balance |
| `/api/ai/chat` | POST | Gemini AI chat |

---

## 👑 Superadmin Credentials

```
Phone: 9847842172
Password: Emobies@2026!
```

---

## 📦 Tech Stack

| Layer | Technology |
|-------|-----------|
| **Frontend** | Flutter 3.27.0 (Dart) |
| **Backend** | Node.js + Express |
| **AI** | Gemini API (Google) |
| **Database** | MongoDB (Atlas) |
| **Deployment** | Railway (Backend) + Vercel (Web) |
| **CI/CD** | GitHub Actions |
| **Store** | Google Play Console |

---

## 🚀 Build & Deploy

### Prerequisites
- Flutter 3.27.0
- Dart 3.6.0
- Android SDK (API 21+)

### Install & Build

```bash
# Clone repo
git clone https://github.com/EmoThewall05/Emobies-Mobile-Fix-v1.0.4.git
cd Emobies-Mobile-Fix-v1.0.4

# Install dependencies
flutter pub get

# Build APK
flutter build apk --release

# Build AppBundle (Play Store)
flutter build appbundle --release
```

### Deploy Backend (Railway)

```bash
# Set Railway secrets
railway variable add DATABASE_URL=your_mongodb_url
railway variable add GEMINI_API_KEY=your_gemini_key
railway variable add JWT_SECRET=your_jwt_secret

# Deploy
railway deploy
```

---

## 🌐 Links

| Link | Purpose |
|------|---------|
| 🔧 [Google Play Store](https://play.google.com) | Download App (Publishing) |
| 🌍 [Web Version](https://emobies-ap-135.vercel.app) | Web Dashboard |
| 📄 [Privacy Policy](https://emobies05.github.io/public-/privacy-policy.html) | Legal |

---

## 🦋 Sister App

- **Emowall AI 2.0** — Multi-generational AI safety companion

---

## 🧠 Powered by Seven Brains

Same multi-AI architecture as Emowall AI 2.0:
- **Claude** — Architecture & Code
- **Gemini** — Voice & In-App AI
- **ChatGPT** — Content & Copy
- **Cursor** — IDE Coding
- **GitHub Copilot** — CI/CD & Tests
- **DeepSeek** — Algorithms
- **Perplexity** — Research

---

## 🦋 Vision

> **"Emobies is not just a repair app. It's the foundation of an ecosystem — Emobies + Emowall + TheWall = One Platform."**

---

## 📱 App Info

- **Package:** `com.nxtbit.emobies_24`
- **Min SDK:** 21
- **Target SDK:** 35
- **Current Version:** 1.0.4

---

## 👤 Creator

**Thewin (Dwin 05)**  
Built from Dubai 🇦🇪 | Powered by Seven Brains 🧠 | Made for Kerala 🌿

GitHub: [@EmoThewall05](https://github.com/EmoThewall05)

---

## 📝 License

This project is built with ❤️

---

**Follow the flow** 🦋
