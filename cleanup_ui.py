import os
import re

dirs = [
    'child-app/app/src/main/res/layout',
    'parent-android-app/app/src/main/res/layout'
]

for d in dirs:
    if not os.path.exists(d): continue
    for f in os.listdir(d):
        if not f.endswith('.xml'): continue
        filepath = os.path.join(d, f)
        with open(filepath, 'r') as file:
            content = file.read()
        
        # 1. Remove gradients
        content = re.sub(r'@drawable/bg_gradient_purple', '@color/primary', content)
        content = re.sub(r'@drawable/bg_gradient_primary', '@color/primary', content)
        
        # 2. Spacing: 24dp, 20dp, 28dp padding -> 16dp
        content = re.sub(r'padding="24dp"', 'padding="16dp"', content)
        content = re.sub(r'padding="20dp"', 'padding="16dp"', content)
        content = re.sub(r'padding="28dp"', 'padding="16dp"', content)
        content = re.sub(r'paddingHorizontal="24dp"', 'paddingHorizontal="16dp"', content)
        content = re.sub(r'paddingHorizontal="20dp"', 'paddingHorizontal="16dp"', content)
        content = re.sub(r'paddingTop="24dp"', 'paddingTop="16dp"', content)
        content = re.sub(r'paddingBottom="24dp"', 'paddingBottom="16dp"', content)
        content = re.sub(r'layout_margin="24dp"', 'layout_margin="16dp"', content)
        content = re.sub(r'layout_margin="20dp"', 'layout_margin="16dp"', content)

        # 3. Radius: 24dp, 20dp, 16dp radius -> 12dp
        content = re.sub(r'app:cardCornerRadius="24dp"', 'app:cardCornerRadius="12dp"', content)
        content = re.sub(r'app:cardCornerRadius="20dp"', 'app:cardCornerRadius="12dp"', content)
        content = re.sub(r'app:cardCornerRadius="16dp"', 'app:cardCornerRadius="12dp"', content)
        content = re.sub(r'app:cornerRadius="24dp"', 'app:cornerRadius="12dp"', content)
        content = re.sub(r'app:cornerRadius="20dp"', 'app:cornerRadius="12dp"', content)
        content = re.sub(r'app:cornerRadius="16dp"', 'app:cornerRadius="12dp"', content)

        # 4. Button Heights: 56dp, 60dp, 80dp -> minHeight 48dp or 52dp (we will use 52dp for major buttons)
        # Actually Phase 1 says 48-52dp height for primary button. Let's just remove hardcoded large heights and use 52dp max or let wrap_content
        content = re.sub(r'android:layout_height="60dp"', 'android:layout_height="wrap_content"\n            android:minHeight="48dp"', content)
        content = re.sub(r'android:layout_height="56dp"', 'android:layout_height="wrap_content"\n            android:minHeight="48dp"', content)
        # Let's leave SOS button at 80dp since it's a huge emergency button, or maybe change to 64dp so it's not too massive.
        
        with open(filepath, 'w') as file:
            file.write(content)

print("UI Cleanup applied!")
