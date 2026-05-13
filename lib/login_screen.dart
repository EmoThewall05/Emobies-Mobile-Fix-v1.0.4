import 'package:emobies_app/main.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
// import 'package:local_auth/local_auth.dart'; // removed
import 'dart:async';
import 'dart:convert';
import 'package:crypto/crypto.dart';

const int _kMaxAttempts   = 3;
const int _kLockSeconds   = 30;
const String _kFailsKey   = 'ew_login_fails';
const String _kLockKey    = 'ew_lock_until';

String _hashPassword(String raw) =>
    sha256.convert(utf8.encode(raw.trim())).toString();

class LoginScreen extends StatefulWidget {
  final AuthState auth;
  const LoginScreen({super.key, required this.auth});
  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen>
    with SingleTickerProviderStateMixin {
  static const _storage = FlutterSecureStorage();

  final _passCtrl    = TextEditingController();
  final _focusNode   = FocusNode();
  // final _localAuth = LocalAuthentication(); // removed

  late AnimationController _shakeCtrl;
  late Animation<double>   _shakeAnim;

  String? _error;
  bool    _loading      = false;
  bool    _obscure      = true;
  bool    _bioAvailable = false;
  bool    _hasFaceId    = false;

  int       _fails       = 0;
  DateTime? _lockUntil;
  Timer?    _countdownTimer;
  int       _secondsLeft = 0;

  bool get _locked =>
      _lockUntil != null && DateTime.now().isBefore(_lockUntil!);

  @override
  void initState() {
    super.initState();
    _shakeCtrl = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 400),
    );
    _shakeAnim = Tween<double>(begin: 0, end: 1).animate(
      CurvedAnimation(parent: _shakeCtrl, curve: Curves.elasticIn),
    );
    _loadLockState();
    _checkBiometrics();
  }

  @override
  void dispose() {
    _passCtrl.dispose();
    _focusNode.dispose();
    _shakeCtrl.dispose();
    _countdownTimer?.cancel();
    super.dispose();
  }

  Future<void> _loadLockState() async {
    final failsStr = await _storage.read(key: _kFailsKey);
    final lockStr  = await _storage.read(key: _kLockKey);
    _fails = int.tryParse(failsStr ?? '0') ?? 0;
    if (lockStr != null) {
      final until = DateTime.tryParse(lockStr);
      if (until != null && DateTime.now().isBefore(until)) {
        _lockUntil = until;
        _startCountdown();
      } else {
        await _clearLock();
      }
    }
    if (mounted) setState(() {});
  }

  Future<void> _saveLockState() async {
    await _storage.write(key: _kFailsKey, value: _fails.toString());
    if (_lockUntil != null) {
      await _storage.write(key: _kLockKey, value: _lockUntil!.toIso8601String());
    }
  }

  Future<void> _clearLock() async {
    _fails     = 0;
    _lockUntil = null;
    await _storage.delete(key: _kFailsKey);
    await _storage.delete(key: _kLockKey);
  }

  void _startCountdown() {
    _countdownTimer?.cancel();
    _updateSecondsLeft();
    _countdownTimer = Timer.periodic(const Duration(seconds: 1), (_) {
      if (!_locked) {
        _countdownTimer?.cancel();
        _clearLock().then((_) {
          if (mounted) setState(() { _error = null; });
        });
      } else {
        _updateSecondsLeft();
      }
    });
  }

  void _updateSecondsLeft() {
    if (_lockUntil == null) return;
    final diff = _lockUntil!.difference(DateTime.now()).inSeconds;
    if (mounted) setState(() => _secondsLeft = diff.clamp(0, _kLockSeconds));
  }

  Future<void> _checkBiometrics() async {
    try {
      final canCheck = false;
      final isDeviceSupported = false;
      final biometrics = <dynamic>[];
      if (mounted) {
        setState(() {
          _bioAvailable = biometrics.isNotEmpty;
          _hasFaceId    = false;
        });
      }
      if (_bioAvailable && !_locked) {
        await Future.delayed(const Duration(milliseconds: 600));
        _biometricLogin();
      }
    } catch (_) {}
  }

  Future<void> _biometricLogin() async {
    if (_locked || _loading) return;
    try {
      final authenticated = false; // biometric disabled
      if (authenticated && mounted) {
        final result = await widget.auth.loginWithBiometric();
        if (!result['success'] && mounted) {
          setState(() => _error = result['error'] ?? 'Session expired. Enter password.');
        }
      }
    } on PlatformException catch (e) {
      if (mounted) {
        if (e.code != 'auth_in_progress' && e.code != 'NotAvailable') {
          setState(() => _error = 'Biometric failed — use password.');
        }
      }
    }
  }

  Future<void> _login() async {
    if (_locked || _loading) return;
    final pw = _passCtrl.text.trim();
    if (pw.isEmpty) { setState(() => _error = 'Enter your password'); return; }
    setState(() { _loading = true; _error = null; });
    HapticFeedback.lightImpact();
    try {
      final result = await widget.auth.login(_hashPassword(pw));
      if (result['success'] == true) {
        await _clearLock();
      } else {
        _fails++;
        await _shakeCtrl.forward(from: 0);
        _passCtrl.clear();
        HapticFeedback.heavyImpact();
        if (_fails >= _kMaxAttempts) {
          _lockUntil = DateTime.now().add(const Duration(seconds: _kLockSeconds));
          await _saveLockState();
          _startCountdown();
          setState(() => _error = null);
        } else {
          await _saveLockState();
          final left = _kMaxAttempts - _fails;
          setState(() => _error = 'Wrong password · $left attempt${left == 1 ? '' : 's'} left');
        }
      }
    } on TimeoutException {
      setState(() => _error = 'Server timeout — check your connection');
    } catch (_) {
      setState(() => _error = 'Cannot reach server — check connection');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: EmobiesTheme.bg,
      body: SafeArea(
        child: GestureDetector(
          onTap: () => _focusNode.unfocus(),
          child: SingleChildScrollView(
            padding: const EdgeInsets.symmetric(horizontal: 28),
            child: ConstrainedBox(
              constraints: BoxConstraints(
                minHeight: MediaQuery.of(context).size.height -
                    MediaQuery.of(context).padding.top -
                    MediaQuery.of(context).padding.bottom,
              ),
              child: IntrinsicHeight(
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    const Spacer(),
                    _buildLogo(),
                    const SizedBox(height: 32),
                    _buildPasswordField(),
                    const SizedBox(height: 10),
                    _buildUnlockButton(),
                    const SizedBox(height: 8),
                    if (_bioAvailable) _buildBiometricButton(),
                    const SizedBox(height: 10),
                    _buildFeedback(),
                    const Spacer(),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildLogo() => Column(children: [
    const Text('⬡', style: TextStyle(fontSize: 48, color: EmobiesTheme.orange)),
    const SizedBox(height: 12),
    RichText(text: TextSpan(
      style: GoogleFonts.syne(fontSize: 38, fontWeight: FontWeight.w800, color: EmobiesTheme.text),
      children: const [
        TextSpan(text: 'E', style: TextStyle(color: EmobiesTheme.orange)),
        TextSpan(text: 'mobies'),
      ],
    )),
    const SizedBox(height: 10),
    Wrap(spacing: 6, runSpacing: 6, alignment: WrapAlignment.center, children: [
      _pill('📱 Mobile Repair', EmobiesTheme.green),
      _pill('🔐 TheWall',       EmobiesTheme.orange),
      _pill('🤖 Emowall AI',    EmobiesTheme.purple),
    ]),
    const SizedBox(height: 6),
    Text('KANNUR · DUBAI · DIVIN K.K.',
      style: GoogleFonts.jetBrainsMono(fontSize: 9, color: EmobiesTheme.muted, letterSpacing: 2)),
  ]);

  Widget _buildPasswordField() => AnimatedBuilder(
    animation: _shakeAnim,
    builder: (context, child) {
      final dx = _shakeCtrl.isAnimating
          ? 8 * (0.5 - (_shakeAnim.value - _shakeAnim.value.floor())).abs()
          : 0.0;
      return Transform.translate(
        offset: Offset(dx * (_shakeAnim.value % 2 == 0 ? 1 : -1), 0),
        child: child,
      );
    },
    child: TextField(
      controller:  _passCtrl,
      focusNode:   _focusNode,
      obscureText: _obscure,
      enabled:     !_locked,
      style: const TextStyle(color: EmobiesTheme.text, letterSpacing: 4),
      textAlign: TextAlign.center,
      decoration: InputDecoration(
        hintText: _locked ? 'Locked — wait $_secondsLeft s' : 'Enter password',
        suffixIcon: IconButton(
          icon: Icon(_obscure ? Icons.visibility_off_outlined : Icons.visibility_outlined,
            color: EmobiesTheme.muted, size: 20),
          onPressed: () => setState(() => _obscure = !_obscure),
        ),
      ),
      onSubmitted: (_) => _login(),
    ),
  );

  Widget _buildUnlockButton() => SizedBox(
    width: double.infinity,
    height: 52,
    child: ElevatedButton(
      onPressed: _loading || _locked ? null : _login,
      style: ElevatedButton.styleFrom(
        backgroundColor: EmobiesTheme.orange,
        foregroundColor: Colors.white,
        disabledBackgroundColor: EmobiesTheme.orange.withOpacity(0.35),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(13)),
      ),
      child: _loading
          ? const SizedBox(width: 20, height: 20,
              child: CircularProgressIndicator(color: Colors.white, strokeWidth: 2))
          : _locked
              ? _CountdownText(seconds: _secondsLeft)
              : const Text('⬡  Unlock Emobies',
                  style: TextStyle(fontWeight: FontWeight.w800)),
    ),
  );

  Widget _buildBiometricButton() => SizedBox(
    width: double.infinity,
    height: 48,
    child: OutlinedButton.icon(
      onPressed: _locked ? null : _biometricLogin,
      icon: Icon(_hasFaceId ? Icons.face_outlined : Icons.fingerprint,
        size: 20, color: EmobiesTheme.text2),
      label: Text(
        _hasFaceId ? 'Face ID Login' : 'Fingerprint Login',
        style: const TextStyle(color: EmobiesTheme.text2),
      ),
      style: OutlinedButton.styleFrom(
        side: const BorderSide(color: EmobiesTheme.border),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(13)),
      ),
    ),
  );

  Widget _buildFeedback() {
    if (_locked) return _LockBanner(secondsLeft: _secondsLeft);
    if (_error != null) {
      return Padding(
        padding: const EdgeInsets.only(top: 4),
        child: Text(_error!,
          style: GoogleFonts.jetBrainsMono(fontSize: 11, color: EmobiesTheme.red),
          textAlign: TextAlign.center),
      );
    }
    return const SizedBox.shrink();
  }

  Widget _pill(String label, Color color) => Container(
    padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
    decoration: BoxDecoration(
      color: color.withOpacity(0.1),
      border: Border.all(color: color.withOpacity(0.3)),
      borderRadius: BorderRadius.circular(20),
    ),
    child: Text(label,
      style: GoogleFonts.jetBrainsMono(fontSize: 9, color: color, fontWeight: FontWeight.w700)),
  );
}

class _CountdownText extends StatelessWidget {
  final int seconds;
  const _CountdownText({required this.seconds});
  @override
  Widget build(BuildContext context) => Row(
    mainAxisAlignment: MainAxisAlignment.center,
    children: [
      const Icon(Icons.lock_clock_outlined, size: 16, color: Colors.white70),
      const SizedBox(width: 8),
      Text('Locked · ${seconds}s',
        style: const TextStyle(fontWeight: FontWeight.w700, color: Colors.white70)),
    ],
  );
}

class _LockBanner extends StatelessWidget {
  final int secondsLeft;
  const _LockBanner({required this.secondsLeft});
  @override
  Widget build(BuildContext context) {
    final frac = secondsLeft / _kLockSeconds;
    return Container(
      margin: const EdgeInsets.only(top: 10),
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: EmobiesTheme.red.withOpacity(0.08),
        border: Border.all(color: EmobiesTheme.red.withOpacity(0.3)),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Column(children: [
        Row(mainAxisAlignment: MainAxisAlignment.center, children: [
          const Icon(Icons.lock_outline, size: 14, color: EmobiesTheme.red),
          const SizedBox(width: 6),
          Text('Too many attempts — wait ${secondsLeft}s',
            style: GoogleFonts.jetBrainsMono(fontSize: 11, color: EmobiesTheme.red)),
        ]),
        const SizedBox(height: 8),
        ClipRRect(
          borderRadius: BorderRadius.circular(4),
          child: LinearProgressIndicator(
            value: frac,
            backgroundColor: EmobiesTheme.red.withOpacity(0.15),
            valueColor: const AlwaysStoppedAnimation(EmobiesTheme.red),
            minHeight: 3,
          ),
        ),
      ]),
    );
  }
}
