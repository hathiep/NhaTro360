# NhaTro360

Android Project

## A. System Design

MVC x MVVM Architecture Patterns

### Model
- **Address**
- **Notification**
- **Room**
- **User**
- **Validate**

### View
- **authenActivity:**
  - `LoginActivity`
  - `RegisterActivity`
  - `ForgotPasswordActivity`
- **mainActivity:**
  - **MainActivity**
  - **Home:**
    - `HomeFragment`
    - `CreateRoomActivity`
      - `AddressFragment`
      - `InformationFragment`
      - `ImageFragment`
      - `ConfirmFragment`
  - **Search:**
    - `SearchFragment`
    - `SearchedRoomsFragment`
  - **Notification:**
    - `NotificationFragment`
  - **Account:**
    - `AccountFragment`
    - `AppInformationFragment`
    - `EditProfileFragment`
    - `PostedRoomsFragment`
    - `SavedRoomsFragment`
    - `ChangePasswordActivity`
- **roomDetailActivity:**
  - `RoomDetailActivity`
  - `FullScreenMapFragment`

### Controller
- `MainViewPagerAdapter`
- `RoomAdapter`
- `ImageUploadAdapter`
- `RoomSingleAdapter`
- `NotificationAdapter`
- `ImagePagerAdapter`

### View Model
- `CreateRoomViewModel`

## B. Technologies

### 1. Tools
- **IDE:** Android Studio
- **API:** Google Maps
  - `play-services-maps:18.0.2`
  - `play-services-location:17.0.0`
- **Figma**
- **Firebase Integration**
  - **Firebase Authentication**
    - **Sign-in method:** Email/Password
    - **Templates**
      - `Email address verification:` Verify email before first login
      - `Password reset:` Forgot password, Change password

  - **Firebase Firestore Database**

  - **Firebase Storage**

### 2. Environment
- **SDK**
  - **CompileSdk Version:** 34
  - **MinSdk Version:** 29
  - **TargetSdk Version:** 34
- **Libraries**
  - `agp = "8.5.0"`
  - `junit = "4.13.2"`
  - `junitVersion = "1.1.5"`
  - `espressoCore = "3.5.1"`
  - `appcompat = "1.7.0"`
  - `material = "1.12.0"`
  - `activity = "1.8.0"`
  - `constraintlayout = "2.1.4"`
  - `googleServices = "4.4.2"`
  - `firebaseAuth = "23.0.0"`
  - `firebaseDatabase = "21.0.0"`
  - `materialVersion = "1.4.0"`
  - `firebaseFirestore = "25.0.0"`
  - `glide = "4.12.0"`
  - `googleAndroidLibrariesMapsplatformSecretsGradlePlugin = "2.0.1"`
  - `playServicesMaps = "19.0.0"`
  - `volley = "1.2.1"`
  - `firebaseStorage = "21.0.0"`
  - `googleGmsGoogleServices = "4.4.2"`
  - `firebaseMessaging = "24.0.0"`

