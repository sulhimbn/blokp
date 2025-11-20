# API Documentation

## Base URLs
- Production: https://api.apispreadsheets.com/data/QjX6hB1ST2IDKaxB/
- Development: http://api-mock:5000/data/QjX6hB1ST2IDKaxB/

## Endpoints

### GET /data/QjX6hB1ST2IDKaxB/ (users)
Mengambil data pengguna untuk ditampilkan di MainActivity

#### Response Format
```json
{
  "data": [
    {
      "first_name": "John",
      "last_name": "Smith", 
      "email": "john@example.com",
      "alamat": "Jl. Contoh No. 1",
      "iuran_perwarga": 150000,
      "total_iuran_rekap": 1800000,
      "jumlah_iuran_bulanan": 150000,
      "total_iuran_individu": 150000,
      "pengeluaran_iuran_warga": 50000,
      "pemanfaatan_iuran": "Perbaikan jalan komplek",
      "avatar": "https://example.com/avatar.jpg"
    }
  ]
}
```

### GET /data/QjX6hB1ST2IDKaxB/ (pemanfaatan)
Mengambil data pemanfaatan iuran untuk ditampilkan di LaporanActivity

#### Response Format
```json
{
  "data": [
    {
      "first_name": "John",
      "last_name": "Smith", 
      "email": "john@example.com",
      "alamat": "Jl. Contoh No. 1",
      "iuran_perwarga": 150000,
      "total_iuran_rekap": 1800000,
      "jumlah_iuran_bulanan": 150000,
      "total_iuran_individu": 150000,
      "pengeluaran_iuran_warga": 50000,
      "pemanfaatan_iuran": "Perbaikan jalan komplek",
      "avatar": "https://example.com/avatar.jpg"
    }
  ]
}
```

## Error Handling
- 200: Success - Data successfully retrieved
- 404: Data not found - No data available at the requested endpoint
- 500: Server error - Internal server error occurred
- Network errors: Connection timeout, no internet connection

## Authentication
This API does not require authentication at the moment.

## Rate Limiting
No specific rate limiting is documented for this API.