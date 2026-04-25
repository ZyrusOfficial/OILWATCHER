# FuelTrack Implementation Plan

## App Analysis Summary

### Core Purpose
A community-sourced gas price monitoring app called "FuelTrack/FuelWatch" that enables users to:
1. Capture gas station price signs with their camera
2. Contribute price data to a shared community database
3. View real-time gas prices on an interactive map
4. Gamify participation through leaderboards and rewards

### Identified Screens & Features
From the 8 HTML prototypes:

1. **Camera Capture Screen** (`camera_capture_final`):
   - Professional camera interface with framing guides
   - Level indicator and scanning visualization
   - Switch camera & flash controls
   - Real-time preview/guides

2. **Analyze & Preview Screen** (`analyze_preview_interactive`):
   - OCR result verification UI
   - Manual price correction interface
   - Fuel type detection (Regular, Midgrade, Premium, Diesel)
   - Confidence indicators

3. **Interactive Map View** (`map_view_interactive`):
   - Price markers with color coding (green=cheap, charcoal=average, white=expensive)
   - Real-time search functionality
   - User location indicator
   - Contribution FAB (floating action button)

4. **Station Details Panel** (`station_details_interactive`):
   - Bottom sheet/side panel design
   - Price comparisons & trending indicators
   - Last update timestamps with contributor attribution
   - Navigation CTA

