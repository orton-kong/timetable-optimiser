# Timetable Optimiser

A Java console-only timetable optimisation application generated from the supplied specification, architecture diagram, and domain model.

## Run in IntelliJ

1. Open this folder as a project in IntelliJ IDEA.
2. Mark `src` as the source root if IntelliJ does not do it automatically.
3. Run `timetableoptimizer.Main`.

## Run from terminal

```bash
javac -d out src/timetableoptimizer/*.java
java -cp out timetableoptimizer.Main
```

## CSV format

The importer expects exactly these columns:

`Topic, Availability, Class, Class instance, Date, Day, Time, Location`

Example data is included at `sample-data/sample-classes.csv`.

## Notes

- No GUI is used.
- Class import updates duplicates when Topic, Availability, Class, Class instance, Date, and Day match.
- Browse/view combine class records by topic code, topic name, attendance mode, campus, semester, availability number, class, and class instance.
- Search and edit are limited to the fields allowed in the specification.
- Timetable generation chooses class instances while avoiding hard clashes and invalid commute gaps unless lecture overlap is explicitly allowed for lectures.
- Timetable edit swaps a class instance only with another instance of the same topic and class.
- Timetable export writes CSV.
