# FlashMatch Mobile — Implementation Context

## What this is
A Kotlin + Jetpack Compose Android flashcard app built for a class demo. Users sign in with Google, create vocabulary decks saved to Firebase Firestore, and study them through a weighted-repetition quiz.

---

## Tech Stack
- **Language**: Kotlin, Jetpack Compose (Material 3), dark theme forced
- **Auth**: Firebase Authentication with Google Sign-In (`play-services-auth:21.3.0`)
- **Database**: Firebase Firestore (real-time listeners via `callbackFlow`)
- **Navigation**: Compose Navigation 2.8.9 with slide animations
- **Min SDK**: 26 (Android 8.0), Target: 36
- **Build**: AGP 8.13.2, Kotlin 2.0.21

---

## File Structure (all under `app/src/main/java/com/flashmatch/mobile/`)

```
MainActivity.kt                     — entry point, holds AuthViewModel
auth/
  AuthViewModel.kt                  — AndroidViewModel, Google Sign-In + Firebase credential
data/
  model/Card.kt                     — id, front, back, correctCount, incorrectCount, correctnessScore (Float, default 0.5)
  model/Deck.kt                     — id, name, description, cardCount, createdAt
  repository/DeckRepository.kt      — Firestore CRUD, observeDecks/Cards via Flow, batchUpdateCards
navigation/
  Screen.kt                         — sealed class with route strings + createRoute() helpers
  NavGraph.kt                       — NavHost, slide transitions, creates DeckRepository once via remember{}
viewmodel/
  HomeViewModel.kt                  — observes decks Flow, deleteDeck()
  DeckDetailViewModel.kt            — loads deck + observes cards, deleteDeck() sets _deleted flag
  CreateDeckViewModel.kt            — createDeck() and updateDeck() (reused for EditDeckScreen)
  QuizViewModel.kt                  — weighted random sampling, session state, QuizSessionCache singleton
ui/
  theme/Color.kt, Theme.kt, Type.kt — forced dark scheme, Primary=#5C6BC0, Background=#121212, Surface=#1E1E1E
  components/
    DeckCard.kt                     — clickable card row in home list
    FlashCard.kt                    — card flip animation (animateFloatAsState + rotationY + cameraDistance)
    ProgressBar.kt                  — animated LinearProgressIndicator for quiz
  screens/
    SplashScreen.kt                 — 900ms delay, checks auth state, navigates to Home or Login
    LoginScreen.kt                  — Google Sign-In button, handles ActivityResult
    HomeScreen.kt                   — deck list, FAB to create, sign-out dialog
    CreateDeckScreen.kt             — name/desc fields + inline card editor list
    DeckDetailScreen.kt             — card list (tappable to flip), Edit/Delete/Start Quiz actions
    EditDeckScreen.kt               — loads existing deck, edit existing cards, add new cards, delete cards
    QuizScreen.kt                   — shows FlashCard, Flip button, then Correct/Wrong buttons
    ResultScreen.kt                 — accuracy %, hardest cards list, Study Again / Home buttons
```

---

## Navigation Flow

```
SplashScreen (900ms)
  ├── signed in  →  HomeScreen
  └── not signed in  →  LoginScreen
        └── Google Sign-In success  →  HomeScreen

HomeScreen
  ├── FAB  →  CreateDeckScreen  →  (save)  →  HomeScreen
  └── tap deck  →  DeckDetailScreen
        ├── Edit button  →  EditDeckScreen  →  (save)  →  DeckDetailScreen (popBackStack)
        ├── Delete button  →  dialog  →  (confirm)  →  HomeScreen (popBackStack to Home)
        └── Start Quiz  →  QuizScreen
              └── all cards cleared  →  ResultScreen
                    ├── Study Again  →  QuizScreen (fresh session)
                    └── Home  →  HomeScreen
```

---

## Firestore Data Model

```
users/{uid}/decks/{deckId}          — Deck document
users/{uid}/decks/{deckId}/cards/{cardId}  — Card document
```

Security rules (must be set in Firebase Console):
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId}/decks/{deckId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      match /cards/{cardId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
  }
}
```

---

## Quiz Logic (QuizViewModel)

- **Session start**: loads all cards from Firestore, clears session state
- **Weighted sampling**: `weight = 1 - correctnessScore` (new cards get 0.5 weight, hardest get ~1.0)
- **markCorrect**: increments correctCount, recalculates score, adds to clearedIds set
- **markWrong**: increments incorrectCount, recalculates score, increments retryMap[cardId], card stays in pool
- **Session ends**: when `clearedIds.size == totalCards`
- **On complete**: batch-writes all updated cards to Firestore, stores hardestCards in `QuizSessionCache` singleton (for ResultScreen)
- **correctnessScore formula**: `correctCount / (correctCount + incorrectCount)` — default 0.5 for new cards

---

## Known Setup Requirements (must be done before first run)

1. **Gradle sync** in Android Studio — generates `default_web_client_id` string resource from `google-services.json` (used by AuthViewModel). App will not compile without this sync.
2. **Firebase Console**:
   - Google Sign-In enabled in Authentication → Sign-in methods
   - Firestore database created (start in test mode or add security rules above)
   - SHA-1 fingerprint of debug keystore added to Firebase app settings (required for Google Sign-In)
3. **SHA-1 fingerprint**: run `./gradlew signingReport` in terminal, copy the SHA-1 from `debug` variant, add to Firebase Console → Project Settings → Your Apps → Android app

---

## Potential Issues to Watch

| Issue | Likely Cause | Fix |
|---|---|---|
| App crashes on launch | Missing Gradle sync (no `default_web_client_id`) | Sync project in Android Studio |
| Google Sign-In fails / "Developer Error" | SHA-1 not registered in Firebase | Add debug SHA-1 to Firebase Console |
| Google Sign-In fails silently | `requestIdToken` web client ID mismatch | Re-download `google-services.json` after adding SHA-1 |
| Cards not saving | Firestore rules blocking write | Set rules to allow authenticated user writes |
| Home screen stuck on loading | Firestore offline / auth not set up | Check Firebase Firestore is enabled |
| Quiz ends immediately | No cards in deck | Add at least 1 card before starting quiz |
| Card flip looks wrong | Rendering issue | Usually fine on real device vs emulator |

---

## Happy Path Test Checklist

- [ ] App launches → splash → login screen (or home if already signed in)
- [ ] Google Sign-In completes and navigates to Home
- [ ] Create a deck with name, description, and 5+ cards
- [ ] Deck appears in Home list with correct card count
- [ ] Restart app → deck still loads from Firestore (persistence check)
- [ ] Tap deck → DeckDetailScreen shows all cards, each tappable to flip
- [ ] Edit deck → can change name, edit a card's text, add a new card, delete a card
- [ ] Start Quiz → progress bar shows 0/N, card flip animation works
- [ ] Tap card → flips to reveal definition
- [ ] Mark Correct → card cleared, progress increments
- [ ] Mark Wrong → card stays in pool, comes back (may repeat)
- [ ] Session ends only when all cards cleared (progress = N/N)
- [ ] Result screen shows accuracy %, hardest cards (if any were retried)
- [ ] Study Again starts a fresh session on same deck
- [ ] Home button returns to deck list
- [ ] Sign out → redirects to Login screen, can sign back in

---

## Color Reference
- Primary (Indigo): `#5C6BC0`
- Background: `#121212`
- Surface: `#1E1E1E`
- SurfaceVariant: `#2A2A2A`
- Correct (green): `#4CAF50`
- Wrong (red): `#F44336`
