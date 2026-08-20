# Task: Add Arabic (ar) localization

## Context
app/src/main/AndroidManifest.xml already has android:supportsRtl="true"
- no manifest change is needed for this task.

The app currently ships translations for: default (English, in
values/strings.xml), French, Italian, Dutch, Spanish, Portuguese
(Brazil), German, Polish, Portuguese - but no Arabic. The base file
app/src/main/res/values/strings.xml contains 176 string resources.

## What to do

1. Create a new directory app/src/main/res/values-ar/.
2. Create app/src/main/res/values-ar/strings.xml inside it, containing
   an Arabic translation of every single string name="..." entry in
   app/src/main/res/values/strings.xml - all 176 of them, none skipped.
3. Use the exact same name="..." attribute for each string as the
   English source - these are resource IDs referenced elsewhere in the
   code and must match exactly, character-for-character.
4. If any string contains a format placeholder (%s, %d, %1$s,
   %2$d, etc.), preserve the placeholder exactly as-is in the same
   position it makes grammatical sense in Arabic - do not translate or
   remove the placeholder itself, only the surrounding text.
5. If any string contains XML-escaped characters (&amp;, apostrophe-escape,
   quote-escape, &lt;, &gt;), preserve the same escaping convention in
   the Arabic version.
6. Use standard Modern Standard Arabic, natural and idiomatic - not a
   literal word-for-word machine translation. This is a UI a real person
   will read every day; tone should be simple, warm, and consistent with
   the app's stated philosophy (calm, unhurried, non-naggy - see the
   About section of README.md for the app's voice before translating
   anything).
7. Follow the exact same XML structure/formatting style as the existing
   translation files (e.g. values-de/strings.xml) - same resources
   wrapper, same indentation style, no extra attributes.

## Constraints
- Do not modify values/strings.xml (the English source) or any other
  existing values-XX/strings.xml file.
- Do not add a plurals block - the source file has none, so the Arabic
  file shouldn't invent one either.
- Do not touch AndroidManifest.xml - RTL support is already declared.
- This task is translation only. Do not go looking for hardcoded
  start/end/left/right assumptions in Compose UI code - that's a
  separate follow-up task once this translation exists and can actually
  be tested on-device with the language switched to Arabic.

## Done when
- app/src/main/res/values-ar/strings.xml exists.
- The count of string entries in the new file matches the count in the
  English source file exactly (count both and compare).
- Every name="..." value in the new file has a matching entry in the
  English source file (no typos, no invented keys).
- A short summary is printed listing any strings you found ambiguous or
  had to make a judgment call on, so they can be spot-checked by a native
  speaker before merging.
