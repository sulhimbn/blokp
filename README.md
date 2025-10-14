# IuranKomplek

IuranKomplek adalah aplikasi Android yang dirancang untuk mengelola dan mengatur pembayaran iuran atau biaya-biaya yang diperlukan dalam suatu komplek perumahan/apartemen. Aplikasi ini memungkinkan pengelola komplek untuk mengelola data pembayaran iuran warga atau penghuni.

## Deskripsi

Aplikasi ini dibangun dengan teknologi Android modern menggunakan Kotlin sebagai bahasa pemrograman utama. Aplikasi ini mendukung fitur-fitur dasar manajemen iuran komplek, termasuk pengelolaan data pembayaran, pengguna, dan transaksi iuran.

## Fitur

- Pengelolaan data iuran
- Manajemen pengguna/penghuni
- Catatan pembayaran
- Sinkronisasi data online
- Pemrosesan data JSON

## Teknologi

- **Android SDK**: API level 34
- **Bahasa Pemrograman**: Kotlin
- **Minimum SDK**: Android 7.0 (API 24)
- **Dependency Management**: Gradle

### Library yang Digunakan

- **Kotlin Extensions**: androidx.core:core-ktx
- **AppCompat**: androidx.appcompat
- **Material Design**: com.google.android.material
- **UI Layout**: androidx.constraintlayout
- **RecyclerView**: untuk menampilkan data dalam bentuk list
- **Image Loading**: Glide
- **Networking**: Retrofit, OkHttp3, Android Async HTTP
- **JSON Parsing**: Gson
- **Debugging**: Chucker (untuk debugging network)

## Struktur Proyek

```
IuranKomplek/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/iurankomplek/     # Source code utama
│   │   │   ├── res/                                # Resources (layout, drawable, values)
│   │   │   └── AndroidManifest.xml                 # Konfigurasi aplikasi
│   │   ├── androidTest/                            # UI tests
│   │   └── test/                                   # Unit tests
│   ├── build.gradle                                # Konfigurasi build untuk modul app
├── build.gradle                                    # Konfigurasi build global
├── settings.gradle                                 # Konfigurasi modul yang disertakan
├── gradle.properties                               # Properti global Gradle
└── README.md                                       # Dokumentasi ini
```

## Build & Run

### Prasyarat
- Android Studio Flamingo atau versi terbaru
- JDK 8 atau lebih baru
- Android SDK API level 34
- Koneksi internet untuk mengunduh dependencies

### Langkah-langkah Build

1. Clone repository ini:
   ```bash
   git clone <url-repository>
   ```

2. Buka project di Android Studio

3. Build project:
   - Menu Build -> Make Project
   - Atau via terminal: `./gradlew build`

4. Jalankan aplikasi:
   - Klik tombol Run (▶) di Android Studio
   - Atau via terminal: `./gradlew installDebug`

### Konfigurasi API

Aplikasi ini dirancang untuk berkomunikasi dengan API server eksternal. Untuk menjalankan secara penuh:

1. Pastikan API server tersedia
2. Konfigurasi base URL di file konfigurasi aplikasi
3. Setup credentials jika diperlukan

## Kontribusi

1. Fork repository ini
2. Buat branch fitur baru (`git checkout -b fitur/NamaFitur`)
3. Commit perubahan Anda (`git commit -m 'Menambahkan fitur X'`)
4. Push ke branch (`git push origin fitur/NamaFitur`)
5. Buat Pull Request

## Lisensi

Proyek ini tidak memiliki lisensi spesifik. Gunakan sesuai dengan kebijakan pengembangan internal Anda.

## Pengembang

Proyek ini dikembangkan sebagai aplikasi manajemen iuran komplek.

## Catatan

- Pastikan untuk menyesuaikan URL API sebelum menjalankan aplikasi secara penuh
- Aplikasi ini saat ini dalam tahap pengembangan
- Beberapa fitur mungkin belum sepenuhnya selesai