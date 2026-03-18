import 'package:flutter/material.dart';

class SimulatedKeyboard extends StatefulWidget {
  final Function(String) onReplySelected;

  const SimulatedKeyboard({super.key, required this.onReplySelected});

  @override
  State<SimulatedKeyboard> createState() => _SimulatedKeyboardState();
}

class _SimulatedKeyboardState extends State<SimulatedKeyboard> {
  bool _showAiReplies = false;
  String _currentPersona = '霸道总裁';

  final List<String> _mockReplies = [
    '“这点小事都做不好？以后遇到问题直接找我，别自己瞎扛。”',
    '“我怎么可能放心让你一个人？在那里别动，我马上过去。”',
    '“你的心思全写在脸上了。说吧，这次又想要我怎么帮你？”',
  ];

  @override
  Widget build(BuildContext context) {
    return Container(
      color: Colors.grey[200],
      height: 300,
      child: Column(
        children: [
          _buildAiFeatureBar(),
          Expanded(
            child: _showAiReplies ? _buildAiRepliesList() : _buildStandardKeyboard(),
          ),
        ],
      ),
    );
  }

  Widget _buildAiFeatureBar() {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 4.0),
      color: Colors.white,
      child: Row(
        children: [
          Image.network(
            'https://cdn-icons-png.flaticon.com/512/862/862836.png', // Magic wand icon placeholder
            height: 24,
            width: 24,
            errorBuilder: (context, error, stackTrace) =>
                const Icon(Icons.auto_awesome, color: Colors.pinkAccent),
          ),
          const SizedBox(width: 8),
          ElevatedButton(
            onPressed: () {
              setState(() {
                _showAiReplies = !_showAiReplies;
              });
            },
            style: ElevatedButton.styleFrom(
              backgroundColor: _showAiReplies ? Colors.grey[300] : Colors.pinkAccent,
              foregroundColor: _showAiReplies ? Colors.black : Colors.white,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(20),
              ),
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
            ),
            child: Text(_showAiReplies ? '返回键盘' : '帮我回复'),
          ),
          const Spacer(),
          DropdownButton<String>(
            value: _currentPersona,
            icon: const Icon(Icons.arrow_drop_down, color: Colors.pinkAccent),
            elevation: 16,
            style: const TextStyle(color: Colors.pinkAccent),
            underline: Container(
              height: 2,
              color: Colors.pinkAccent,
            ),
            onChanged: (String? newValue) {
              setState(() {
                _currentPersona = newValue!;
              });
            },
            items: <String>['霸道总裁', '温柔暖男', '幽默大师', '高冷男神']
                .map<DropdownMenuItem<String>>((String value) {
              return DropdownMenuItem<String>(
                value: value,
                child: Text(value),
              );
            }).toList(),
          ),
        ],
      ),
    );
  }

  Widget _buildAiRepliesList() {
    return ListView.builder(
      itemCount: _mockReplies.length,
      padding: const EdgeInsets.all(8.0),
      itemBuilder: (context, index) {
        return Card(
          margin: const EdgeInsets.symmetric(vertical: 4.0),
          child: ListTile(
            title: Text(_mockReplies[index]),
            trailing: const Icon(Icons.send, color: Colors.pinkAccent, size: 20),
            onTap: () {
              widget.onReplySelected(_mockReplies[index]);
              setState(() {
                _showAiReplies = false;
              });
            },
          ),
        );
      },
    );
  }

  Widget _buildStandardKeyboard() {
    // A highly simplified visual mock of a keyboard
    return Column(
      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
      children: [
        _buildKeyboardRow(['Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P']),
        _buildKeyboardRow(['A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L']),
        _buildKeyboardRow(['⬆️', 'Z', 'X', 'C', 'V', 'B', 'N', 'M', '⌫']),
        _buildKeyboardRow(['123', '🌐', '空格', '回车']),
      ],
    );
  }

  Widget _buildKeyboardRow(List<String> keys) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: keys.map((key) {
        double width = 32;
        if (key == '空格') width = 150;
        if (key == '回车' || key == '⬆️' || key == '⌫' || key == '123' || key == '🌐') {
          width = 45;
        }

        return Container(
          margin: const EdgeInsets.symmetric(horizontal: 2.0),
          width: width,
          height: 45,
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(6),
            boxShadow: const [
              BoxShadow(
                color: Colors.grey,
                blurRadius: 1,
                offset: Offset(0, 1),
              )
            ],
          ),
          child: Center(
            child: Text(
              key,
              style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w500),
            ),
          ),
        );
      }).toList(),
    );
  }
}
