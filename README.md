# CMC – Customer Management Center

Endüstriyel makine (kompresör, kurutucu vb.) bakım süreçlerini yöneten Android/Kotlin/Jetpack Compose müşteri uygulaması. Şirket ve makine envanteri, periyodik bakım planlama–hazırlık–tamamlama akışı, malzeme/yedek parça stok takibi, kullanıcı/yetki yönetimi ve bildirim altyapısını tek bir uygulamada bir araya getirir.

- **Package / applicationId:** `com.cmc.customer`
- **Version:** 1.0 (versionCode 1)
- **minSdk:** 26 · **targetSdk / compileSdk:** 35 · **JVM target:** 17

## İçindekiler

- [Teknoloji Yığını](#teknoloji-yığını)
- [Özellikler / Modüller](#özellikler--modüller)
- [Mimari](#mimari)
- [Kurulum](#kurulum)
- [Derleme](#derleme)
- [Yapılandırma Notları](#yapılandırma-notları)
- [Proje Yapısı](#proje-yapısı)

## Teknoloji Yığını

- **Dil / UI:** Kotlin, Jetpack Compose (Material3 + kısmen Material2), Navigation Compose, Accompanist Pager/Permissions, Coil, Compose Animation.
- **Build:** Gradle Kotlin DSL, AGP 8.9.1, Kotlin 2.0.21, Compose Compiler plugin.
- **Backend / Cloud:** Firebase (BOM 33.13.0) — Auth, Firestore, Storage, Analytics, Cloud Messaging (FCM). `google-services` Gradle eklentisi kullanılıyor.
- **Ağ:** Retrofit2 + Gson, OkHttp (resmi tatil API'si için), NanoHTTPD (yerel P2P HTTP sunucu), Kotlin Coroutines.
- **Harita / OCR:** Google Maps (play-services-maps + maps-compose), Google ML Kit Text Recognition (kağıt bakım formlarının OCR ile dijitalleştirilmesi).
- **Güvenlik:** `androidx.security:security-crypto` — "beni hatırla" verisi EncryptedSharedPreferences ile saklanıyor.
- **Test:** JUnit4, Espresso, Compose UI Test (temel iskelet).

## Özellikler / Modüller

| Modül | Açıklama |
|---|---|
| **Auth** | Firebase Auth ile giriş (`LoginScreen`), kullanıcı işlem logları (`LogScreen`) |
| **Company** | Şirket listeleme, detay, ekleme/düzenleme |
| **Machine** | Makine detayları; kompresör/kurutucu tipine özel diyaloglar |
| **Maintenance** | Bakım planlama, makine seçimi, detay ve tamamlama akışı (malzeme kullanımıyla entegre) |
| **Preparation** | Bakım öncesi hazırlık listesi ve detayları |
| **Material** | Kategori bazlı malzeme/stok yönetimi |
| **Calendar** | Aylık takvim üzerinde planlanmış bakımlar + resmi tatil günleri (dış API) |
| **Notification** | Stok kritik, bakım yaklaşan/gecikmiş/tamamlanan ve görev atama bildirimleri (FCM + local) |
| **OCR** | Kamera ile form fotoğrafı çekip ML Kit ile metne çevirme, bakım planına aktarma |
| **User** | Kullanıcı listesi, rol/izin ataması, ayarlar |

## Mimari

- **Desen:** MVVM + tek-Activity Compose mimarisi. `MainActivity` giriş noktası; oturum kontrolü sonrası tek `NavHost` (`AppNavigation`) üzerinden yönlendirme yapılır.
- **Navigasyon:** Route bazlı; `Company`/`Machine`/`Maintenance` gibi nesneler Gson ile serileştirilip URI-encode edilerek argüman olarak taşınır.
- **Yetkilendirme:** `PermissionManager` singleton, her rotaya girişte rol bazlı kontrol yapar; yetkisiz erişimde Snackbar gösterir.
- **Veri katmanı:** Firebase Firestore birincil veritabanı, Firebase Auth kimlik doğrulama, Firebase Storage dosya depolama, FCM push bildirimleri.
- **Bildirim akışı:** `MainActivity` FCM token'ını alıp `users/{uid}` dokümanına yazar ve `maintenance` topic'ine abone olur → `MyFirebaseMessagingService` (paket: `com.cmc.customer.fcm`) gelen mesajı `NotificationHelper` üzerinden ilgili kanaldan gösterir → bildirime tıklama ilgili detay ekranına yönlendirir.

## Kurulum

1. **Android Studio** (AGP 8.9.1 uyumlu sürüm) ve **JDK 17** kurulu olmalı.
2. Firebase projenizden indirdiğiniz `google-services.json` dosyasını `app/` klasörüne ekleyin (repoda bulunmaz, `.gitignore` ile hariç tutulmuştur).
3. `local.properties` dosyasında aşağıdaki alanları tanımlayın:
   ```properties
   sdk.dir=<Android SDK yolunuz>
   MAPS_API_KEY=<Google Maps API anahtarınız>
   ```
4. Projeyi Android Studio'da açın, Gradle sync'in tamamlanmasını bekleyin.

## Derleme

```bash
./gradlew assembleDebug     # debug APK
./gradlew assembleRelease   # release APK (imzalama yapılandırması gerekir)
```

Uygulama çalışma zamanında Kamera, Konum, Bildirim (Android 13+) ve medya/depolama izinlerini ilk açılışta toplu olarak ister.

## Yapılandırma Notları

- `gradle/libs.versions.toml` version kataloğu projede tanımlı ancak `app/build.gradle.kts` bağımlılıkları doğrudan string olarak eklediği için katalog fiilen kullanılmıyor; bazı girdiler (Places, Firebase Data Connect) atıl durumda.
- `AndroidManifest.xml`'de tek activity (`MainActivity`) ve tek servis (`MyFirebaseMessagingService`) tanımlıdır; broadcast receiver yoktur. PDF/görsel paylaşımı için FileProvider yapılandırılmıştır.
- Compose'da Material2 ve Material3 bileşenleri bir arada kullanılmaktadır.
- Hassas dosyalar (`google-services.json`, Maps API key içeren kaynaklar, keystore, `*.apk`/`*.aab` build çıktıları) `.gitignore` ile depo dışında tutulur; bu dosyaları kendi ortamınızda ayrıca sağlamanız gerekir.

## Proje Yapısı

```
app/src/main/java/com/cmc/customer/
├── model/          # Company, Machine, Maintenance, Material, User, Notification... veri sınıfları
├── screen/
│   ├── auth/       # Login, Log
│   ├── calendar/   # Planlanmış bakım takvimi
│   ├── company/    # Şirket yönetimi
│   ├── machine/    # Makine detay ve tipe özel diyaloglar
│   ├── main/       # AppNavigation, MainMenuScreen
│   ├── maintenance/# Bakım planlama/detay/tamamlama
│   ├── material/   # Malzeme/stok yönetimi
│   ├── notification/ # Bildirim ekranı
│   ├── ocr/        # Kamera + ML Kit OCR
│   ├── preparation/# Bakım hazırlık süreci
│   └── user/       # Kullanıcı yönetimi, ayarlar
├── ui/             # Tema (theme/) ve paylaşılan bileşenler (dialogs/, ui/)
├── util/           # PermissionManager, NotificationHelper, FCM servisi, PDF/foto yardımcıları, calendar/ (Retrofit ile tatil API'si)
└── viewmodel/      # Her ekran grubu için MVVM ViewModel'leri
```
