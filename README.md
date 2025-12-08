# DontKillMyApp

Helps you set up your phone background tasks so that your apps can finally work for YOU even when not looking at the screen right now. See how is your phone doing and test different settings with DontKillMyApp benchmark.

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="80">](https://f-droid.org/packages/com.urbandroid.dontkillmyapp/)
[<img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png"
     alt="Get it on Google Play"
     height="80">](https://play.google.com/store/apps/details?id=com.urbandroid.dontkillmyapp)

## Features:
* DKMA benchmark: Measure how aggressively is your phone killing background apps
* Guides: Get actionable steps to overcome most background process restrictions
* Make a change:️ Help smartphones stay smart by sharing your benchmark report to dontkillmyapp.com

DontKillMyApp is a tool for benchmarking nonstandard background processing limitations that are done by OEMs on different Android flavours. You can measure before setting up your phone, then go through the setup guides and benchmark again to see how much has your phone been slacking in the background.

You can share your report through the app to the maintainers of the dontkillmyapp.com website who compile it and base the overall negative score on it.

## How does the benchmark work?

The app starts a foreground service with a wake lock and schedules repetitive task on the main thread, a custom thread executor and schedules regular alarms (`AlarmManager.setExactAndAllowWhileIdle`). Then it calculates executed vs. expected. That's it!
