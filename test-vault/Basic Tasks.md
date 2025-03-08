- [ ] Basic task [[Dataview Page 1]] #task #status/today #other
- [ ] This Week task #task #status/thisweek
- [ ] Task with subtasks #task #status/today 
	- [ ] Subtask 1
	- [ ] Subtask 2
- [ ] Task with a note #task #status/today 
	- This is a note
- [ ] Task with note and subtask #task #status/backlog
	- [ ] Subtask
	- Note

# Codeblock

```minion
query: tasks
display: list
heading: Foo
exclude:
  source:
    - Other Tasks
groupBy: parent_tag
groupByField: status
groupByOrder:
  - /today AS Today
  - /thisweek AS This Week
```
