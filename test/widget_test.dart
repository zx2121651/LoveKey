import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:lovekey_clone/main.dart';

void main() {
  testWidgets('App smoke test', (WidgetTester tester) async {
    // Build our app and trigger a frame.
    await tester.pumpWidget(const LovekeyCloneApp());

    // Verify that the bottom navigation bar has the correct tabs.
    expect(find.text('话术库'), findsOneWidget);
    expect(find.text('人设市场'), findsOneWidget);
    expect(find.text('我的'), findsOneWidget);

    // Tap the '人设市场' icon and trigger a frame.
    await tester.tap(find.text('人设市场'));
    await tester.pumpAndSettle();

    // Verify that the '人设市场' text is present on the screen in the AppBar.
    expect(find.text('人设市场'), findsAtLeast(1));
  });
}
