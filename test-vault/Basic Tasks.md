- [ ] Basic task #task #status/today #other
- [ ] This Week task #task #status/thisweek
- [ ] Task with subtasks #task #status/today 
	- [ ] Subtask 1
	- [ ] Subtask 2
- [ ] Task with a note #task #status/today 
	- This is a note
- [ ] Task with note and subtask #task #status/today 
	- [ ] Subtask
	- Note

# Codeblock

```minion
query: tasks
display: list
heading: Foo
groupBy: parent_tag
groupByField: status
groupByOrder:
  - /today AS Today
  - /thisweek AS This Week
```
