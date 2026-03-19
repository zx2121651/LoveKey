import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:lovekey_clone/main.dart';

void main() {
  testWidgets('Onboarding and Bottom navigation test', (WidgetTester tester) async {
    await tester.pumpWidget(const LoveKeyApp());

    // Verify that we start on the Onboarding screen.
    expect(find.text('第一步：开启键盘'), findsWidgets);
    expect(find.text('跳过'), findsOneWidget);

    // Tap '跳过' to go to MainScreen
    await tester.tap(find.text('跳过'));
    // Use pump instead of pumpAndSettle because HomeScreen has an infinite animation (heartbeat)
    await tester.pump(const Duration(milliseconds: 500));
    await tester.pump(const Duration(milliseconds: 500));

    // Verify that we are now on the Home screen.
    expect(find.text('首页'), findsWidgets);

    // Tap the '键盘' icon and trigger a frame.
    await tester.tap(find.byIcon(Icons.keyboard_outlined));
    await tester.pump(const Duration(milliseconds: 300)); // Just pump once instead of settle due to animations

    // Verify that we are on the Keyboard screen.
    expect(find.text('键盘'), findsWidgets);
  });
}
