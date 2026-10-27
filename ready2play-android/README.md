# Ready2Play Launcher — Android Edition

Launcher game Android bergaya konsol PS5 dengan navigasi sentuh (touch swipe), stik gamepad Bluetooth/USB, dan integrasi game fan port / emulator.

## Fitur Versi Android
1. **Tampilan Konsol PS5**:
   - Layout fullscreen landscape modern dengan dynamic background key art.
   - Responsif untuk berbagai resolusi layar HP, tablet, maupun Android TV.
2. **Navigasi Lengkap**:
   - **Touchscreen**: Geser / swipe layar horizontal untuk pindah game, ketuk tile untuk memilih/main.
   - **Gamepad / Stik**: D-Pad / Analog kiri untuk geser, ✕ (Main), △ (Cari), OPTIONS (Pengaturan), ○ (Keluar/Minimize).
   - **Keyboard**: Panah kiri/kanan, Enter, F2, F3, ESC.
3. **Deteksi Game & Fan Port**:
   - Otomatis mendeteksi game/aplikasi Android yang terpasang di perangkat.
   - Mendukung penambahan game fan port / ROM emulator kustom (misal Hollow Knight port, Def Jam AetherSX2, PPSSPP, dll).
4. **Android Native Bridge**:
   - Terintegrasi dengan `PackageManager` dan `Intent` Android.
   - Dapat didaftarkan sebagai **Default Home Launcher** Android.

---

## 1. Cara Uji Coba / Preview di Browser (Tanpa Install APK Dulu)
Buka terminal di folder `ready2play-android`:
```bash
node preview.js
```
Lalu buka browser di:
`http://localhost:3000`

> **Tips:** Di browser PC, Anda bisa menekan tombol `F12` lalu aktifkan **Device Toolbar (Ctrl+Shift+M)** dan putar ke mode **Landscape** untuk merasakan pengalaman layar HP dengan swipe sentuhan mouse.

---

## 2. Cara Build Menjadi File APK
Folder `ready2play-android` ini adalah proyek standar **Android Studio**:
1. Buka software **Android Studio**.
2. Pilih **Open**, lalu arahkan ke folder `ready2play-android`.
3. Tunggu proses sinkronisasi Gradle selesai.
4. Klik menu **Build** > **Build Bundle(s) / APK(s)** > **Build APK(s)**.
5. File `.apk` akan tercipta di `ready2play-android/app/build/outputs/apk/debug/app-debug.apk` dan siap ditransfer ke HP/tablet Android Anda!
