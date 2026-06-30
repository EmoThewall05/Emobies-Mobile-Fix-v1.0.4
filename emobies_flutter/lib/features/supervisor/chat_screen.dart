import 'dart:async';
import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import '../../config/theme.dart';
import '../../core/models/chat_model.dart';
import '../../core/services/supabase_service.dart';
import '../../core/services/auth_service.dart';
import '../../core/services/cloudflare_ai_service.dart';

class ChatScreen extends StatefulWidget {
  final String chatId;
  final String title;
  final List<String> participants;

  const ChatScreen({
    super.key,
    required this.chatId,
    required this.title,
    required this.participants,
  });

  @override
  State<ChatScreen> createState() => _ChatScreenState();
}

class _ChatScreenState extends State<ChatScreen> {
  final _supabase = SupabaseService.instance;
  final _auth = AuthService();
  final _ai = CloudflareAIService();
  final _ctrl = TextEditingController();
  final _scrollCtrl = ScrollController();

  List<ChatMessage> _messages = [];
  bool _loading = true;
  StreamSubscription? _sub;

  @override
  void initState() {
    super.initState();
    _loadMessages();
    _subscribe();
  }

  Future<void> _loadMessages() async {
    try {
      final msgs = await _supabase.getChatMessages(widget.chatId);
      setState(() {
        _messages = msgs.reversed.toList();
        _loading = false;
      });
      _scrollToBottom();
    } catch (e) {
      setState(() => _loading = false);
    }
  }

  void _subscribe() {
    _sub = _supabase.client
        .channel('chat_${widget.chatId}')
        .onPostgresChanges(
          event: PostgresChangeEvent.insert,
          schema: 'public',
          table: 'chat_messages',
          filter: PostgresChangeFilter(
            type: PostgresChangeFilterType.eq,
            column: 'room_id',
            value: widget.chatId,
          ),
          callback: (payload) {
            final msg = ChatMessage.fromJson(payload.newRecord);
            setState(() => _messages.add(msg));
            _scrollToBottom();
          },
        )
        .subscribe();
  }

  Future<void> _send() async {
    final text = _ctrl.text.trim();
    if (text.isEmpty) return;

    final userId = _auth.currentUser?.id ?? '';
    final userName = _auth.currentUser?.displayName ?? 'User';

    _ctrl.clear();

    try {
      // AI monitoring for service center chats
      final monitorResult = await _ai.monitorChat(text);
      if (monitorResult['safe'] != true) {
        final flags = (monitorResult['flags'] as List?)?.join(', ') ?? 'policy violation';
        _showWarning('Message flagged: $flags');
        return;
      }

      await _supabase.sendMessage({
        'room_id': widget.chatId,
        'sender_id': userId,
        'sender_name': userName,
        'content': text,
        'type': 'text',
        'is_ai_generated': false,
      });
    } catch (e) {
      _showWarning('Failed to send message');
    }
  }

  void _showWarning(String msg) {
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(msg, style: GoogleFonts.syne(color: EmobiesTheme.yellow)),
        backgroundColor: EmobiesTheme.card,
      ),
    );
  }

  void _scrollToBottom() {
    Future.delayed(const Duration(milliseconds: 100), () {
      if (_scrollCtrl.hasClients) {
        _scrollCtrl.animateTo(
          _scrollCtrl.position.maxScrollExtent,
          duration: const Duration(milliseconds: 300),
          curve: Curves.easeOut,
        );
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: EmobiesTheme.bg,
      appBar: AppBar(
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(widget.title, style: GoogleFonts.syne(fontWeight: FontWeight.w800, fontSize: 14)),
            Text('${widget.participants.length} participants · AI monitored',
                style: GoogleFonts.jetBrainsMono(fontSize: 9, color: EmobiesTheme.muted)),
          ],
        ),
        actions: [
          Container(
            margin: const EdgeInsets.only(right: 12),
            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
            decoration: BoxDecoration(
              color: EmobiesTheme.yellow.withOpacity(0.1),
              borderRadius: BorderRadius.circular(20),
            ),
            child: Text('🤖 Monitored',
                style: GoogleFonts.jetBrainsMono(fontSize: 9, color: EmobiesTheme.yellow)),
          ),
        ],
      ),
      body: Column(
        children: [
          Expanded(
            child: _loading
                ? const Center(child: CircularProgressIndicator(color: EmobiesTheme.orange))
                : ListView.builder(
                    controller: _scrollCtrl,
                    padding: const EdgeInsets.all(16),
                    itemCount: _messages.length,
                    itemBuilder: (_, i) => _msgBubble(_messages[i]),
                  ),
          ),
          _buildInput(),
        ],
      ),
    );
  }

  Widget _msgBubble(ChatMessage msg) {
    final isMe = msg.senderId == _auth.currentUser?.id;
    final isSystem = msg.type == MessageType.system;

    if (isSystem) {
      return Center(
        child: Container(
          margin: const EdgeInsets.symmetric(vertical: 8),
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
          decoration: BoxDecoration(
            color: EmobiesTheme.surface,
            borderRadius: BorderRadius.circular(20),
          ),
          child: Text(msg.content,
              style: GoogleFonts.jetBrainsMono(fontSize: 10, color: EmobiesTheme.muted)),
        ),
      );
    }

    return Align(
      alignment: isMe ? Alignment.centerRight : Alignment.centerLeft,
      child: Container(
        margin: const EdgeInsets.only(bottom: 8),
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
        constraints: BoxConstraints(maxWidth: MediaQuery.of(context).size.width * 0.75),
        decoration: BoxDecoration(
          color: isMe ? EmobiesTheme.orange.withOpacity(0.15) : EmobiesTheme.card,
          borderRadius: BorderRadius.only(
            topLeft: const Radius.circular(14),
            topRight: const Radius.circular(14),
            bottomLeft: Radius.circular(isMe ? 14 : 4),
            bottomRight: Radius.circular(isMe ? 4 : 14),
          ),
          border: Border.all(
            color: isMe ? EmobiesTheme.orange.withOpacity(0.2) : EmobiesTheme.border,
          ),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            if (!isMe)
              Text(msg.senderName,
                  style: GoogleFonts.syne(fontSize: 10, fontWeight: FontWeight.w700, color: EmobiesTheme.orange)),
            Text(msg.content, style: TextStyle(fontSize: 13, color: isMe ? EmobiesTheme.text : EmobiesTheme.text2)),
          ],
        ),
      ),
    );
  }

  Widget _buildInput() {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      decoration: BoxDecoration(
        color: EmobiesTheme.surface,
        border: const Border(top: BorderSide(color: EmobiesTheme.border)),
      ),
      child: SafeArea(
        child: Row(
          children: [
            Expanded(
              child: TextField(
                controller: _ctrl,
                style: const TextStyle(color: EmobiesTheme.text, fontSize: 14),
                decoration: InputDecoration(
                  hintText: 'Type a message...',
                  filled: true,
                  fillColor: EmobiesTheme.bg,
                  contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(24),
                    borderSide: BorderSide.none,
                  ),
                ),
                onSubmitted: (_) => _send(),
              ),
            ),
            const SizedBox(width: 8),
            GestureDetector(
              onTap: _send,
              child: Container(
                padding: const EdgeInsets.all(10),
                decoration: const BoxDecoration(color: EmobiesTheme.orange, shape: BoxShape.circle),
                child: const Icon(Icons.send, color: Colors.white, size: 20),
              ),
            ),
          ],
        ),
      ),
    );
  }

  @override
  void dispose() {
    _sub?.cancel();
    _ctrl.dispose();
    _scrollCtrl.dispose();
    super.dispose();
  }
}