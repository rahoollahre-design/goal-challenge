# Architecture

## `game-core`
Pure Kotlin, no Android imports. Owns:
- game state
- fixed-step physics
- wall/ground collisions
- ball-to-ball collision
- black-hole eligibility/geometry
- scoring and round lifecycle
- deterministic RNG injection
- engine events for audio/UI

## `app`
Android-specific layer. Owns:
- Activity lifecycle
- native Canvas rendering
- Choreographer frame pacing
- input/touch handling
- SoundPool / TextToSpeech / vibration
- score and settings persistence
- application resources

## Timing
Physics is authored in the original web game's frame-based units and advances at `1000/60 ms`. Rendering follows device VSYNC independently. Long resume gaps are clamped and backlog is dropped after six catch-up steps, matching the defensive behavior of the web game.

## Coordinate system
All simulation geometry uses a stable 430×764 world. The Android `GameView` aspect-fits this world inside the current window, so screen size never changes game physics.
