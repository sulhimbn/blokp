# Iuran BlokP

Aplikasi Android untuk mengelola pembayaran iuran blok perumahan/apartemen. Memungkinkan pengelola blok mengatur data pembayaran iuran warga atau penghuni dengan mudah dan efisien.

## Deskripsi Singkat

Aplikasi Iuran BlokP adalah solusi lengkap untuk mengelola pembayaran iuran blok perumahan/apartemen. Aplikasi ini dibangun dengan teknologi Android modern menggunakan Kotlin sebagai bahasa pemrograman utama, dilengkapi dengan Java untuk kompatibilitas. Aplikasi menyediakan interface yang intuitif untuk pengelola blok dalam mengatur data pembayaran iuran warga secara efisien.

## Fitur Utama

### 1. Manajemen Pengguna/Warga
- **Tampilan Daftar Pengguna**: Menampilkan informasi lengkap warga termasuk nama, email, alamat, dan avatar
- **Data Iuran Perwarga**: Melacak jumlah iuran yang harus dibayar per warga
- **Total Iuran Individu**: Menghitung total iuran yang telah terkumpul per individu
- **Avatar Pengguna**: Fitur tampilan gambar profil dengan transformasi lingkaran

### 2. Laporan Keuangan Iuran
- **Perhitungan Iuran Bulanan**: Menghitung total iuran yang terkumpul dari semua warga
- **Tracking Pengeluaran**: Mencatat semua pengeluaran dari dana iuran
- **Rekap Total Iuran**: Menghitung saldo akhir setelah dikurangi pengeluaran
- **Laporan Pemanfaatan**: Menampilkan detail penggunaan dana iuran

### 3. Sistem Navigasi
- **Menu Utama**: Interface navigasi sederhana dengan dua opsi utama
- **Aktivitas Terpisah**: Screen khusus untuk daftar warga dan laporan keuangan
- **Navigasi Intuitif**: Mudah berpindah antara fitur-fitur aplikasi

### 4. Integrasi Data Online
- **API Integration**: Sinkronisasi data real-time dengan server eksternal
- **Mock API Support**: Environment development dengan mock API lokal
- **Error Handling**: Penanganan error yang robust dengan pesan toast
- **Network Debugging**: Fitur debug network untuk development

## Teknologi

- **Platform**: Android SDK API level 34
- **Bahasa Pemrograman**: Kotlin
- **Minimum SDK**: Android 7.0 (API 24)
- **Build System**: Gradle

### Dependencies Utama

- **AndroidX Libraries**: Core KTX, AppCompat, Material Design Components, ConstraintLayout
- **UI Components**: RecyclerView dengan LinearLayoutManager untuk daftar data
- **Networking**: Retrofit 2 dengan OkHttp3 untuk komunikasi API REST
- **Image Loading**: Glide dengan transformasi CircleCrop untuk avatar pengguna
- **JSON Processing**: Gson Converter untuk parsing response API
- **Debugging**: Chucker interceptor untuk inspeksi network traffic (hanya di debug mode)

## Struktur Proyek

