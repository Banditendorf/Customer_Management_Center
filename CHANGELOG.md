# Changelog

Bu proje [Semantic Versioning](https://semver.org/) kullanır.

## [1.0.0] - 2026-07-16

### Added
- İlk sürüm: şirket, makine, bakım, malzeme, kullanıcı ve bildirim modülleri.
- Firebase Auth / Firestore / Storage / Cloud Messaging entegrasyonu.
- OCR ile kağıt bakım formu dijitalleştirme (ML Kit).
- Takvim üzerinde planlanmış bakım görünümü ve resmi tatil entegrasyonu.
- Detaylı README, LICENSE (MIT), .gitattributes eklendi.

### Security
- Hardcoded Google Maps API anahtarı kaldırıldı, path traversal düzeltildi, backup kısıtlandı (`fb14373`).

### Changed
- Kullanılmayan kod ve boilerplate temizlendi (`f55e627`).
