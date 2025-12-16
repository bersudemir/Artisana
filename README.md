📱 Mobil E-Ticaret Uygulaması

Kotlin • Firebase • MVVM • Retrofit • Glide

Bu proje, Kocaeli Üniversitesi - Bilişim Sistemleri Mühendisliği Mobil Uygulama Geliştirme dersi kapsamında geliştirilen tam işlevli bir e-ticaret uygulamasıdır. Uygulama hem User hem de Admin rollerini destekler; ürün yönetimi, sipariş yönetimi, favoriler sistemi, sepet yönetimi ve API entegrasyonları içeren kapsamlı bir yapıya sahiptir.

Uygulama Google Play Store’a yüklenmeye hazır olacak şekilde tasarlanmış, modern mimari prensipleriyle geliştirilmiş ve Firebase altyapısıyla desteklenmiştir.

🚀 Özellikler

👤 Kullanıcı (User)

Ürünleri listeleme ve detay görüntüleme

Ürünleri favorilere ekleme / çıkarma

Ürünleri sepete ekleme / sepette miktar güncelleme / sepetten silme

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

Sipariş detayı gösteren Qr Web Api-Glide

Siparişleri onaylama

Admin profil bilgilerini güncelleme

Şifre güncelleme ve şifre sıfırlama

Hesap dondurma / aktifleştirme

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

📸 Ekran Görüntüleri 

### 👨‍💻 Yönetici (Admin) Paneli
| Giriş | Admin Dashboard | Ürün Yönetimi | Ürün Ekleme |
| :---: | :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/83da11cb-477d-4553-8699-cbe7fd8b6006" width="200"> | <img src="https://github.com/user-attachments/assets/315033dd-4d19-40c9-82c6-cae1d8a6a89c" width="200"> | <img src="https://github.com/user-attachments/assets/41a26fe6-20dc-4abc-a0e3-04285b2ce38a" width="200"> | <img src="https://github.com/user-attachments/assets/75acd5c2-d3fe-48ef-886c-5628733a5645" width="200"> |
| **Kullanıcı Yönetimi** | **Sipariş Detayı** | **Sipariş Detay QR Kod** | **Profil** |
| <img src="https://github.com/user-attachments/assets/3a58e48d-b92d-4393-9e0a-0bd5b886c912" width="200"> | <img src="https://github.com/user-attachments/assets/314efc9f-1da2-4bb5-8cb9-0441535bddc7" width="200"> | <img src="https://github.com/user-attachments/assets/45ba8d9c-3053-4cb6-9f52-812244727b16" width="200"> | <img src="https://github.com/user-attachments/assets/61c34a50-9788-45b4-a849-ac677487f182" width="200"> |

### 🛍️ Kullanıcı (User) Arayüzü
| Giriş Ekranı | Sepetim | Favoriler | Profilim |
| :---: | :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/2c692e10-3d9e-417b-ad7c-0cb5b1a5a467" width="200"> | <img src="https://github.com/user-attachments/assets/d2d6d3dd-4e34-4207-aa3f-56f3a25a9593" width="200"> | <img src="https://github.com/user-attachments/assets/97feb635-d304-4b89-88ac-6686a3ff73dc" width="200"> | <img src="https://github.com/user-attachments/assets/1477e0d0-77b8-454b-be2d-ea8dc7cf7374" width="200"> |

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

📊 ERD & Akış Diyagramları

<img width="17601" height="11543" alt="Artisana Akış Diyagramı" src="https://github.com/user-attachments/assets/ac4bd432-1973-4aea-ad4b-41c34656d922" />
<img width="5752" height="5860" alt="Artisana ERD" src="https://github.com/user-attachments/assets/00fac884-f1af-436b-aff0-643d4af1eba9" />

📄 Ek Bilgi

Rapor IEEE formatında hazırlanmış olup README içerisinde yer almaktadır.

[241307014.pdf](https://github.com/user-attachments/files/24112953/241307014.pdf)

Veritabanı yedeği README içerisinde yer almaktadır.

[artisana_veritabani_yedegi.json](https://github.com/user-attachments/files/24192584/artisana_veritabani_yedegi.json)

