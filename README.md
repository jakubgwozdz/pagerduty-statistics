# pagerduty-statistics
A project for 25th Schibsted Hackaton

## What is it for
The aim of this tool is to provide a way to display team members participation
in PagerDuty on-calls schedule. 

## How it is working
Whole application runs in user's browser, and there is no processing done
on hosting server. It requires user's personal API key for PagerDuty services.

## How it is written
Whole application has been written by a single BACKEND DEVELOPER, in pure Kotlin.
Nothing in javascript, typescript, whateverSCRIPT. So forget me the ugly UI.
Half of time spent on that project was wasted on centering the images, I hate UI.

Kotlin 1.4.10 multiplatform feature has been employed for this task.
The "development" webpack build is deployed to github pages, so you can
debug it freely even kotlin line-by-line, there's no obfuscation, and source maps
should be visible under F12.

Have fun at https://jakubgwozdz.github.com/pagerduty-statistics/

## Legend

Right now the total time spent on-call is divided into three areas:
* WEEKEND (RED...ish) - all the time between Friday 17:00 to Monday 09:00
* AFTERDUTY (BLACK) - afternoons and nights on weekdays
* WORKDAY (WHITE) - Monday-Friday 09:00-17:00

## Build instructions

before pushing changes, let's do the development webpack and copy it to 
`docs` directory, so we can have github pages updated with new version

```shell script
rm -Rf build/distributions/*
rm -Rf docs/*
./gradlew build
cp -R build/distributions/* docs
```

## Legal note

Project shared on my public account with approval of my direct manager, but all code created
during company's hackdays is owned by schibsted.com



