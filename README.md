# NaviBuzz
### Participants
* **Developer**: Omar Ibrahim - _UW CSE_
* **Developer**: Dylan Burton - _UW CSE_
* **Developer**: Allyson Ely - _UW CSE_
* **Needs Expert**: David Miller - _Seattle Transit_

## Abstract
Our overall goal this quarter was to create an audio based navigation tool for visually-impaired transit users that could extend to places with poor internet connectivity. This project is in collaboration with Seattle Sound Transit and individuals with visual impairment disabilities in the surrounding community. Navigating a new environment can be a challenge with a disability, and currently Seattle Transit employs O&M guides to aid disabled transit users in learning a path through transit stations. After learning about the current processes for supporting visually-impaired transit users with O&M guides in Seattle Transit, we decided to build NaviBuzz to be a tool for recording and renavigating personalized paths through indoor environments. NaviBuzz is a navigation system with audio assistance which allows users to work with O&M Guides to record a path through a transit station in the time that the O&M Guide would be training them on that path. Users then have the ability to use the navigation system to retrace that path. This enables users to safely and repeatedly navigate throughout indoor transit environments, as well as reinforcing independent navigation skills for the user. We’d eventually like to be able to use recorded path data to augment GTFS path data within these stations.

### Project Goals
1. Paths are very specific to the person, so the tool should allow very customizable paths
  - Have different landmarks or locations in a station to verify location per user. 
  - They have different preferences for metrics ( feet/ meters/steps)  
2. Allow users to have easy access to path training from O&M guides without having to have O&M guides present
3. Able to navigate through indoor/underground environments
4. Have accessible features to aid with different levels of visual impairment challenges

## Development Plan

### Constrains/Limitations

## Experimental Results

### Bug List
- App seems to time out during RecordPathActivity if it runs too long
- After RecordPathActivity crashes, upon openeing it again hitting "Start Path" btn will crash (propbably because it shouldn't be enabled yet)
- ~"Confirm Landmark" button on replay path screen does nothing~
- Waypoint list on ReplayPathActivity does not respond to replaying path (should delete cards once user has passed their points)
- ~"Stop Path" currently only capable of fully stoping path, would be useful to be able to pause~
- ~using a duplicate path name for a new path fully crashes the app, should be an error message~
- path name gets saved even if user never records path
- deleting a single path from the database does not work
- setting landmark/instruction should create a point at that location, not assign them to the next/last point, gets innacurate with non-straight path lines
- Audio navigation involving orientation will give odd degree directions when walking at a moderate pace (I think voice instructions get added to the queue and it takes a while to catch up)
- Audio navigation can give odd directions, unsure if it's related to the previous point. Needs more testing/refining
- Occasionally when I start recording a path my heading is inaccurate. I'm unsure whether this is a hardware/Navisens/or code issue.

## Conclusion

## User Manual
