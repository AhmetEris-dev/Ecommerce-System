# Ecommerce System (Microservices)

Bu proje, Spring Boot tabanli mikroservis mimarisi ile gelistirilmis bir e-ticaret backend sistemidir.  
Sistem; kullanici, kimlik dogrulama, urun ve siparis yonetimini ayri servisler olarak sunar ve tek bir API Gateway uzerinden dis dunyaya acar.

## Projenin Amaci

Monolitik bir uygulama yerine, sorumluluklari ayrilmis servislerle daha olceklenebilir ve bakimi kolay bir e-ticaret backend yapisi kurmak.

Bu repoda:
- Kullanici ve firma yonetimi
- JWT tabanli kimlik dogrulama
- Urun ve urun gorseli yonetimi
- Siparis olusturma/listeleme/iptal
- Gateway ile merkezi route yonetimi

## Mimari Genel Bakis

Servisler:
- `api-gateway`: Dis istekleri alir, ilgili servise route eder.
- `auth-service`: Login/refresh/logout is akislari, access + refresh token yonetimi.
- `user-service`: Kullanici ve firma kayit/yonetim islemleri.
- `product-service`: Urun CRUD, urun durumu ve gorsel yonetimi.
- `order-service`: Siparis akislari ve urun servisi entegrasyonu.

Veritabani:
- Her servisin ayri PostgreSQL veritabani vardir:
  - `auth_db`
  - `user_db`
  - `product_db`
  - `order_db`

## Teknoloji Yigini

- Java 17
- Spring Boot 4
- Spring Security
- Spring Data JPA
- Spring Cloud Gateway (WebFlux)
- PostgreSQL
- JWT (`io.jsonwebtoken`)
- Swagger / OpenAPI (`springdoc`)
- Gradle (multi-module)

## Dizin Yapisi

```text
ecommerce-system/
|- api-gateway/
|- auth-service/
|- user-service/
|- product-service/
|- order-service/
|- gradle/
|- build.gradle
|- settings.gradle
|- gradlew / gradlew.bat
```

## API Gateway Route'lari

Gateway, asagidaki path'leri ilgili servislere yonlendirir:
- `/users/**` -> `user-service`
- `/products/**` -> `product-service`
- `/orders/**` -> `order-service`
- `/auth/**` -> `auth-service`

Not: `/internal/**` endpoint'leri dis erisime kapatilmis durumda (403).

## Temel Is Akislari

### 1) Kayit / Kullanici Yonetimi
- Buyer veya seller kaydi yapilir.
- Admin; kullanici/firma durumlarini goruntuleyip guncelleyebilir.

### 2) Giris ve Token Sureci
- Login istegi `auth-service` uzerinden alinir.
- `auth-service`, `user-service` uzerindeki internal dogrulama endpoint'ini cagirir.
- Basarili giriste access token + refresh token uretilir.

### 3) Urun Yonetimi
- Urun listeleme ve detay endpoint'leri public erisimdedir.
- Urun olusturma/guncelleme/silme isleri seller/admin yetkisine baglidir.

### 4) Siparis Sureci
- Siparis olustururken urun bilgisi `product-service` uzerinden dogrulanir.
- Siparis olusturulduktan sonra stok dusme islemi hedeflenir.

## Kurulum

### Gereksinimler
- JDK 17
- PostgreSQL
- Git

### 1) Repoyu klonla
```bash
git clone <repo-url>
cd ecommerce-system
```

### 2) Veritabanlarini olustur
PostgreSQL uzerinde asagidaki DB'leri olusturun:
- `auth_db`
- `user_db`
- `product_db`
- `order_db`

### 3) Servis konfiglerini kontrol et
Her servisteki `application.yml` dosyalarinda:
- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`
- `jwt.secret` (auth-service ve token kullanan servisler)

degerlerini ortaminiza gore guncelleyin.

## Calistirma

Kok dizinde:

```bash
# tum projeyi derle
./gradlew build

# tum testleri calistir
./gradlew test
```

Windows icin:

```powershell
.\gradlew.bat build
.\gradlew.bat test
```

Servisleri tek tek calistirmak icin (ornek):

```bash
./gradlew :user-service:bootRun
./gradlew :product-service:bootRun
./gradlew :order-service:bootRun
./gradlew :auth-service:bootRun
./gradlew :api-gateway:bootRun
```

## Swagger / API Dokumantasyonu

Her serviste springdoc aktiftir. Servis ayaga kalktikten sonra tipik olarak:
- `/swagger-ui.html` veya
- `/swagger-ui/index.html`

uzerinden endpoint dokumantasyonuna erisebilirsiniz.

## Ortam Degiskenleri ve Guvenlik Notlari

Bu repoda `.env.example` bulunmuyor; bircok ayar `application.yml` icinde tanimli.

Canli ortam icin oneriler:
- DB kullanici/sifrelerini ortam degiskenine tasi
- `jwt.secret` degerini guvenli secret manager ile yonet
- Servis URL'lerini (`user-service.base-url`, `product-service.base-url`) env tabanli hale getir
- CORS ve gateway kurallarini ortama gore sertlestir

## Bilinen Durumlar / Gelistirme Notlari

- Bazi akislarda TODO/eksik implementasyon notlari bulunuyor (ozellikle refresh token claim uretilen kisimlar).
- `order-service` ile `product-service` arasinda internal stok dusme endpoint'i adlandirmasini/protokolunu tekrar kontrol etmek faydali olabilir.
- Dockerfile / docker-compose tanimi repo icinde yok.
- `src/test` kapsami sinirli; test sayisi arttirilmali.

## Katki

1. Bir branch acin.
2. Degisikliklerinizi yapin.
3. Testleri calistirin.
4. Pull Request acin.

## Lisans

Bu proje icin lisans bilgisi repoda belirtilmemis.  
Gerekirse kok dizine bir `LICENSE` dosyasi ekleyebilirsiniz.
