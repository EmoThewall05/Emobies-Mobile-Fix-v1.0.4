import 'dart:async';
import 'dart:convert';
import 'dart:developer';
import 'package:http/http.dart' as http;
import 'package:local_auth/local_auth.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../../config/constants.dart';
import '../models/user_model.dart';

class AuthService {
  static const _storage = FlutterSecureStorage(
    aOptions: AndroidOptions(encryptedSharedPreferences: true),
  );
  static const _tokenKey = 'ew_token';
  static const _roleKey = 'ew_role';
  static const _userKey = 'ew_user';
  static const _biometricKey = 'ew_biometric';

  final LocalAuthentication _localAuth = LocalAuthentication();
  UserModel? _currentUser;

  UserModel? get currentUser => _currentUser;
  String? get token => _currentUser?.emoKey;

  // Initialize auth state
  Future<bool> init() async {
    try {
      final storedToken = await _storage.read(key: _tokenKey);
      final storedRole = await _storage.read(key: _roleKey);
      final storedUser = await _storage.read(key: _userKey);

      if (storedToken != null && storedUser != null) {
        _currentUser = UserModel.fromJson(jsonDecode(storedUser));
        final isValid = await _validateToken(storedToken);
        if (!isValid) {
          await logout();
          return false;
        }
        return true;
      }
      return false;
    } catch (e) {
      log('Auth init error: $e');
      return false;
    }
  }

  Future<bool> _validateToken(String token) async {
    try {
      final res = await http.get(
        Uri.parse('${AppConstants.apiBase}/api/user/profile'),
        headers: {'Authorization': 'Bearer $token'},
      ).timeout(const Duration(seconds: 8));
      return res.statusCode == 200;
    } catch (_) {
      return true;
    }
  }

  // Login with password
  Future<Map<String, dynamic>> login(String phone, String password) async {
    try {
      final res = await http.post(
        Uri.parse('${AppConstants.apiBase}/api/login'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({'phone': phone, 'password': password}),
      ).timeout(const Duration(seconds: 10));

      final data = jsonDecode(res.body) as Map<String, dynamic>;

      if (res.statusCode == 200 && data['token'] != null) {
        final role = data['role'] as String? ?? 'customer';
        final userData = data['user'] as Map<String, dynamic>? ?? {};

        await _storage.write(key: _tokenKey, value: data['token']);
        await _storage.write(key: _roleKey, value: role);
        await _storage.write(key: _userKey, value: jsonEncode(userData));

        _currentUser = UserModel.fromJson(userData);

        // Log to Telegram/Discord for staff logins
        if (role != 'customer') {
          await _logStaffLogin(phone, role);
        }

        return {'success': true, 'role': role};
      }
      return {'success': false, 'error': data['error'] ?? 'Invalid credentials'};
    } catch (e) {
      log('Login error: $e');
      return {'success': false, 'error': 'Cannot reach server. Check connection.'};
    }
  }

  // Sign up (customer only)
  Future<Map<String, dynamic>> signUp({
    required String name,
    required String phone,
    required String password,
    String? email,
  }) async {
    try {
      final res = await http.post(
        Uri.parse('${AppConstants.apiBase}/api/register'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'name': name,
          'phone': phone,
          'email': email,
          'password': password,
          'role': 'customer',
        }),
      ).timeout(const Duration(seconds: 10));

      final data = jsonDecode(res.body);

      if (res.statusCode == 200 && data['success'] == true) {
        return {'success': true};
      }
      return {'success': false, 'error': data['error'] ?? 'Registration failed'};
    } catch (e) {
      log('SignUp error: $e');
      return {'success': false, 'error': 'Cannot reach server.'};
    }
  }

  // Biometric authentication
  Future<bool> isBiometricAvailable() async {
    try {
      final canCheck = await _localAuth.canCheckBiometrics;
      final isAvailable = await _localAuth.isDeviceSupported();
      return canCheck && isAvailable;
    } catch (e) {
      return false;
    }
  }

  Future<bool> authenticateWithBiometric({String reason = 'Authenticate to access Emobies'}) async {
    try {
      final isEnabled = await _storage.read(key: _biometricKey);
      if (isEnabled != 'true') return false;

      final didAuth = await _localAuth.authenticate(
        localizedReason: reason,
        authMessages: const [
          AndroidAuthMessages(
            signInTitle: 'Emobies Authentication',
            cancelButton: 'Cancel',
            biometricHint: 'Verify your identity',
            biometricNotRecognized: 'Not recognized, try again',
            biometricSuccess: 'Success!',
            deviceCredentialsRequiredTitle: 'Device credential required',
            deviceCredentialsSetupDescription: 'Please set up device credentials',
            goToSettingsButton: 'Go to Settings',
            goToSettingsDescription: 'Please set up biometric in Settings',
          ),
        ],
        options: const AuthenticationOptions(
          biometricOnly: false,
          stickyAuth: true,
          sensitiveTransaction: true,
        ),
      );
      return didAuth;
    } catch (e) {
      log('Biometric error: $e');
      return false;
    }
  }

