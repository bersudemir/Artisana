📱 Mobil E-Ticaret Uygulaması

Kotlin • Firebase • MVVM • Retrofit • Glide

Bu proje, Kocaeli Üniversitesi Mobil Uygulama Geliştirme dersi kapsamında geliştirilen tam işlevli bir e-ticaret uygulamasıdır. Uygulama hem User hem de Admin rollerini destekler; ürün yönetimi, sipariş yönetimi, favoriler sistemi, sepet yönetimi ve API entegrasyonları içeren kapsamlı bir yapıya sahiptir.

Uygulama Google Play Store’a yüklenmeye hazır olacak şekilde tasarlanmış, modern mimari prensipleriyle geliştirilmiş ve Firebase altyapısıyla desteklenmiştir.

🚀 Özellikler

👤 Kullanıcı (User)

Ürünleri listeleme ve detay görüntüleme

Ürünleri favorilere ekleme / çıkarma

Ürünleri sepete ekleme / sepette miktar güncelleme / silme

Sepet toplam tutarını görme

Sipariş oluşturma

Kendi profil bilgilerini güncelleme

Şifre güncelleme ve şifre sıfırlama

Hesap dondurma / aktifleştirme

🛠 Yönetici (Admin)

Ürün ekleme (fotoğraflı), güncelleme, silme

Ürün stok yönetimi ve satışa açma–kapama

Kullanıcı listeleme, güncelleme, silme, dondurma

Tüm siparişleri görüntüleme 

Sipariş detayı gösteren WebApi-Glide

Siparişleri onaylama

Admin profil bilgilerini düzenleme

USD/TRY API’si ile döviz kuru görüntüleme 

🌐 API Entegrasyonları

Retrofit ile Döviz Kuru API

QR Code API (Sipariş detaylarında QR gösterimi)

🗄 Firebase Entegrasyonları

Firebase Authentication (Kayıt, giriş, şifre sıfırlama, e-posta doğrulama)

Firebase Realtime Database (ürünler, kullanıcılar, favoriler, sepet, siparişler)

🧱 Mimari Yapı

Uygulama modern Android geliştirme standartlarına göre hazırlanmıştır:

MVVM (Model–View–ViewModel)

ViewBinding

Repository Pattern

Singleton Service Yönetimi

Glide ile resim yükleme

Base64 + Resize ile fotoğraf optimizasyonu (Performans için özel geliştirme)

Kod yapısı modülerdir ve her ekran kendi görevini sorumluluk ayrımı prensibiyle yerine getirir.

📸 Ekran Görüntüleri (İsteğe göre eklenebilir)
Admin Paneli Görüntüleri :
![WhatsApp Görsel 2025-12-10 saat 23 26 08_2ee05245](https://github.com/user-attachments/assets/83da11cb-477d-4553-8699-cbe7fd8b6006)
![WhatsApp Görsel 2025-12-10 saat 23 26 08_256a173b](https://github.com/user-attachments/assets/315033dd-4d19-40c9-82c6-cae1d8a6a89c)
![WhatsApp Görsel 2025-12-10 saat 23 26 09_d6045610](https://github.com/user-attachments/assets/41a26fe6-20dc-4abc-a0e3-04285b2ce38a)
![WhatsApp Görsel 2025-12-10 saat 23 26 09_50e7f666](https://github.com/user-attachments/assets/75acd5c2-d3fe-48ef-886c-5628733a5645)
![WhatsApp Görsel 2025-12-10 saat 23 26 09_1d73fe30](https://github.com/user-attachments/assets/d33a8fa7-7fd6-428a-8bb4-61f7d209f45f)
![WhatsApp Görsel 2025-12-10 saat 23 26 09_707ece0e](https://github.com/user-attachments/assets/45ba8d9c-3053-4cb6-9f52-812244727b16)
![WhatsApp Görsel 2025-12-10 saat 23 26 09_796301a3](https://github.com/user-attachments/assets/61c34a50-9788-45b4-a849-ac677487f182)

Kullanıcı Ekranları :
![WhatsApp Görsel 2025-12-10 saat 23 26 10_7bbf2b3c](https://github.com/user-attachments/assets/2c692e10-3d9e-417b-ad7c-0cb5b1a5a467)
![WhatsApp Görsel 2025-12-11 saat 00 16 50_3a2438ae](https://github.com/user-attachments/assets/d2d6d3dd-4e34-4207-aa3f-56f3a25a9593)
![WhatsApp Görsel 2025-12-11 saat 00 16 50_8c1f36bf](https://github.com/user-attachments/assets/97feb635-d304-4b89-88ac-6686a3ff73dc)
![WhatsApp Görsel 2025-12-11 saat 00 16 49_08c26f69](https://github.com/user-attachments/assets/1477e0d0-77b8-454b-be2d-ea8dc7cf7374)


📦 Kullanılan Teknolojiler

Kotlin

Android Studio

Firebase Realtime Database

Firebase Authentication

Retrofit

Glide

Coroutines

ViewBinding

MVVM

🛒 Ana Kullanıcı Akışı

Kullanıcı uygulamaya giriş yapar

Ürünleri listeler

Ürünü favorilere ekleyebilir veya sepete koyabilir

Sepet toplamını görüp sipariş oluşturabilir

Siparişler Firebase'e kaydedilir

Admin panelinde sipariş onaylanabilir

🧩 Admin Paneli Akışı

Admin giriş yapar

Dashboard ekranında toplam ürün, kullanıcı, sipariş ve döviz kuru bilgisi görüntülenir

Ürün ekleme/güncelleme/silme işlemleri yapılır

Kullanıcı yönetimi gerçekleştirilir

Siparişler incelenip onaylanır

🔐 Güvenlik Özellikleri

Firebase Auth ile güvenli giriş

E-posta doğrulama

Eski şifre doğrulamadan şifre güncelleme engellendi

ViewBinding ile NullPointerException önleme

Fragment destroy sonrası binding temizliği

Firebase Realtime DB'de rol bazlı veri ayrımı

🚀 Kurulum

1. Depoyu klonla: git clone https://github.com/bersudemir/artisana.git
   
2. Android Studio ile aç
   
3. Firebase yapılandırması ekle
   app/google-services.json dosyasını kendi projenle değiştir.
   
4. Çalıştır 🎉

📄 Ek Bilgi

Rapor IEEE formatında hazırlanmış olup README içerisinde yer almaktadır.
