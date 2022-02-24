# Roadmap
Below is an outline of the planned features to be released with each specific release

## 0.1.0: Basic User
 - Storage of user data: Locally and Cloud
 - Authentication of user credentials
 - Create/Modify/Delete an account

## 0.2.0: Basic calendar
 - Create/Modify/Delete a calendar
 - Calendar properties to implement for this version:
     - Name
	 - Owner
	 - Description
	 - Colour

## 0.3.0: Basic Event
 - Create/Modify/Delete an event
 - Event properties to implement for this version:
     - Name
	 - Calendar
	 - Description
	 - Start/End

 > At this point the underlying logic for creating events should now be present meaning that at this point we should be able to present the events which have been created. This of course will eventually be expanded on to provide a larger feature set but we need to provide a minimal presenter for the calendar data.

## 0.4.0: Calendar View - Dashboard

## 0.5.0: Repeating Event
 - Event Scheduler
 - Repeat rule
 - Event instance

## 0.6.0: Extended Event Data 
 - Calendar properties to implement for this version:
	 - Calendar visibility & modification rights
 - Event properties to implement for this version:
     - Location
	 - Time-zones
	 - Attendees
	 - Event visibility & modification rights


## 0.7.0: Tasks & Todo Lists
 - Todo Lists are time-independent
 - Tasks are time-dependent (maybe events + todo lists) + potentially could also be location-dependent (i.e. shopping lists only happen at a store)
 - Shared properties:
	 - Ordinal number
	 - Description
     - Counter (if maxCount == 1: shows checkbox else: counter UI)
	 - Child items

## 0.8.0: Goals
Automatically generate events allocated to not clash with specific events (busy events) throughout the day, that meets some requirements and hints. 
*E.g. I want to run 3 times a week but after 6pm $\Rightarrow$ Therefore we generate 3 events across the week after 6pm where possible (with the aim to evenly distribute the events)*

 - Goal trackers

## 0.9.0: Nested Event 

> Should now provide the rest of the calendar views

## 0.12.0: Calendar View - Agenda

## 0.13.0: Calendar View - Day

## 0.14.0: Calendar View - Week

## 0.15.0: Calendar View - Month

> Implement some of the non-core features

## 0.16.0: Marked Periods

## 0.17.0: Projects
 - Progress tracker
 - Gantt Charts
 - Kanban board
 
> Now everything I want in the first release has been implemented I should go about making UI/UX look nice and work
 
## 0.17.0+: UI/UX Overhaul
Up until this point most of the UI is present mostly to just fulfil the functionality with very little regard to properly designing the UI/UX to be intuitive and/or aesthetically pleasing
### 0.12.1: Splash Screen
### 0.12.2: Onboarding
Part of the overhaul will be implementation of Onboarding

> Version 1.0+ features will provide integration with external sources

## 1.0.0: Initial Release
 - All features have been implemented and the the UX/UI overhaul completed
 - Final bug fixes
 
## 1.1.0: iCal Export/Import
### 1.1.1: iCal Import

### 1.1.2: iCal Export

## 1.2.0: Integrations
 - System calendar integration
 - Google calendar integration
 - Facebook events integration
 - Trello board integration


> Version 2.0+ will incorporate more social features

## 2.0.0: Public calendar browser
Allow users to search for public calendars which they may subscribe to 

> Version 3.0+ will incorporate a context and inferencing to the app, allowing it to make smart decisions based on what it knows about the user as well as other events
