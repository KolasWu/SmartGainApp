# SmartGain — 智慧銷售管理 App

![Android CI](https://github.com/KolasWu/SmartGainApp/actions/workflows/smartgain-ci.yml/badge.svg)

SmartGain 是一款專為小型商家設計的 Android 銷售管理工具，支援商品管理、手動下單、訂單追蹤，並整合網頁版賣場讓顧客自助下單。

---

## 技術棧

| 類別 | 技術 |
|------|------|
| 語言 | Kotlin |
| 架構 | MVVM + Repository Pattern |
| 依賴注入 | Hilt (Dagger) |
| 資料庫 | Firebase Firestore |
| 儲存 | Firebase Storage |
| 驗證 | Firebase Auth |
| 圖片載入 | Glide |
| UI | ViewBinding、Material Design 3、RecyclerView |
| 非同步 | Kotlin Coroutines、StateFlow |
| 測試 | JUnit4、MockK |
| CI | GitHub Actions |
| 網頁賣場 | HTML、Tailwind CSS、Firebase Cloud Functions |

---

## 架構說明
View (Fragment)

↓ 觀察 StateFlow

ViewModel          ← 業務邏輯，不知道 Firebase 的存在

↓ 呼叫
Repository         ← 資料存取層，封裝 Firestore 操作

↓
Firebase Firestore / Storage

- **單向資料流**：Fragment 觀察 ViewModel 的 StateFlow，資料只往一個方向流動
- **依賴注入**：所有 Repository 和 Firebase 實例透過 Hilt 管理，方便測試和替換
- **Batch Write**：下單時使用 Firestore Batch 確保訂單與庫存更新的原子性

---

## 主要功能

- 商品管理：新增、編輯、刪除商品，支援圖片上傳
- 訂單管理：手動建單、狀態追蹤、取消補回庫存
- 總覽儀表板：即時營收、待處理訂單數、低庫存警告
- 網頁賣場：顧客透過專屬連結自助下單，使用 Cloud Functions 處理並發安全

---

## 如何執行

1. Clone 此 repo
2. 在 [Firebase Console](https://console.firebase.google.com) 建立專案
3. 下載 `google-services.json` 放入 `app/` 目錄
4. 在 Android Studio 執行即可

---

## 執行測試

```bash
./gradlew test
```

測試涵蓋：
- `OrderStatus.fromString()` — 所有狀態的正常與邊界情境（17 個測試）
- `OrdersViewModel.executeBatchOrder()` — 空購物車、庫存警告、批次下單（4 個測試）

---

## CI/CD

每次 Push 到 `main` branch，GitHub Actions 自動執行所有單元測試。