  Future<void> setBiometricEnabled(bool enabled) async {
    await _storage.write(key: _biometricKey, value: enabled ? 'true' : 'false');
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(AppConstants.prefBiometricEnabled, enabled);
  }

  Future<bool> isBiometricEnabled() async {
    final val = await _storage.read(key: _biometricKey);
    return val == 'true';
  }

  // Staff login (supervisor, delivery boy, service center)
  Future<Map<String, dynamic>> staffLogin({
    required String email,
    required String password,
    required String emoKey,
  }) async {
    try {
      final res = await http.post(
        Uri.parse('${AppConstants.apiBase}/api/staff/login'),
        headers: {
          'Content-Type': 'application/json',
          'X-Emo-Key': emoKey,
        },
        body: jsonEncode({'email': email, 'password': password}),
      ).timeout(const Duration(seconds: 10));

      final data = jsonDecode(res.body) as Map<String, dynamic>;

      if (res.statusCode == 200 && data['token'] != null) {
        final role = data['role'] as String? ?? '';
        final userData = data['user'] as Map<String, dynamic>? ?? {};

        await _storage.write(key: _tokenKey, value: data['token']);
        await _storage.write(key: _roleKey, value: role);
        await _storage.write(key: _userKey, value: jsonEncode(userData));

        _currentUser = UserModel.fromJson(userData);
        await _logStaffLogin(email, role);

        return {'success': true, 'role': role};
      }
      return {'success': false, 'error': data['error'] ?? 'Invalid credentials'};
    } catch (e) {
      log('Staff login error: $e');
      return {'success': false, 'error': 'Server unreachable'};
    }
  }

  // Super admin login
  Future<Map<String, dynamic>> superAdminLogin({
    required String password,
    required String secretKey,
  }) async {
    try {
      final res = await http.post(
        Uri.parse('${AppConstants.apiBase}/api/admin/login'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({'password': password, 'secret_key': secretKey}),
      ).timeout(const Duration(seconds: 10));

      final data = jsonDecode(res.body) as Map<String, dynamic>;

      if (res.statusCode == 200 && data['token'] != null) {
        await _storage.write(key: _tokenKey, value: data['token']);
        await _storage.write(key: _roleKey, value: 'super_admin');
        await _storage.write(key: _userKey, value: jsonEncode(data['user'] ?? {}));
        _currentUser = UserModel.fromJson(data['user'] ?? {});
        return {'success': true, 'role': 'super_admin'};
      }
      return {'success': false, 'error': data['error'] ?? 'Access denied'};
    } catch (e) {
      log('Admin login error: $e');
      return {'success': false, 'error': 'Server unreachable'};
    }
  }

  // Get stored role
  Future<String?> getRole() async {
    return _storage.read(key: _roleKey);
  }

  // Get auth headers
  Future<Map<String, String>> getAuthHeaders() async {
    final token = await _storage.read(key: _tokenKey);
    return {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer $token',
    };
  }

  // Logout
  Future<void> logout() async {
    try {
      final token = await _storage.read(key: _tokenKey);
      if (token != null) {
        await http.post(
          Uri.parse('${AppConstants.apiBase}/api/logout'),
          headers: {'Authorization': 'Bearer $token'},
        );
      }
    } catch (_) {}
    _currentUser = null;
    await _storage.deleteAll();
  }

  // Log staff login to monitoring
  Future<void> _logStaffLogin(String identifier, String role) async {
    try {
      final msg = '📱 *Emobies Staff Login*\n'
          '👤 $identifier\n'
          '🔑 Role: $role\n'
          '⏰ ${DateTime.now().toIso8601String()}';

      // Telegram
      if (AppConstants.telegramBotToken.isNotEmpty) {
        await http.post(
          Uri.parse('https://api.telegram.org/bot${AppConstants.telegramBotToken}/sendMessage'),
          headers: {'Content-Type': 'application/json'},
          body: jsonEncode({
            'chat_id': AppConstants.telegramChatId,
            'text': msg,
            'parse_mode': 'Markdown',
          }),
        );
      }

      // Discord
      if (AppConstants.discordWebhookUrl.isNotEmpty) {
        await http.post(
          Uri.parse(AppConstants.discordWebhookUrl),
          headers: {'Content-Type': 'application/json'},
          body: jsonEncode({
            'content': msg,
            'username': 'Emobies Monitor',
          }),
        );
      }
    } catch (e) {
      log('Monitor log error: $e');
    }
  }
}