5. **User Profile Screen** (`profile_interactive`):
   - Contribution statistics (238 contributions, 99% accuracy, #12 rank)
   - User verification badge
   - Action links to history/leaderboard/settings
   - Progress tracking for rewards

6. **Community Leaderboard** (`community_leaderboard_final`):
   - Gamified ranking system with top 8 contributors
   - Points system (12,450 points for current user)
   - Monthly vs all-time rankings
   - Rank movement indicators (+4, trending up/down)

7. **Community History** (`community_history_final`) - History of user contributions

8. **Settings Screen** (`settings_interactive`) - App configuration

### Design System Analysis
The `DESIGN.md` reveals a sophisticated design philosophy called "The Tactile Architect":
- **Colors**: Primary orange (`#FF5E00`/`#A63B00`), charcoal text, paper-like surfaces
- **Typography**: Clash Display (headlines) + DM Sans/Manrope (body)
- **Principles**: No borders, tonal layering, glassmorphism, intentional asymmetry
- **Interactions**: Kinetic animations, tactile feedback, minimal visual clutter

## Technical Implementation Plan

### Recommended Technology Stack

| Component | Technology | Justification |
|-----------|------------|---------------|
| **App Framework** | Kotlin + Jetpack Compose | Native Android with modern declarative UI |
| **ML/OCR Engine** | Google ML Kit (Text Recognition v2) | On-device OCR, supports Latin script, no cloud dependency |
| **Camera** | CameraX | Consistent API, lifecycle management, ML Kit integration |
| **Maps** | Google Maps SDK for Android | Interactive maps, markers, clustering, location services |
| **Database** | Room + SQLite (local) + Firebase Realtime DB (cloud) | Offline-first with sync capabilities |
| **Authentication** | Firebase Authentication | Email/Google sign-in ready |
| **Architecture** | MVVM + Clean Architecture | Separation of concerns, testability |
| **Dependency Injection** | Hilt | Modern DI for Android |
| **Networking** | Retrofit + Moshi | Type-safe API client |
| **Image Processing** | Coil/Glide + OpenCV (if needed) | Image loading + preprocessing for OCR |

### Core Features Implementation Priority

**Tier 1 - MVP (First 4 Weeks)**
1. **Authentication & User Management** - Firebase Auth setup
2. **Camera Integration** - CameraX with ML Kit pipeline
3. **Basic Map View** - Google Maps with mock price markers
4. **Price Submission Flow** - Camera → OCR → Verification → Upload
5. **Basic Station Database** - Firebase Realtime DB structure

**Tier 2 - Core App (Weeks 5-8)**
1. **Advanced OCR Processing** - Custom preprocessing for gas signs
2. **Real Map Integration** - Live price markers with clustering
3. **User Profiles** - Contribution tracking, accuracy metrics
4. **Data Validation System** - Duplicate detection, outlier filtering
5. **Offline Support** - Room database for cached prices

**Tier 3 - Community Features (Weeks 9-12)**
1. **Leaderboard System** - Points, rankings, badges
2. **Gamification** - Rewards, achievements, streaks
3. **Social Features** - User verification, trust scores
4. **Advanced Analytics** - Price trends, station popularity
5. **Content Moderation** - Flagging system, admin tools

### ML/OCR Implementation Details

**Challenges with Gas Price Signs:**
1. **Varied formats** - Digital/LED, analog, handwritten
2. **Lighting conditions** - Night, glare, shadows
3. **Angled shots** - Perspective distortion
4. **Multiple prices** - Regular, Premium, Diesel in one image

**Proposed Solution Pipeline:**
```
1. Image Capture → 2. Preprocessing → 3. ML Kit OCR → 4. Price Parsing → 5. Confidence Scoring → 6. Manual Verification
```

**Preprocessing Steps:**
- Perspective correction (OpenCV)
- Contrast enhancement
- Digital/LED text detection (template matching)
- Region of Interest (ROI) detection for price areas

**Confidence System:**
- Pattern matching (`$X.XX` format)
- Contextual validation (typical price ranges)
- Multi-frame verification for video input option
- Historical comparison with same station

### Database Schema Design

```kotlin
// Firebase Structure
users/
  {userId}/
    name, email, profileImage
    stats: {contributions, accuracy, points, rank}
    history: [{submissionId, timestamp}]
    
stations/
  {stationId}/
    name, address, coordinates
    metadata: {chain, services, hours}
    
prices/
  {stationId}/
    {timestamp}/
      regular, premium, diesel, midgrade (optional)
      userId, confidence, verified, imageUrl
      location: {lat, lng, accuracy}
      
contributions/
  {userId}_{timestamp}/
    submission data for leaderboard calculations
```

### UI/UX Implementation Strategy

**Jetpack Compose Components to Build:**
1. `CameraPreviewScreen` - With framing guides and level indicator
2. `PriceVerificationScreen` - OCR results with manual correction
3. `InteractiveMapScreen` - Google Maps with custom price markers
4. `StationDetailSheet` - Bottom sheet with price comparisons
5. `LeaderboardScreen` - Ranking system with achievements
6. `ProfileScreen` - User stats and contribution history

**Design System Implementation:**
- Custom Compose Theme with Material 3 tokens
- Typography system matching Clash Display + DM Sans
- **No borders rule** - Use elevation and spacing instead
- Glassmorphism effects for navigation elements
- Kinetic orange (`#FF5E00`) as primary action color

### Performance Considerations

1. **ML Processing Optimization:**
   - Downscale images before OCR (720p max)
   - Batch processing for video frames
   - Background thread execution

2. **Map Performance:**
   - Marker clustering for dense areas
   - Viewport-based data loading
   - Cached tile layers

3. **Battery Efficiency:**
   - Optimized camera usage
   - Intelligent location updates
   - Background sync intervals

### Testing Strategy

**Unit Tests:**
- OCR price parsing logic
- Data validation algorithms
- Business logic (points calculation, rankings)

**Integration Tests:**
- Camera → OCR → Database flow
- Map marker loading and clustering
- Firebase sync operations

**UI Tests:**
- Screen navigation flows
- Interactive map functionality
- Camera preview and capture

### Deployment & Scalability

**Initial Scale Planning:**
- Single region launch (test market)
- 1,000-5,000 daily active users target
- Serverless backend (Firebase Cloud Functions)

**Scalability Considerations:**
- Redis caching for frequent price queries
- CDN for user-uploaded images
- Regional database sharding if expanding globally

### Monetization Opportunities

1. **Freemium Model:**
   - Free: Basic features, ads
   - Premium ($3.99/month): Ad-free, detailed analytics, offline maps

2. **Data Licensing:**
   - Historical price data to financial institutions
   - Real-time API for navigation apps

3. **Partnerships:**
   - Gas station chains for promotional pricing
   - Payment apps for cashback integration

### Risks & Mitigations

| Risk | Mitigation |
|------|------------|
| **Low OCR Accuracy** | Manual verification fallback + community validation |
| **Limited Initial Community** | Seed data + gamification to drive engagement |
| **Data Spam/Fraud** | Verification system + trusted user program |
| **Battery Drain** | Optimized ML processing + user education |
| **Privacy Concerns** | Clear data policy + anonymized location options |

### Questions for Your Input

1. **Development Resources:**
   - Will you develop this yourself or need a team?
   - Android development experience level?

2. **Business Model:**
   - Immediate monetization needed or focus on growth first?
   - Target user acquisition channels?

3. **Data Strategy:**
   - Region focus initially? Which geography?
   - Historical data import needed or fresh start?

4. **Timeline:**
   - MVP deadline vs full feature timeline?
   - Beta testing group availability?

5. **Competition:**
   - Similar apps in your target market?
   - Unique selling points to prioritize?

### Recommended Next Steps

1. **Phase 0 - Preparation (1-2 weeks):**
   - Set up Android Studio with necessary SDKs
   - Create Firebase project with authentication
   - Obtain Google Maps API key
   - Test ML Kit OCR with real gas station photos

2. **Phase 1 - Foundation (3-4 weeks):**
   - Create basic app structure with Clean Architecture
   - Implement authentication flow
   - Build camera screen with basic preview
   - Set up database structure

3. **Phase 2 - Core Features (4-6 weeks):**
   - Complete OCR pipeline
   - Implement map with price markers
   - Build price submission flow
   - Add basic user profiles

4. **Phase 3 - Polish (2-3 weeks):**
   - Add gamification elements
   - Implement advanced features
   - Performance optimization
   - Beta testing and bug fixes