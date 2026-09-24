# AppManager — Codemagic build

هذا المشروع مهيأ لبناء `app-debug.apk` تلقائياً على Codemagic.

## ما الذي تم تجهيزه؟

- ملف `codemagic.yaml` في جذر المشروع.
- Workflow باسم `AppManager - Debug APK`.
- ضبط Android SDK تلقائياً.
- إنشاء Gradle Wrapper 8.7 إذا لم يكن موجوداً.
- تشغيل `assembleDebug` تلقائياً.
- التحقق من وجود `app/build/outputs/apk/debug/app-debug.apk`.
- رفع `app-debug.apk` كـ Build Artifact.

## الاستخدام

1. ارفع **محتويات مجلد AppManager** إلى مستودع GitHub جديد، بحيث يكون `codemagic.yaml` في جذر المستودع.
2. اربط المستودع مع Codemagic.
3. اختر Workflow: `appmanager-debug`.
4. اضغط Start new build.
5. بعد نجاح البناء، نزّل `app-debug.apk` من قسم Artifacts.

لا تحتاج إلى إعداد مفتاح توقيع Release لهذا الاختبار؛ `assembleDebug` ينتج APK Debug موقّعاً بمفتاح debug تلقائياً.
