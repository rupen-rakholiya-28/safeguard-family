import os
import re

dirs = [
    'child-app/app/src/main/res/layout',
    'parent-android-app/app/src/main/res/layout'
]

# We will move android:background from LinearLayouts inside MaterialCardView to the CardView itself
for d in dirs:
    if not os.path.exists(d): continue
    for f in os.listdir(d):
        if not f.endswith('.xml'): continue
        filepath = os.path.join(d, f)
        with open(filepath, 'r') as file:
            content = file.read()
        
        # This regex matches MaterialCardView followed by a LinearLayout with a background.
        # However, regex for nested XML is tricky. Since it's a simple optimization, 
        # let's just make sure we don't have redundant background colors if the parent is a Card.
        # Actually, simpler: if a MaterialCardView has a child LinearLayout with android:background="@color/primary", 
        # we can just remove that android:background from the LinearLayout and ensure the card has app:cardBackgroundColor="@color/primary"
        
        # Find all cards without background but with a child having primary background
        content = re.sub(
            r'(<com\.google\.android\.material\.card\.MaterialCardView[^>]*?)(\s*>)\s*<LinearLayout([^>]*?)android:background="(@color/primary)"',
            r'\1\n            app:cardBackgroundColor="\4"\2\n            <LinearLayout\3',
            content,
            flags=re.DOTALL
        )
        
        with open(filepath, 'w') as file:
            file.write(content)

print("Overdraw optimization applied!")
