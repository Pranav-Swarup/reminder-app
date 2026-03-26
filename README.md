# 🌸 PebblePing

a straightforward notes+todo+reminder app that doesn't yell at you (unless you want it to).


### download the apk and open it on your phone

```bash
/pebbleping.apk
```

---

setting an alarm on the clock app just to remember to "do that on coming friday" felt wrong. PebblePing lets you drop reminders on any future date and choose if you'd like a gentler anonymous notification or not. There's also daily todos and a convenient note taker. (whole app is lockable if needed)

## what it does

**today** — quick stuff for right now. set a time or don't.

**todos** — brain dump. no date and time. just a list.

**reminders** — the main thing. pick a date, optionally a time, then choose:
- **alert** — normal notification with sound
- **passive** — silent, shows up when you open your phone
- **anonymous** — hides the content. notification just says "pebble-ping says hi!" so nobody reading over your shoulder knows what it's about.

**notes** — tap to write or edit. pastel color-coded cards.

## other stuff

- blazing fast UI, no clutter or bloat in the way of adding a new reminder
- dark and light pastel themes (toggle in settings)
- password or fingerprint lock for the whole app (with backup security question)


## (optional) build it from the code

1. open this entire project repository in android studio
2. sync gradle
3. run on your phone or emulator / build an apk and send it to your phone
4. that's it

## code

kotlin, jetpack compose, material3, gson for persistence, biometric api for fingerprint lock. one activity, one file, no fragments, no navigation library, no room, no firebase, no nonsense.

---

made by pranav, llm credit - claude 