```
BlokP/
├── app/
 │   ├── src/
 │   │   ├── main/
 │   │   │   ├── java/com/example/iurankomplek/     # Kode sumber utama
 │   │   │   │   ├── MainActivity.kt                 # Activity daftar pengguna
 │   │   │   │   ├── LaporanActivity.kt              # Activity laporan keuangan
 │   │   │   │   ├── MenuActivity.java               # Activity menu utama
│   │   │   │   ├── UserAdapter.kt                  # Adapter RecyclerView untuk pengguna
│   │   │   │   └── PemanfaatanAdapter.kt           # Adapter RecyclerView untuk pemanfaatan
│   │   │   │   └── network/                        # Networking layer
 │   │   │   │       ├── ApiConfig.kt                # Konfigurasi Retrofit
 │   │   │   │       └── ApiService.kt               # Interface API endpoints
 │   │   │   │   └── model/                          # Data models
 │   │   │   │       ├── DataItem.kt                 # Model data item pengguna
 │   │   │   │       ├── UserResponse.kt             # Model response API for user endpoint
│   │   │   │       ├── PemanfaatanResponse.kt      # Model response API for pemanfaatan endpoint
 │   │   │   ├── res/                                # Resources (layout, drawable, values)
 │   │   │   └── AndroidManifest.xml                 # Konfigurasi aplikasi
 │   │   ├── androidTest/                            # UI tests
 │   │   └── test/                                   # Unit tests
 │   ├── build.gradle                                # Konfigurasi build untuk modul app
 ├── build.gradle                                    # Konfigurasi build global
 ├── settings.gradle                                 # Konfigurasi modul yang disertakan
 ├── gradle.properties                               # Propeti global Gradle
 ├── docs/                                           # Dokumentasi tambahan
 │   ├── docker-setup.md                             # Setup lingkungan Docker
 └── README.md                                       # Dokumentasi utama
```

## Lingkungan Pengembangan

### Setup Manual dengan Android Studio
**Prasyarat:**
- Android Studio Flamingo atau versi terbaru
- JDK 8 atau lebih baru
- Android SDK API level 34
- Koneksi internet untuk mengunduh dependencies

**Langkah-langkah:**
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

### Setup dengan Docker (Direkomendasikan)

Untuk lingkungan pengembangan yang konsisten, gunakan Docker:

1. Jalankan setup Docker:
   ```bash
   ./scripts/setup-dev-env.sh
   ```

2. Akses VS Code di: http://localhost:8081

3. Build aplikasi:
   ```bash
   ./scripts/build.sh
   ```

Lihat [`docs/docker-setup.md`](docs/docker-setup.md) untuk instruksi lengkap setup Docker.

## Konfigurasi API

### Endpoints API

Aplikasi menggunakan API Spreadsheet untuk mengambil data pengguna dan pemanfaatan iuran:

- **Base URL**: `https://api.apispreadsheets.com/data/QjX6hB1ST2IDKaxB/`
- **Endpoint Pengguna**: `GET /` - Mengambil data pengguna/warga
- **Endpoint Pemanfaatan**: `GET /` - Mengambil data pemanfaatan iuran

### Model Data

#### DataItem
Data class yang merepresentasikan item data pengguna:

```kotlin
data class DataItem(
    val first_name: String,           // Nama depan
    val last_name: String,            // Nama belakang
    val email: String,                // Email pengguna
    val alamat: String,               // Alamat tempat tinggal
    val iuran_perwarga: Int,          // Jumlah iuran per warga
    val total_iuran_rekap: Int,       // Total rekap iuran
    val jumlah_iuran_bulanan: Int,    // Jumlah iuran bulanan
    val total_iuran_individu: Int,    // Total iuran individu
    val pengeluaran_iuran_warga: Int, // Pengeluaran dari iuran
    val pemanfaatan_iuran: String,    // Deskripsi pemanfaatan
    val avatar: String                // URL avatar pengguna
)
```

#### UserResponse
Model response dari endpoint pengguna:

```kotlin
data class UserResponse(val data: List<DataItem>)
```

#### PemanfaatanResponse
Model response dari endpoint pemanfaatan iuran:

```kotlin
data class PemanfaatanResponse(val data: List<DataItem>)
```

### Environment Configuration

- **Development**: Menggunakan mock API server lokal (`http://api-mock:5000`)
- **Production**: Menggunakan API Spreadsheet eksternal
- **Auto-switching**: Berdasarkan `BuildConfig.DEBUG` atau environment variable `DOCKER_ENV`

## Testing

### Menjalankan Tests
```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest

# Dengan Docker
./scripts/test.sh
```

Lihat [`AGENTS.md`](AGENTS.md) untuk perintah build dan test lengkap.

## Kontribusi

