import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:lovekey_clone/main.dart';

void main() {
  testWidgets('Bottom navigation test', (WidgetTester tester) async {
    await tester.pumpWidget(const LoveKeyApp());

    // Verify that we start on the Home screen.
    expect(find.text('首页'), findsWidgets);

    // Tap the '键盘' icon and trigger a frame.
    await tester.tap(find.byIcon(Icons.keyboard_outlined));
    await tester.pump();

    // Verify that we are on the Keyboard screen.
    expect(find.text('键盘'), findsWidgets);
  });
}
