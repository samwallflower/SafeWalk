# Device test checklist

Run on a real phone with the backend up. Use throwaway accounts and contacts; emails to contacts are real.

## Install and start
- [✅] Home-screen icon is the blue pin and the label reads "SafeWalk".
- [✅] Launch shows the blue screen with the pin, then the app.

## Account
- [✅] Register, verify by email, sign in. A wrong password shows a plain message.
- [✅] Kill the app and reopen: still signed in. Sign out from Settings: back at login.

## Map and reports
- [] Map shows your position and nearby dots; every dot is its own dot.
- [✅] Tap a dot: sheet opens; upvote/downvote update at once and survive a refresh.
- [✅ ] Report: pick a spot, category, description; it appears on the map.

## Walk
- [✅] Plan a route; Start is refused when more than 100 m from it.
- [✅] Walking updates progress; leaving the phone still raises the idle prompt, "I'm OK" clears it.
- [ ] Leave the route: off-route banner shows. Arrive: summary shows time taken.
- [✅] Hold SOS for 1 s: emergency screen, call button opens the dialer only.
- [✅] Switch to another app mid-walk: a notification arrives for an alert (Settings shows if notifications are off).
- [ ] Lose the connection (airplane mode 10 s): banner shows, then recovers.

## My Safety
- [✅] Stat tiles are the same height in each row, including with five contacts.
- [✅] "Walk with SafeWalk" card opens the Walk tab.
- [✅ ] Chart shows the last 7 days.
- [✅] Add, edit and remove a contact, including one that was notified in a past emergency (no technical errors shown).
- [✅] Contact limit of five disables adding. `0630123` is rejected, `+36301234567` is accepted.
- [✅] Walk history and My reports lists open, and a report can be deleted.

## Accessibility and comfort
- [ ] TalkBack: every button is announced with a purpose (SOS, vote buttons, call, menu).
- [ ] Settings > Display > Font size at the largest: no clipped text on login, map sheet, walk and My Safety.
- [ ] Rotate and return: no state lost mid-walk.