1. Fork repository ini
2. Buat branch fitur baru (`git checkout -b fitur/NamaFitur`)
3. Commit perubahan Anda (`git commit -m 'Menambahkan fitur X'`)
4. Push ke branch (`git push origin fitur/NamaFitur`)
5. Buat Pull Request

## Lisensi

Proyek ini tidak memiliki lisensi spesifik. Gunakan sesuai dengan kebijakan pengembangan internal Anda.

## Aktivitas Aplikasi

### MenuActivity (Java)
Aktivitas utama yang menampilkan menu navigasi aplikasi dengan dua opsi:
- **Tombol Menu 1**: Navigasi ke MainActivity (Daftar Pengguna)
- **Tombol Menu 2**: Navigasi ke LaporanActivity (Laporan Keuangan)
- **Fullscreen Mode**: Interface immersif tanpa status bar

### MainActivity (Kotlin)
Aktivitas yang menampilkan daftar pengguna/warga komplek:
- **RecyclerView**: Daftar pengguna dengan informasi lengkap
- **UserAdapter**: Adapter untuk menampilkan data pengguna dengan avatar
- **Data Fetching**: Mengambil data dari API menggunakan Retrofit
- **Error Handling**: Toast messages untuk error network dan data kosong

### LaporanActivity (Kotlin)
Aktivitas yang menampilkan laporan keuangan iuran:
- **Perhitungan Otomatis**: Menghitung total iuran bulanan, pengeluaran, dan rekap
- **PemanfaatanAdapter**: Adapter untuk menampilkan detail pemanfaatan dana
- **Formula Khusus**: `total_iuran_individu * 3` untuk perhitungan rekap
- **Real-time Updates**: Data diperbarui langsung dari API

## Arsitektur Kode

### Mixed Language
- **Kotlin**: MainActivity.kt, LaporanActivity.kt, adapters, network layer
- **Java**: MenuActivity.java untuk kompatibilitas dan legacy support

### Design Patterns
- **MVVM Light**: Activity sebagai View, Adapter sebagai View Holder
- **Repository Pattern**: ApiConfig dan ApiService untuk data access
- **Adapter Pattern**: RecyclerView adapters untuk UI binding

## Status Proyek

Aplikasi ini dalam tahap pengembangan aktif dengan fitur-fitur inti yang telah berfungsi. Arsitektur hybrid Kotlin-Java memungkinkan transisi bertahap ke Kotlin sepenuhnya.

**Catatan:** Pastikan untuk mengkonfigurasi URL API sebelum menjalankan aplikasi di environment production.

## For Developers

### Documentation
- [API Documentation](docs/API.md) - Complete API endpoint specifications
- [Architecture Documentation](docs/ARCHITECTURE.md) - System architecture and component relationships  
- [Development Guidelines](docs/DEVELOPMENT.md) - Coding standards and development workflow
- [Troubleshooting Guide](docs/TROUBLESHOOTING.md) - Common issues and solutions

### Project Structure
This project follows a simplified MVVM pattern with the following key components:

**Activities (View Layer):**
- `MainActivity.kt` - Displays user list with UserAdapter
- `LaporanActivity.kt` - Displays financial reports with PemanfaatanAdapter
- `MenuActivity.java` - Main menu navigation (Java for compatibility)

**Network Layer:**
- `ApiConfig.kt` - Retrofit configuration with conditional base URLs
- `ApiService.kt` - API interface definitions

**Data Models:**
- `DataItem.kt` - Core data structure for user/iuran information
- `UserResponse.kt` - Response wrapper for user data
- `PemanfaatanResponse.kt` - Response wrapper for financial data

### Development Workflow
1. Check the [Development Guidelines](docs/DEVELOPMENT.md) for coding standards
2. Refer to [Architecture Documentation](docs/ARCHITECTURE.md) for system design patterns
3. Use the [Troubleshooting Guide](docs/TROUBLESHOOTING.md) for common issues
4. Follow the Git workflow: create feature branches, submit PRs with issue references