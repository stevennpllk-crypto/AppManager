# AppManager

تطبيق Android أصلي بـ Kotlin وJetpack Compose وMaterial 3 لإدارة التطبيقات وعرض مؤشرات الجهاز باستخدام واجهات Android الرسمية فقط.

## الوظائف

يعرض التطبيق التطبيقات المثبتة مع الاسم والحزمة والإصدار والحجم التقريبي، ويميّز تطبيقات النظام، ويفتح التطبيق أو صفحة معلوماته الرسمية في إعدادات Android. كما يعرض RAM والتخزين والبطارية ودرجة الحرارة ومعلومات الجهاز وإصدار Android.

إجراء الذاكرة الآمن يطلب من Android تخفيف ذاكرة **AppManager نفسه فقط**. لا يستطيع تطبيق عادي إيقاف تطبيقات أخرى أو تنظيف RAM الخاصة بها بالقوة، ولذلك لا يدّعي التطبيق ذلك.

## الخصوصية والصلاحيات

لا يطلب المشروع أي صلاحية وقت تشغيل أو صلاحية خاصة، ولا يستخدم Root أو ADB أو Accessibility أو Device Admin أو Notification Listener أو الشبكة. معلومات الحزم ومؤشرات الجهاز المستخدمة متاحة عبر Android SDK، وإجراءات إدارة التطبيق تُسلَّم إلى إعدادات النظام الرسمية.

## البناء والتثبيت

1. افتح مجلد المشروع في Android Studio Hedgehog أو أحدث.
2. اترك Android Studio ينشئ/يحدّث Gradle Wrapper ويثبّت Android SDK Platform 35 وBuild Tools المناسبة.
3. نفّذ `./gradlew assembleDebug` من مجلد المشروع.
4. ثبّت `app/build/outputs/apk/debug/app-debug.apk` على جهاز Android 8.0 أو أحدث.

## ملاحظات التوافق

- معلومات CPU التفصيلية ليست واجهة عامة مستقرة للتطبيقات العادية في Android الحديث؛ يعرض التطبيق هذا القيد بدل استخدام قراءة ملفات خاصة أو صلاحيات غير رسمية.
- الحجم المعروض هو حجم ملف APK الأساسي عند توفره، وليس دائمًا الحجم الكامل الذي يحتسبه النظام.
- زر إلغاء التثبيت غير منفذ مباشرة داخل التطبيق لأن Android الحديث يفرض تأكيد العملية عبر واجهة النظام؛ يمكن الوصول إلى صفحة معلومات التطبيق الرسمية من كل صف.
- الواجهة تستخدم موارد عربية وإنجليزية، ويدعم Compose RTL تلقائيًا عند اختيار العربية، كما يتبع الوضع الداكن إعداد النظام.

---

## البناء مباشرة على هاتف Android

هذه النسخة مجهزة للعمل مع بيئات Android IDE على الهاتف. راجع `ANDROID_PHONE_BUILD.md`.

المشروع يستخدم Kotlin + Jetpack Compose + Material 3، ويحتاج Android SDK Platform 35 وJDK 17 أو أحدث مدعوم من بيئة البناء.

بعد نجاح البناء، يكون APK في:

`app/build/outputs/apk/debug/app-debug.apk`

## Codemagic — automatic APK build

This project includes `codemagic.yaml`. Connect the repository to Codemagic and run the `appmanager-debug` workflow. It automatically builds `app/build/outputs/apk/debug/app-debug.apk` and exposes it as an artifact.
