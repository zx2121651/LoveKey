import 'package:flutter/material.dart';

class CounselorScreen extends StatefulWidget {
  const CounselorScreen({super.key});

  @override
  State<CounselorScreen> createState() => _CounselorScreenState();
}

class ChatMessage {
  final String text;
  final bool isUser;
  ChatMessage({required this.text, required this.isUser});
}

class _CounselorScreenState extends State<CounselorScreen> {
  final TextEditingController _textController = TextEditingController();
  final ScrollController _scrollController = ScrollController();
  final List<ChatMessage> _messages = [];
  final GlobalKey<AnimatedListState> _listKey = GlobalKey<AnimatedListState>();
  bool _isTyping = false;

  void _handleSubmitted(String text) {
    _textController.clear();
    if (text.trim().isEmpty) return;

    setState(() {
      _isTyping = true;
    });

    _messages.add(ChatMessage(text: text, isUser: true));
    _listKey.currentState?.insertItem(_messages.length); // length+1 is effectively the end, index is _messages.length - 1 + 1 (for header)

    _scrollToBottom();

    // Simulate AI thinking and replying
    Future.delayed(const Duration(milliseconds: 1500), () {
      if (!mounted) return;
      setState(() {
        _isTyping = false;
      });

      _messages.add(ChatMessage(
        text: _generateMockResponse(text),
        isUser: false,
      ));
      _listKey.currentState?.insertItem(_messages.length);

      _scrollToBottom();
    });
  }

  String _generateMockResponse(String userText) {
    if (userText.contains('暧昧') || userText.contains('天天聊天')) {
      return '天天聊天确实容易产生依赖，如果关系一直没有突破，可以尝试稍微“冷”一下对方，看看TA的反应。或者找个合适的契机，比如周末，主动邀约对方出来吃个饭。';
    } else if (userText.contains('吵架')) {
      return '情侣吵架很正常。先给自己和对方一点空间冷静一下。然后试着从对方的角度想想，主动破冰的时候可以说：“其实我刚才态度也有点急，我们好好聊聊可以吗？”';
    } else if (userText.contains('异地恋')) {
      return '异地恋最重要的是建立信任和分享欲。建议你们可以约定一个固定的视频时间，平时多分享一些生活中的小确幸，如果条件允许，定期安排见面也是非常必要的哦。';
    }
    return '我理解你的感受。感情的事情有时候确实很复杂，你能再多跟我说说具体的细节吗？这样我能更好地帮到你。';
  }

