# FP_Komputasi_Bergerak_Android
FP Komputasi Bergerak - Melakukan prediksi aktivitas yang dilakukan pengguna dengan sensor Accelerometer. Aktivitas yang diprediksi adalah:

0 - Diam
1 - Naik Sepeda Motor
2 - Lompat-Lompat

Per aktivitas dilakukan rekam data selama satu menit dengan jumlah data terekam sebanyak 10 data/detik (600 data/menit).
Untuk melakukan prediksi, dilakukan dengan algoritma kNN dengan k = 5, data sebanyak 450 data per aktivitas yang dilakukan penghitungan fitur antara lain:

1. Average
2. Standard Deviation
3. Max Value
4. Min Value
5. Average Absolute Difference
6. Average Resultant Acceleration

Keenam fitur tersebut dijabarkan per masing-masing axis (x, y, z) kecuali untuk Average Resultant Acceleration, sehingga nantinya akan ada total 16 fitur untuk diprediksi. Sebelum 
melakukan prediksi, data dilakukan pre-processing dengan overlap window dengan ukuran window per 5 detik (50 data) dan overlap sebesar 60% sehingga untuk 450 data train setelah 
diekstraksi fiturnya akan ada ((450 -50) // 20) + 1 yaitu 21 baris data fitur dan labelnya.

Untuk mengecek akurasi disediakan juga data testing sebanyak 150 per aktivitasnya yang juga dilakukan pre-processing dan ekstraksi fitur, menghasilkan 6 baris data fitur dan label 
per aktivitasnya.

Untuk menjalankan aplikasi, pindah file `ready_train_50.csv` dan `ready_test_50.csv` ke `internal_storage/Android/data/com.example.accelerometersensor/files/`.
