class AppConstants {
  AppConstants._();

  // API Endpoints
  static const String apiBase = String.fromEnvironment(
    'API_BASE',
    defaultValue: 'https://emobies-mobile-fix-v1-0-4.onrender.com',
  );
  static const String emoKeyBase = String.fromEnvironment(
    'EMO_KEY_BASE',
    defaultValue: 'https://emo-key.vercel.app',
  );
  static const String cloudflareAiBase = String.fromEnvironment(
    'CF_AI_BASE',
    defaultValue: 'https://emobies-ai.meradivin.workers.dev',
  );
  static const String supabaseUrl = String.fromEnvironment('SUPABASE_URL');
  static const String supabaseAnonKey = String.fromEnvironment('SUPABASE_ANON_KEY');

  // Telegram/Discord Bot
  static const String telegramBotToken = String.fromEnvironment('TELEGRAM_BOT_TOKEN');
  static const String telegramChatId = String.fromEnvironment('TELEGRAM_CHAT_ID');
  static const String discordWebhookUrl = String.fromEnvironment('DISCORD_WEBHOOK_URL');

  // App Info
  static const String appName = 'Emobies';
  static const String appVersion = '2.0.0';
  static const String packageName = 'com.nxtbit.emobies_24';

  // EmoCoins
  static const int dailyLoginCoins = 1;
  static const int coinToRupeeRate = 1;
  static const int coinsForCryptoExchange = 500;
  static const double coinRupeeValue = 1.0;

  // Auth
  static const int maxLoginAttempts = 3;
  static const Duration lockoutDuration = Duration(seconds: 30);
  static const Duration tokenRefreshBuffer = Duration(minutes: 5);

  // Roles
  static const String roleSuperAdmin = 'super_admin';
  static const String roleSupervisor = 'supervisor';
  static const String roleDeliveryBoy = 'delivery_boy';
  static const String roleServiceCenter = 'service_center';
  static const String roleCustomer = 'customer';

  // Complaint Status
  static const String statusPending = 'pending';
  static const String statusAccepted = 'accepted';
  static const String statusAssigned = 'assigned';
  static const String statusPickupOngoing = 'pickup_ongoing';
  static const String statusReachedCustomer = 'reached_customer';
  static const String statusPhoneCollected = 'phone_collected';
  static const String statusDroppedServiceCenter = 'dropped_sc';
  static const String statusRepairOngoing = 'repair_ongoing';
  static const String statusRepairCompleted = 'repair_completed';
  static const String statusPaymentPending = 'payment_pending';
  static const String statusPaid = 'paid';
  static const String statusReadyForDelivery = 'ready_for_delivery';
  static const String statusHandoverToDelivery = 'handover_delivery';
  static const String statusOutForDelivery = 'out_for_delivery';
  static const String statusDelivered = 'delivered';
  static const String statusCompleted = 'completed';
  static const String statusCancelled = 'cancelled';

  // Image Upload
  static const int maxImageSize = 5 * 1024 * 1024; // 5MB
  static const List<String> allowedImageTypes = ['jpg', 'jpeg', 'png'];
  static const int maxImagesPerUpload = 5;

  // Location
  static const Duration locationUpdateInterval = Duration(seconds: 10);
  static const double defaultMapZoom = 15;

  // Cache
  static const Duration cacheExpiry = Duration(hours: 24);
  static const String prefUserKey = 'emobies_user';
  static const String prefTokenKey = 'emobies_token';
  static const String prefRoleKey = 'emobies_role';
  static const String prefFirstLaunch = 'emobies_first_launch';
  static const String prefDailyCoinDate = 'emobies_daily_coin_date';
  static const String prefEmoCoins = 'emobies_coins';
  static const String prefBiometricEnabled = 'emobies_biometric';
}

class AppAssets {
  AppAssets._();
  static const String logo = 'assets/images/emobies_logo.png';
  static const String emptyState = 'assets/images/empty_state.svg';
  static const String noInternet = 'assets/images/no_internet.svg';
  static const String coinAnimation = 'assets/animations/coin.json';
  static const String successAnimation = 'assets/animations/success.json';
}