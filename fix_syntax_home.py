import re

def main():
    with open('lib/screens/home_screen.dart', 'r') as f:
        content = f.read()

    # The issue:
    # return Transform.translate(
    #   ...
    #   child: Transform.scale(
    #     ...
    #     child: SizedBox(
    #       ...
    #       child: Stack(
    #       ...
    #       )
    #     ),
    #   );

    # Notice that SizedBox is missing a closing parenthesis.
    # We have:
    #                   child: SizedBox(
    #                     width: 160,
    #                     height: 160,
    #                   child: Stack(
    # ...
    #                     ],
    #                   ),
    #                 ),
    #               );

    # We need one more `)` before the `;`.
    # Let's replace the end of the block.

    bad_end = '''                      ],
                    ),
                  ),
                ),
              );'''

    good_end = '''                      ],
                    ),
                  ),
                ),
              );
            },
          ),'''

    # Let's just fix it properly with regex targeting the end of that specific AnimatedBuilder return block.
    content = content.replace(
        "                    ],
                  ),
                ),
              );",
        "                      ],
                    ),
                  ),
                ),
              );"
    ) # This might not be precise.

    # Easier to just inject the missing closing parenthesis for SizedBox:
    content = re.sub(
        r'(                          \],
                        \),
                      \],
                    \),
                  \),
                \),
              \);)',
        r'                          ],\n                        ),\n                      ],\n                    ),\n                  ),\n                ),\n              );\n',
        content
    )
    pass
if __name__ == '__main__':
    main()