  void _scrollToBottom() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (_scrollController.hasClients) {
        _scrollController.animateTo(
          _scrollController.position.maxScrollExtent,
          duration: const Duration(milliseconds: 300),
          curve: Curves.easeOut,
        );
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF9FAFB),
      appBar: AppBar(
        title: const Text('情感导师', style: TextStyle(color: Colors.black, fontSize: 18, fontWeight: FontWeight.bold)),
        backgroundColor: Colors.white,
        elevation: 0.5,
        centerTitle: true,
        leading: IconButton(
          icon: const Icon(Icons.chevron_left, color: Colors.black, size: 30),
          onPressed: () => Navigator.of(context).pop(),
        ),
      ),
      body: SafeArea(
        child: Column(
          children: [
            Expanded(child: _buildChatArea()),
            if (_isTyping)
              const Padding(
                padding: EdgeInsets.symmetric(horizontal: 20, vertical: 8),
                child: Align(
                  alignment: Alignment.centerLeft,
                  child: Text('导师正在思考中...', style: TextStyle(color: Colors.grey, fontSize: 12)),
                ),
              ),
            _buildMessageInput(),
          ],
        ),
      ),
    );
  }

  Widget _buildChatArea() {
    return AnimatedList(
      key: _listKey,
      controller: _scrollController,
      padding: const EdgeInsets.all(16),
      initialItemCount: _messages.length + 1,
      itemBuilder: (context, index, animation) {
        if (index == 0) {
          return _buildGreetingCard();
        }
        final message = _messages[index - 1];
        return SlideTransition(
          position: Tween<Offset>(
            begin: const Offset(0, 0.5),
            end: Offset.zero,
          ).animate(CurvedAnimation(
            parent: animation,
            curve: Curves.easeOutBack,
          )),
          child: FadeTransition(
            opacity: animation,
            child: _buildChatMessage(message),
          ),
        );
      },
    );
  }

  Widget _buildChatMessage(ChatMessage message) {
    return Container(
      margin: const EdgeInsets.symmetric(vertical: 8),
      child: Row(
        mainAxisAlignment: message.isUser ? MainAxisAlignment.end : MainAxisAlignment.start,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          if (!message.isUser)
            CircleAvatar(
              radius: 16,
              backgroundColor: Colors.blue[100],
              child: const Icon(Icons.support_agent, size: 20, color: Colors.blue),
            ),
          const SizedBox(width: 8),
          Flexible(
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
              decoration: BoxDecoration(
                color: message.isUser ? const Color(0xFFFF4D85) : Colors.white,
                borderRadius: BorderRadius.only(
                  topLeft: const Radius.circular(16),
                  topRight: const Radius.circular(16),
                  bottomLeft: Radius.circular(message.isUser ? 16 : 4),
                  bottomRight: Radius.circular(message.isUser ? 4 : 16),
                ),
                boxShadow: [
                  BoxShadow(
                    color: Colors.grey.withOpacity(0.08),
                    spreadRadius: 1,
                    blurRadius: 4,
                    offset: const Offset(0, 1),
                  ),
                ],
              ),
              child: Text(
                message.text,
                style: TextStyle(
                  color: message.isUser ? Colors.white : Colors.black87,
                  fontSize: 14,
                  height: 1.4,
                ),
              ),
            ),
          ),
          const SizedBox(width: 8),
          if (message.isUser)
            CircleAvatar(
              radius: 16,
              backgroundColor: Colors.orange[100],
              child: Icon(Icons.pets, size: 20, color: Colors.orange[400]),
            ),
        ],
      ),
    );
  }

  Widget _buildGreetingCard() {
    return Container(
      margin: const EdgeInsets.only(bottom: 24, top: 8),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withOpacity(0.05),
            spreadRadius: 1,
            blurRadius: 4,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              CircleAvatar(
                radius: 14,
                backgroundColor: Colors.blue[100],
                child: const Icon(Icons.support_agent, size: 16, color: Colors.blue),
              ),
              const SizedBox(width: 8),
              const Text(
                '告诉我你的困扰吧，我随时准备倾听和陪伴～',
                style: TextStyle(fontSize: 14, color: Colors.black87, fontWeight: FontWeight.w500),
              ),
            ],
          ),
          const SizedBox(height: 16),
          _buildQuickQuestion('我们天天聊天，关系很暧昧，但就是没...'),
          _buildQuickQuestion('异地恋如何维持感情？总觉得没有安...'),
          _buildQuickQuestion('我们吵架了，不知道该怎么开口和好'),
        ],
      ),
    );
  }

  Widget _buildQuickQuestion(String text) {
    return GestureDetector(
      onTap: () {
        _handleSubmitted(text);
      },
      child: Container(
        margin: const EdgeInsets.only(bottom: 8),
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        decoration: BoxDecoration(
          color: const Color(0xFFF3F4F6),
          borderRadius: BorderRadius.circular(12),
        ),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Expanded(
              child: Text(
                text,
                style: const TextStyle(fontSize: 13, color: Colors.black87),
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
            ),
            const Icon(Icons.chevron_right, size: 16, color: Colors.grey),
          ],
        ),
      ),
    );
  }

  Widget _buildMessageInput() {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      decoration: BoxDecoration(
        color: Colors.white,
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withOpacity(0.1),
            spreadRadius: 1,
            blurRadius: 10,
            offset: const Offset(0, -2),
          ),
        ],
      ),
      child: Row(
        children: [
          Expanded(
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              decoration: BoxDecoration(
                color: const Color(0xFFF3F4F6),
                borderRadius: BorderRadius.circular(24),
              ),
              child: TextField(
                controller: _textController,
                textInputAction: TextInputAction.send,
                onSubmitted: _handleSubmitted,
                decoration: const InputDecoration(
                  hintText: '输入你想要写的内容',
                  hintStyle: TextStyle(color: Colors.grey, fontSize: 14),
                  border: InputBorder.none,
                ),
              ),
            ),
          ),
          const SizedBox(width: 12),
          GestureDetector(
            onTap: () => _handleSubmitted(_textController.text),
            child: const CircleAvatar(
              backgroundColor: Color(0xFFFF4D85),
              radius: 20,
              child: Icon(Icons.send, color: Colors.white, size: 18),
            ),
          ),
        ],
      ),
    );
  }
}
