# بناء AppManager مباشرة على هاتف Android

## الطريقة الموصى بها: AndroidIDE

1. ثبّت AndroidIDE على الهاتف من مصدره الموثوق.
2. فك ضغط `AppManager_PhoneBuild.zip` إلى مجلد يمكن الوصول إليه.
3. افتح AndroidIDE واختر فتح/استيراد مشروع Gradle موجود.
4. اختر مجلد `AppManager` الذي يحتوي على `settings.gradle.kts`.
5. عند طلب JDK استخدم JDK 17 أو أحدث مدعوم من بيئة البناء.
6. تأكد من توفر Android SDK Platform 35 وBuild Tools مناسبة.
7. انتظر Gradle Sync حتى يكتمل بدون أخطاء.
8. اختر مهمة `assembleDebug` أو زر Build/Run.
9. بعد نجاح البناء ستجد:

`app/build/outputs/apk/debug/app-debug.apk`

يمكن تثبيت APK على الهاتف بعد السماح بتثبيت التطبيقات من هذا المصدر إذا طلب Android ذلك.

## إذا كنت تستخدم Termux

ضع المشروع في مساحة تخزين يمكن الوصول إليها، ثم ثبّت Java وGradle بالطريقة المناسبة لبيئة Termux لديك.

من مجلد المشروع شغّل:

`bash tools/build-termux.sh`

إذا لم يكن Gradle مثبتًا، استخدم AndroidIDE بدلًا من محاولة تثبيت أدوات Android يدويًا.

## متطلبات المشروع

- Android Gradle Plugin: 8.5.2
- Kotlin: 2.0.21
- Compile SDK: 35
- Target SDK: 35
- Min SDK: 26
- Java/JVM target: 17

## مهم

هذا المشروع لا يحتاج Root أو ADB أو Shizuku لبناء أو تشغيل وظائفه الأساسية.

لا تضع ملف `local.properties` داخل المشروع عند نقله بين الأجهزة؛ AndroidIDE ينشئ إعدادات SDK المحلية حسب بيئته.
