class AppConstants {
  static const String apiBase = 'https://emobies-backend.onrender.com';
  static const String supabaseUrl = String.fromEnvironment('SUPABASE_URL');
  static const String supabaseAnonKey = String.fromEnvironment('SUPABASE_ANON_KEY');
  static const String emoKeyBase = 'https://emo-key.vercel.app';
  static const String cfAiBase = 'https://emobies-ai.meradivin.workers.dev';
  
  static const String telegramBotToken = String.fromEnvironment('TELEGRAM_BOT_TOKEN');
  static const String telegramChatId = String.fromEnvironment('TELEGRAM_CHAT_ID');
  static const String discordWebhookUrl = String.fromEnvironment('DISCORD_WEBHOOK_URL');
  
  // Auth
  static const String adminPhone = '9847842172';
  static const String jwtSecret = String.fromEnvironment('JWT_SECRET');
}

  // EmoCoin exchange rates
  static const int coinsForCryptoExchange = 1000;
  static const double cryptoExchangeRate = 0.001;
