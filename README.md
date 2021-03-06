# NaviBuzz
### Participants
* **Developer**: Omar Ibrahim - _UW CSE_
* **Developer**: Dylan Burton - _UW CSE_
* **Developer**: Allyson Ely - _UW CSE_
* **Needs Expert**: David Miller - _Seattle Transit_

## Abstract

### Project Goals

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

## Conclusion

## User Manual
