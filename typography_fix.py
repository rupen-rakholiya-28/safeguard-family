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
        
        # Ensure bold for headings instead of textStyle="bold"
        content = re.sub(r'android:textStyle="bold"', 'android:fontFamily="sans-serif-bold"', content)
        
        # For standard section headers in Android that might lack fontFamily
        # Find TextViews with textSize >= 20sp without fontFamily and add it
        content = re.sub(r'(<TextView[^>]*?android:textSize="(?:20|24|26|28|32)sp"(?![^>]*?android:fontFamily))', r'\1\n            android:fontFamily="sans-serif-bold"', content)
        
        # For subheadings (16sp, 18sp)
        content = re.sub(r'(<TextView[^>]*?android:textSize="(?:16|18)sp"(?![^>]*?android:fontFamily))', r'\1\n            android:fontFamily="sans-serif-semibold"', content)
        
        # Standardize button styles in Auth
        # Make sure btnSubmit and btnSwitchMode in auth use minHeight 48dp
        content = re.sub(r'(<com\.google\.android\.material\.button\.MaterialButton[^>]*?android:id="@+id/(?:btnSubmit|btnJoinFamily|btnSignIn|btnSwitchMode)"[^>]*?)(\s*/>|\s*>)', 
                         lambda m: m.group(1) + ('' if 'android:minHeight' in m.group(1) else '\n            android:minHeight="48dp"') + m.group(2), 
                         content)

        # Standardize input field labels/hints spacing
        # Ensure TextInputLayouts have standard spacing
        content = re.sub(r'(<com\.google\.android\.material\.textfield\.TextInputLayout[^>]*?android:layout_marginBottom=")24dp"', r'\116dp"', content)
        
        with open(filepath, 'w') as file:
            file.write(content)

print("Typography & Auth spacing fixed!")
