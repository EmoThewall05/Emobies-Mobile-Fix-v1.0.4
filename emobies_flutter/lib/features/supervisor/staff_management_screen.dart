import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import '../../config/routes.dart';
import '../../config/theme.dart';
import '../../core/services/api_service.dart';
import '../../core/services/auth_service.dart';

class StaffManagementScreen extends StatefulWidget {
  const StaffManagementScreen({super.key});

  @override
  State<StaffManagementScreen> createState() => _StaffManagementScreenState();
}

class _StaffManagementScreenState extends State<StaffManagementScreen> {
  final _api = ApiService(AuthService());
  List<Map<String, dynamic>> _staff = [];
  bool _loading = true;
  String _filter = 'all';

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    try {
      final staff = await _api.getStaff(
        role: _filter == 'all' ? null : _filter,
      );
      setState(() { _staff = staff; _loading = false; });
    } catch (e) {
      setState(() => _loading = false);
    }
  }

  Future<void> _removeStaff(String id) async {
    final confirm = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: EmobiesTheme.card,
        title: Text('Remove Staff?', style: GoogleFonts.syne(fontWeight: FontWeight.w800)),
        content: Text('This action cannot be undone.', style: GoogleFonts.syne(color: EmobiesTheme.text2)),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(ctx).pop(false),
            child: Text('Cancel', style: GoogleFonts.syne(color: EmobiesTheme.muted)),
          ),
          TextButton(
            onPressed: () => Navigator.of(ctx).pop(true),
            child: Text('Remove', style: GoogleFonts.syne(color: EmobiesTheme.red)),
          ),
        ],
      ),
    );

    if (confirm == true) {
      try {
        await _api.removeStaff(id);
        _load();
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('Staff removed', style: GoogleFonts.syne(color: EmobiesTheme.green)),
              backgroundColor: EmobiesTheme.card,
            ),
          );
        }
      } catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('Failed: $e', style: GoogleFonts.syne(color: EmobiesTheme.red))),
          );
        }
      }
    }
  }

  Color _roleColor(String role) {
    switch (role) {
      case 'delivery_boy': return EmobiesTheme.orange;
      case 'service_center': return EmobiesTheme.blue;
      case 'supervisor': return EmobiesTheme.purple;
      default: return EmobiesTheme.muted;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: EmobiesTheme.bg,
      appBar: AppBar(
        title: const Text('Staff Management'),
        actions: [
          IconButton(
            icon: const Icon(Icons.person_add, color: EmobiesTheme.orange),
            onPressed: () => Navigator.of(context).pushNamed(AppRoutes.adminStaffAdd),
          ),
        ],
      ),
      body: Column(
        children: [
          // Filter tabs
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
            child: SingleChildScrollView(
              scrollDirection: Axis.horizontal,
              child: Row(
                children: [
                  _filterChip('All', 'all'),
                  _filterChip('Delivery', 'delivery_boy'),
                  _filterChip('Service Center', 'service_center'),
                  _filterChip('Supervisor', 'supervisor'),
                ],
              ),
            ),
          ),
          Expanded(
            child: _loading
                ? const Center(child: CircularProgressIndicator(color: EmobiesTheme.orange))
                : _staff.isEmpty
                    ? Center(child: Text('No staff found', style: GoogleFonts.syne(color: EmobiesTheme.muted)))
                    : RefreshIndicator(
                        color: EmobiesTheme.orange,
                        onRefresh: _load,
                        child: ListView.builder(
                          padding: const EdgeInsets.all(16),
                          itemCount: _staff.length,
                          itemBuilder: (_, i) => _staffCard(_staff[i]),
                        ),
                      ),
          ),
        ],
      ),
    );
  }

  Widget _filterChip(String label, String value) {
    final active = _filter == value;
    return GestureDetector(
      onTap: () => setState(() { _filter = value; _load(); }),
      child: Container(
        margin: const EdgeInsets.only(right: 8),
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 6),
        decoration: BoxDecoration(
          color: active ? EmobiesTheme.orange.withOpacity(0.15) : EmobiesTheme.card,
          border: Border.all(color: active ? EmobiesTheme.orange : EmobiesTheme.border),
          borderRadius: BorderRadius.circular(20),
        ),
        child: Text(label,
            style: GoogleFonts.syne(fontWeight: FontWeight.w700, fontSize: 11, color: active ? EmobiesTheme.orange : EmobiesTheme.text2)),
      ),
    );
  }

  Widget _staffCard(Map<String, dynamic> s) {
    final role = s['role'] ?? 'unknown';
    final color = _roleColor(role);
    final isActive = s['is_active'] ?? true;

    return Container(
      margin: const EdgeInsets.only(bottom: 10),
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: EmobiesTheme.card,
        border: Border.all(color: EmobiesTheme.border),
        borderRadius: BorderRadius.circular(13),
      ),
      child: Row(
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(s['name'] ?? 'Unknown', style: GoogleFonts.syne(fontWeight: FontWeight.w700, fontSize: 14, color: EmobiesTheme.text)),
                const SizedBox(height: 4),
                Text(s['phone'] ?? '', style: GoogleFonts.jetBrainsMono(fontSize: 10, color: EmobiesTheme.muted)),
                Text(s['email'] ?? '', style: GoogleFonts.jetBrainsMono(fontSize: 10, color: EmobiesTheme.muted)),
                const SizedBox(height: 6),
                Row(
                  children: [
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                      decoration: BoxDecoration(
                        color: color.withOpacity(0.1),
                        borderRadius: BorderRadius.circular(20),
                      ),
                      child: Text(
                        role.toUpperCase(),
                        style: GoogleFonts.jetBrainsMono(fontSize: 8, color: color, fontWeight: FontWeight.w700),
                      ),
                    ),
                    const SizedBox(width: 8),
                    Container(
                      width: 6,
                      height: 6,
                      decoration: BoxDecoration(
                        color: isActive ? EmobiesTheme.green : EmobiesTheme.red,
                        shape: BoxShape.circle,
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
          IconButton(
            icon: const Icon(Icons.delete_outline, color: EmobiesTheme.red),
            onPressed: () => _removeStaff(s['id']),
          ),
        ],
      ),
    );
  }
}